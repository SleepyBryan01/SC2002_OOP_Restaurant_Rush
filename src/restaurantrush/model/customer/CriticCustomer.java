package restaurantrush.model.customer;

import java.util.Comparator;

import restaurantrush.model.Menu;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Order;

public class CriticCustomer extends Customer {
    private static final int LOSS_PER_TURN = 12;

    public static final int COLD_FOOD_PENALTY = 20;

    public CriticCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getTypeName() {
        return "Critic";
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return LOSS_PER_TURN;
    }

    @Override
    public MenuItem chooseDish(Menu menu) {
        return menu.getItems().stream()
                .max(Comparator.comparingInt(MenuItem::getPrepUnits)
                        .thenComparing(Comparator.comparingInt(MenuItem::getPriceCents).reversed()))
                .orElseThrow();
    }

    @Override
    protected String reactToService(Order servedOrder, int turn) {
        int turnsLeftWaiting = turn - servedOrder.getReadyTurn();
        if (turnsLeftWaiting > 1) {
            reduceSatisfaction(COLD_FOOD_PENALTY);
            return "The Critic complains the " + servedOrder.getItem().getName() + " went cold: -"
                    + COLD_FOOD_PENALTY + " satisfaction (now " + getSatisfaction() + ").";
        }
        return "The Critic approves: the dish arrived hot.";
    }
}
