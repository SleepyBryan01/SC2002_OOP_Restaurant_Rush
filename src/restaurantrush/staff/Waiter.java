package restaurantrush.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import restaurantrush.enums.CustomerState;
import restaurantrush.model.ActionResult;
import restaurantrush.model.Menu;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Order;
import restaurantrush.model.Restaurant;
import restaurantrush.model.customer.Customer;
import restaurantrush.util.Money;

public class Waiter extends Staff {
    private final Menu menu;

    public Waiter(Menu menu) {
        this.menu = menu;
    }

    @Override
    public String getRole() {
        return "Waiter";
    }

    @Override
    public String describeCommands() {
        return "The Waiter can: seat n, order n, serve n or wait.";
    }

    @Override
    protected ActionResult handle(StaffCommand command, Restaurant restaurant, int turn) {
        switch (command.getAction()) {
            case SEAT:
                return seat(restaurant, command.getTargetId());
            case ORDER:
                return takeOrder(restaurant, command.getTargetId(), turn);
            case SERVE:
                return serve(restaurant, command.getTargetId(), turn);
            default:
                return super.handle(command, restaurant, turn);
        }
    }

    public ActionResult seat(Restaurant restaurant, int customerId) {
        return perform(() -> restaurant.seatCustomer(customerId));
    }

    public ActionResult takeOrder(Restaurant restaurant, int customerId, int turn) {
        return perform(() -> {
            Optional<Customer> found = restaurant.findCustomer(customerId);
            if (found.isEmpty()) {
                return ActionResult.fail("There is no customer #" + customerId + ".");
            }
            Customer customer = found.get();
            if (customer.getState() != CustomerState.SEATED) {
                return ActionResult.fail("Cannot take an order from " + customer.getLabel()
                        + ": the customer must be seated and not have ordered yet (currently " + customer.getState() + ").");
            }
            MenuItem dish = customer.chooseDish(menu);
            int bill = customer.billFor(dish);
            customer.placeOrder(new Order(customer, dish, bill, turn));
            return ActionResult.ok("Customer " + customer.getLabel() + " orders " + dish + ". Bill locked at " + Money.format(bill) + ".");
        });
    }

    public ActionResult serve(Restaurant restaurant, int customerId, int turn) {
        return perform(() -> {
            Optional<Customer> found = restaurant.findCustomer(customerId);
            if (found.isEmpty()) {
                return ActionResult.fail("There is no customer #" + customerId + ".");
            }
            Customer customer = found.get();
            Order order = customer.getOrder();
            if (customer.getState() != CustomerState.ORDERED || order == null) {
                return ActionResult.fail("Customer " + customer.getLabel() + " has no dish waiting to be served (currently " + customer.getState() + ").");
            }
            if (!order.canBeServed(turn)) {
                return ActionResult.fail("Order #" + order.getId() + " is not READY yet (" + order.describeStatus() + ").");
            }
            String reaction = customer.serve(turn);
            restaurant.recordServed();
            return ActionResult.ok("Served " + order.getItem().getName() + " to " + customer.getLabel() + "."
                    + (reaction.isEmpty() ? "" : " " + reaction));
        });
    }

    @Override
    public List<TaskOption> availableTasks(Restaurant restaurant, int turn) {
        List<TaskOption> tasks = new ArrayList<>();
        if (restaurant.findFreeTable().isPresent()) {
            for (Customer c : restaurant.getWaitingQueue()) {
                tasks.add(new TaskOption(new StaffCommand(StaffCommand.Action.SEAT, c.getId()),
                        "Seat " + c.getLabel() + " at a free table"));
            }
        }
        for (Customer c : restaurant.getSeatedCustomers()) {
            if (c.getState() == CustomerState.SEATED) {
                tasks.add(new TaskOption(new StaffCommand(StaffCommand.Action.ORDER, c.getId()),
                        "Take " + c.getLabel() + "'s order"));
            } else if (c.getState() == CustomerState.ORDERED && c.getOrder().canBeServed(turn)) {
                tasks.add(new TaskOption(new StaffCommand(StaffCommand.Action.SERVE, c.getId()),
                        "Serve " + c.getOrder().getItem().getName() + " to " + c.getLabel()));
            }
        }
        tasks.add(TaskOption.waitOption());
        return tasks;
    }
}
