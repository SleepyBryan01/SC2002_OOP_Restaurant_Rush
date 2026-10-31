package restaurantrush.staff;

import java.util.List;
import java.util.Optional;

import restaurantrush.model.ActionResult;
import restaurantrush.model.Restaurant;
import restaurantrush.model.customer.Customer;
import restaurantrush.util.Money;

public class Cashier extends Staff {
    @Override
    public String getRole() {
        return "Cashier";
    }

    @Override
    public String describeCommands() {
        return "The Cashier works automatically.";
    }

    public Optional<ActionResult> collectNextPayment(Restaurant restaurant, int turn) {
        Optional<Customer> next = restaurant.nextCustomerToPay(turn);
        if (next.isEmpty()) {
            return Optional.empty();
        }
        Customer customer = next.get();
        return Optional.of(perform(() -> {
            int bill = restaurant.collectPayment(customer);
            return ActionResult.ok("Cashier collected " + Money.format(bill) + " from " + customer.getLabel()
                    + ". PAID with satisfaction " + customer.getSatisfaction() + "; table freed.");
        }));
    }

    @Override
    public List<TaskOption> availableTasks(Restaurant restaurant, int turn) {
        return List.of();
    }

    @Override
    public String summariseTasks(Restaurant restaurant, int turn) {
        return restaurant.nextCustomerToPay(turn)
                .map(c -> "(automatic) will charge " + c.getLabel())
                .orElse("(automatic) nobody ready to pay");
    }
}
