package restaurantrush.checks;

import restaurantrush.controller.GameConfig;
import restaurantrush.enums.CustomerState;
import restaurantrush.model.Menu;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Order;
import restaurantrush.model.Restaurant;
import restaurantrush.model.customer.CriticCustomer;
import restaurantrush.model.customer.Customer;
import restaurantrush.model.customer.RegularCustomer;
import restaurantrush.model.customer.VIPCustomer;

public final class Stage2Checks {
    private Stage2Checks() {
    }

    public static void main(String[] args) {
        System.out.println("Stage 2 checks");
        Menu menu = Menu.standard();
        MenuItem burger = menu.get(1);

        GameConfig cfg = GameConfig.baseSetting();
        Check.equal("schedule: turn 4 is a VIP", "VIP", cfg.arrivalTypeName(4));
        Check.equal("schedule: turn 7 is a Critic", "Critic", cfg.arrivalTypeName(7));
        Check.equal("schedule: still six arrivals", 6, cfg.getArrivals().size());

        Check.equal("loss per turn: Regular 8 / VIP 5 / Critic 12", "8/5/12",
                new RegularCustomer(1, 1).getSatisfactionLossPerTurn() + "/"
                        + new VIPCustomer(2, 4).getSatisfactionLossPerTurn() + "/"
                        + new CriticCustomer(3, 7).getSatisfactionLossPerTurn());
        Check.equal("bill: VIP gets 10% off a $15.00 Burger -> $13.50", 1350, new VIPCustomer(2, 4).billFor(burger));
        Check.equal("bill: Critic and Regular pay full price", "1500/1500",
                new CriticCustomer(3, 7).billFor(burger) + "/" + new RegularCustomer(1, 1).billFor(burger));
        Check.equal("Critic order preference: most demanding item (Pasta, 2 units)", "Pasta",
                new CriticCustomer(3, 7).chooseDish(menu).getName());

        Check.equal("Critic served a hot dish (READY last turn): no penalty", 100, servedCritic(1));
        Check.equal("Critic served a cold dish (READY 2 turns earlier): -20", 80, servedCritic(2));
        Check.equal("Regular is not affected by a cold dish", 100, servedRegular(2));

        Check.that("same flow for every type: VIP and Critic go WAITING -> SEATED -> ORDERED",
                flows(new VIPCustomer(1, 1)) && flows(new CriticCustomer(2, 1)));
        Check.finish("Stage2Checks");
    }

    private static int servedCritic(int turnsAfterReady) {
        return serve(new CriticCustomer(1, 1), turnsAfterReady);
    }

    private static int servedRegular(int turnsAfterReady) {
        return serve(new RegularCustomer(1, 1), turnsAfterReady);
    }

    private static int serve(Customer c, int turnsAfterReady) {
        Restaurant r = new Restaurant(2);
        r.admit(c);
        r.seatCustomer(c.getId());
        MenuItem salad = Menu.standard().get(0);
        Order o = new Order(c, salad, c.billFor(salad), 1);
        c.placeOrder(o);
        o.prepareOneUnit(1);
        c.serve(1 + turnsAfterReady);
        return c.getSatisfaction();
    }

    private static boolean flows(Customer c) {
        Restaurant r = new Restaurant(2);
        r.admit(c);
        boolean ok = c.getState() == CustomerState.WAITING;
        r.seatCustomer(c.getId());
        ok &= c.getState() == CustomerState.SEATED;
        MenuItem dish = c.chooseDish(Menu.standard());
        c.placeOrder(new Order(c, dish, c.billFor(dish), 1));
        return ok && c.getState() == CustomerState.ORDERED;
    }
}
