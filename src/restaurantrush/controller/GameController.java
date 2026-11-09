package restaurantrush.controller;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.model.ActionResult;
import restaurantrush.model.Menu;
import restaurantrush.model.Restaurant;
import restaurantrush.model.customer.Customer;
import restaurantrush.staff.Cashier;
import restaurantrush.staff.Chef;
import restaurantrush.staff.Staff;
import restaurantrush.staff.StaffCommand;
import restaurantrush.staff.Waiter;
import restaurantrush.view.GameView;

public class GameController {
    private final GameConfig config;

    private final Restaurant restaurant;

    private final Menu menu;

    private final List<Staff> directedStaff;

    private final Cashier cashier;

    private final GameView view;

    private int nextCustomerId = 1;

    public GameController(GameConfig config, GameView view) {
        this.config = config;
        this.view = view;
        this.restaurant = new Restaurant(config.getTableCount());
        this.menu = Menu.standard();
        this.directedStaff = List.of(new Waiter(menu), new Chef());
        this.cashier = new Cashier();
    }

    public GameResult play() {
        view.showWelcome(config, menu);
        for (int turn = 1; turn <= config.getMaxTurns(); turn++) {
            playTurn(turn);
        }
        GameResult result = GameResult.evaluate(config, restaurant);
        view.showResult(result);
        return result;
    }

    private void playTurn(int turn) {
        view.showTurnHeader(turn, config.getMaxTurns());
        for (Staff s : allStaff()) {
            s.startTurn();
        }

        view.showStep(TurnStep.ARRIVALS);
        CustomerFactory arriving = config.getArrivals().get(turn);
        if (arriving != null) {
            Customer customer = arriving.create(nextCustomerId++, turn);
            restaurant.admit(customer);
            view.showEvent("Customer " + customer.getLabel() + " arrives and joins the queue (satisfaction "
                    + customer.getSatisfaction() + ").");
        }
        view.showStatus(restaurant, turn, allStaff());

        view.showStep(TurnStep.MANAGER_CHOICES);
        for (Staff s : directedStaff) {
            runStaffChoice(s, turn);
        }

        view.showStep(TurnStep.EATING_AND_PAYMENT);
        cashier.collectNextPayment(restaurant, turn).ifPresent(r -> view.showEvent(r.getMessage()));
        for (Customer c : restaurant.finishEating(turn)) {
            view.showEvent("Customer " + c.getLabel() + " finished eating and is READY_TO_PAY (pays next turn at the earliest).");
        }

        view.showStep(TurnStep.WAITING_AND_ABANDONMENT);
        for (Customer c : restaurant.applyEndOfTurnPatience()) {
            view.showEvent("Customer " + c.getLabel() + " ran out of patience and LEFT without paying.");
        }
        view.showTurnEnd(restaurant, turn, config.getMaxTurns());
    }

    private void runStaffChoice(Staff staff, int turn) {
        while (!staff.hasActed()) {
            StaffCommand cmd = view.askCommand(staff, staff.availableTasks(restaurant, turn));
            ActionResult result = staff.carryOut(cmd, restaurant, turn);
            if (result.isSuccess()) {
                view.showEvent(result.getMessage());
            } else {
                view.showRejected(staff, result.getMessage());
            }
        }
    }

    private List<Staff> allStaff() {
        List<Staff> all = new ArrayList<>(directedStaff);
        all.add(cashier);
        return all;
    }
}
