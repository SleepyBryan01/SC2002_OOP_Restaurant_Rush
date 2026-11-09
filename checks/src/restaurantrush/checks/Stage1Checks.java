package restaurantrush.checks;

import restaurantrush.controller.GameConfig;
import restaurantrush.enums.CustomerState;
import restaurantrush.enums.OrderStatus;
import restaurantrush.model.ActionResult;
import restaurantrush.model.Menu;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Order;
import restaurantrush.model.Restaurant;
import restaurantrush.model.customer.Customer;
import restaurantrush.model.customer.RegularCustomer;
import restaurantrush.staff.Cashier;
import restaurantrush.staff.Chef;

public final class Stage1Checks {
    private Stage1Checks() {
    }

    public static void main(String[] args) {
        System.out.println("Stage 1 checks");
        baseSetting();
        menu();
        briefExampleTimeline();
        abandonment();
        oneTaskPerTurn();
        cashierOldestFirstOnePerTurn();
        seatingRules();
        Check.finish("Stage1Checks");
    }

    private static void baseSetting() {
        GameConfig cfg = GameConfig.baseSetting();
        Check.equal("base setting: 18 turns", 18, cfg.getMaxTurns());
        Check.equal("base setting: 2 tables", 2, cfg.getTableCount());
        Check.equal("base setting: arrivals on turns 1,4,7,10,13,16", "[1, 4, 7, 10, 13, 16]",
                cfg.getArrivals().keySet().toString());
        Check.equal("objective: 4 paid, $45.00, average 60", "4/4500/60",
                cfg.getMinPaidCustomers() + "/" + cfg.getMinRevenueCents() + "/" + cfg.getMinAverageSatisfaction());
    }

    private static void menu() {
        Menu menu = Menu.standard();
        Check.equal("menu: Salad $9.00, 1 unit", "Salad 900 1", describe(menu.get(0)));
        Check.equal("menu: Burger $15.00, 1 unit", "Burger 1500 1", describe(menu.get(1)));
        Check.equal("menu: Pasta $18.00, 2 units", "Pasta 1800 2", describe(menu.get(2)));
        Check.equal("food choice: customer #3 orders item (3-1) mod 3 = Pasta", "Pasta",
                new RegularCustomer(3, 7).chooseDish(menu).getName());
    }

    private static void briefExampleTimeline() {
        Restaurant r = new Restaurant(2);
        Chef chef = new Chef();
        Cashier cashier = new Cashier();
        Customer c = new RegularCustomer(1, 1);
        MenuItem pasta = Menu.standard().get(2);

        r.admit(c);
        r.seatCustomer(1);
        endTurn(r, cashier, 1);
        Check.equal("3.3 turn 1: seated, satisfaction 92", 92, c.getSatisfaction());

        chef.startTurn();
        Order order = new Order(c, pasta, c.billFor(pasta), 2);
        c.placeOrder(order);
        chef.cook(r, 1, 2);
        endTurn(r, cashier, 2);
        Check.equal("3.3 turn 2: order taken, unit 1 of 2, satisfaction 84", "PREPARING 1/2 84",
                order.describeStatus() + " " + c.getSatisfaction());

        chef.startTurn();
        chef.cook(r, 1, 3);
        Check.that("3.3 turn 3: READY but cannot be served in the same turn",
                order.getStatus() == OrderStatus.READY && !order.canBeServed(3));
        endTurn(r, cashier, 3);
        Check.equal("3.3 turn 3: satisfaction 76", 76, c.getSatisfaction());

        Check.that("3.3 turn 4: READY dish can now be served", order.canBeServed(4));
        c.serve(4);
        endTurn(r, cashier, 4);
        Check.equal("3.3 turn 4: SERVED / EATING, no further loss (76)", "SERVED EATING 76",
                order.getStatus() + " " + c.getState() + " " + c.getSatisfaction());

        Check.that("3.3 turn 5: cashier cannot charge before READY_TO_PAY", !c.canPay(5));
        endTurn(r, cashier, 5);
        Check.equal("3.3 turn 5: finished eating -> READY_TO_PAY", CustomerState.READY_TO_PAY, c.getState());
        Check.that("3.3 turn 5: became READY_TO_PAY this turn, so cannot pay this turn", !c.canPay(5));

        endTurn(r, cashier, 6);
        Check.equal("3.3 turn 6: PAID $18.00, satisfaction 76 recorded, table freed", "PAID 1800 76.0 true",
                c.getState() + " " + r.getRevenueCents() + " " + r.getAveragePaidSatisfaction().getAsDouble()
                        + " " + r.getTables().get(0).isFree());
    }

    private static void abandonment() {
        Restaurant r = new Restaurant(2);
        Customer c = new RegularCustomer(1, 1);
        r.admit(c);
        r.seatCustomer(1);
        MenuItem salad = Menu.standard().get(0);
        Order order = new Order(c, salad, c.billFor(salad), 1);
        c.placeOrder(order);
        int turns = 0;
        while (c.getState() != CustomerState.LEFT && turns < 20) {
            r.applyEndOfTurnPatience();
            turns++;
        }
        Check.equal("abandonment: Regular (-8) leaves after 13 unserved turns", 13, turns);
        Check.equal("abandonment: unfinished order cancelled", OrderStatus.CANCELLED, order.getStatus());
        Check.that("abandonment: table freed, no revenue, unhappy departure counted",
                r.getTables().get(0).isFree() && r.getRevenueCents() == 0 && r.getUnhappyDepartures() == 1);
    }

    private static void oneTaskPerTurn() {
        Restaurant r = new Restaurant(2);
        Customer a = new RegularCustomer(1, 1);
        Customer b = new RegularCustomer(2, 1);
        for (Customer c : new Customer[] {a, b}) {
            r.admit(c);
            r.seatCustomer(c.getId());
            MenuItem pasta = Menu.standard().get(2);
            c.placeOrder(new Order(c, pasta, c.billFor(pasta), 1));
        }
        Chef chef = new Chef();
        chef.startTurn();
        ActionResult bad = chef.cook(r, 9, 1);
        Check.that("invalid choice is explained and does NOT use up the task", !bad.isSuccess() && !chef.hasActed());
        Check.that("valid choice uses the task", chef.cook(r, 1, 1).isSuccess() && chef.hasActed());
        Check.that("second task in the same turn is rejected", !chef.cook(r, 2, 1).isSuccess());
    }

    private static void cashierOldestFirstOnePerTurn() {
        Restaurant r = new Restaurant(2);
        Cashier cashier = new Cashier();
        Customer first = readyToPay(r, 1, 3);
        Customer second = readyToPay(r, 2, 2);
        cashier.startTurn();
        cashier.collectNextPayment(r, 4);
        Check.that("cashier: oldest READY_TO_PAY customer pays first, one payment per turn",
                second.getState() == CustomerState.PAID && first.getState() == CustomerState.READY_TO_PAY);
    }

    private static void seatingRules() {
        Restaurant r = new Restaurant(2);
        for (int id = 1; id <= 3; id++) {
            r.admit(new RegularCustomer(id, 1));
        }
        r.seatCustomer(1);
        r.seatCustomer(2);
        ActionResult full = r.seatCustomer(3);
        Check.that("seating: no free table -> rejected with a reason", !full.isSuccess() && !full.getMessage().isEmpty());
        Check.that("seating: a table stays occupied until its customer pays or leaves", r.findFreeTable().isEmpty());
    }

    private static Customer readyToPay(Restaurant r, int id, int readyTurn) {
        Customer c = new RegularCustomer(id, 1);
        r.admit(c);
        r.seatCustomer(id);
        MenuItem salad = Menu.standard().get(0);
        Order o = new Order(c, salad, c.billFor(salad), 1);
        c.placeOrder(o);
        o.prepareOneUnit(1);
        c.serve(readyTurn - 1);
        c.finishEatingIfDone(readyTurn);
        return c;
    }

    private static void endTurn(Restaurant r, Cashier cashier, int turn) {
        cashier.startTurn();
        cashier.collectNextPayment(r, turn);
        r.finishEating(turn);
        r.applyEndOfTurnPatience();
    }

    private static String describe(MenuItem item) {
        return item.getName() + " " + item.getPriceCents() + " " + item.getPrepUnits();
    }
}
