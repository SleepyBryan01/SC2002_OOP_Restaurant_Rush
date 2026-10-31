package restaurantrush.model.customer;

import restaurantrush.model.MenuItem;
import restaurantrush.util.Money;

public class VIPCustomer extends Customer {
    private static final int LOSS_PER_TURN = 5;

    private static final int PRICE_PERCENT = 90;

    public VIPCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public String getTypeName() {
        return "VIP";
    }

    @Override
    public int getSatisfactionLossPerTurn() {
        return LOSS_PER_TURN;
    }

    @Override
    public int billFor(MenuItem item) {
        return Money.percentOf(super.billFor(item), PRICE_PERCENT);
    }
}
