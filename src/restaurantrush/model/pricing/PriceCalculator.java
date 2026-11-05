package restaurantrush.model.pricing;

import restaurantrush.model.MenuItem;
import restaurantrush.model.customer.Customer;

public class PriceCalculator {
    private final HappyHour happyHour;

    public PriceCalculator(HappyHour happyHour) {
        this.happyHour = happyHour;
    }

    public int priceFor(Customer customer, MenuItem item, int turn) {
        int customerPrice = customer.billFor(item);
        return happyHour.apply(customerPrice, turn);
    }
}
