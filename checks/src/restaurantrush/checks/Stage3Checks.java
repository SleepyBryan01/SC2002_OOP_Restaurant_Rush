package restaurantrush.checks;

import restaurantrush.model.ComboMeal;
import restaurantrush.model.Menu;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Order;
import restaurantrush.model.Restaurant;
import restaurantrush.model.customer.CriticCustomer;
import restaurantrush.model.customer.Customer;
import restaurantrush.model.customer.RegularCustomer;
import restaurantrush.model.customer.VIPCustomer;
import restaurantrush.model.pricing.HappyHour;
import restaurantrush.model.pricing.PriceCalculator;
import restaurantrush.staff.Host;
import restaurantrush.staff.Waiter;

public final class Stage3Checks {
    private Stage3Checks() {
    }

    public static void main(String[] args) {
        System.out.println("Stage 3 checks");
        combos();
        happyHour();
        host();
        Check.finish("Stage3Checks");
    }

    private static void combos() {
        Menu standard = Menu.standard();
        ComboMeal saladPasta = new ComboMeal(standard.get(0), standard.get(2));
        Check.equal("combo: Salad + Pasta = ($9 + $18) x 0.9 = $24.30", 2430, saladPasta.getPriceCents());
        Check.equal("combo: units = 1 + 2 = 3", 3, saladPasta.getPrepUnits());
        Menu menu = Menu.withCombos();
        Check.equal("menu: three dishes then three combos", 6, menu.size());
        Check.equal("combo choice: customer #4 orders the first combo", "Salad + Burger Combo",
                new RegularCustomer(4, 10).chooseDish(menu).getName());
        Check.equal("combo choice: Critic picks the 3-unit, cheaper Salad + Pasta Combo", "Salad + Pasta Combo",
                new CriticCustomer(3, 7).chooseDish(menu).getName());
    }

    private static void happyHour() {
        HappyHour hh = new HappyHour();
        PriceCalculator pricing = new PriceCalculator(hh);
        MenuItem burger = Menu.standard().get(1);
        Customer vip = new VIPCustomer(2, 4);
        Check.equal("before activation: VIP Burger $13.50", 1350, pricing.priceFor(vip, burger, 5));
        hh.activate(5);
        Check.equal("brief example: VIP Burger during Happy Hour = $15 x 0.9 x 0.8 = $10.80", 1080,
                pricing.priceFor(vip, burger, 5));
        Check.that("lasts the activation turn and the next only",
                hh.isActive(5) && hh.isActive(6) && !hh.isActive(7));
        Check.equal("remaining duration shown: 2 then 1", "2/1", hh.turnsRemaining(5) + "/" + hh.turnsRemaining(6));
        Check.that("once per game", !hh.canActivate());

        Restaurant r = new Restaurant(2);
        Customer waiting = new RegularCustomer(1, 1);
        r.admit(waiting);
        for (int i = 0; i < 3; i++) {
            r.applyEndOfTurnPatience();
        }
        r.recoverWaitingCustomers(HappyHour.RECOVERY);
        Check.equal("recovery: waiting customer 76 + 15 = 91", 91, waiting.getSatisfaction());
        r.recoverWaitingCustomers(HappyHour.RECOVERY);
        Check.equal("recovery: capped at 100", 100, waiting.getSatisfaction());

        HappyHour later = new HappyHour();
        PriceCalculator laterPricing = new PriceCalculator(later);
        Restaurant r2 = new Restaurant(2);
        Customer early = new RegularCustomer(1, 1);
        Customer gone = new RegularCustomer(2, 1);
        r2.admit(early);
        r2.admit(gone);
        r2.seatCustomer(1);
        Order placed = new Order(early, burger, laterPricing.priceFor(early, burger, 3), 3);
        early.placeOrder(placed);
        gone.leave();
        later.activate(4);
        Check.equal("order placed before Happy Hour keeps its locked price ($15.00)", 1500, placed.getPriceCents());
        Check.that("Happy Hour does not affect customers who have left",
                !r2.recoverWaitingCustomers(HappyHour.RECOVERY).contains(gone));
    }

    private static void host() {
        Restaurant r = new Restaurant(2);
        for (int id = 1; id <= 3; id++) {
            r.admit(new RegularCustomer(id, 1));
        }
        Host host = new Host();
        Waiter waiter = new Waiter(Menu.withCombos(), new PriceCalculator(new HappyHour()));
        host.startTurn();
        waiter.startTurn();
        Check.that("Host seats one customer", host.seat(r, 1).isSuccess());
        Check.that("Host cannot seat a second customer in the same turn", !host.seat(r, 2).isSuccess());
        Check.that("Waiter can still seat a customer in the same turn", waiter.seat(r, 2).isSuccess());
        Check.that("both tables now occupied", r.findFreeTable().isEmpty());
    }
}
