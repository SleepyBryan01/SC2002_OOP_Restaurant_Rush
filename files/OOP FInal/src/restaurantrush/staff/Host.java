package restaurantrush.staff;

import java.util.ArrayList;
import java.util.List;

import restaurantrush.model.ActionResult;
import restaurantrush.model.Restaurant;
import restaurantrush.model.customer.Customer;

public class Host extends Staff {
    @Override
    public String getRole() {
        return "Host";
    }

    @Override
    public String describeCommands() {
        return "The Host can: seat n or wait.";
    }

    @Override
    protected ActionResult handle(StaffCommand command, Restaurant restaurant, int turn) {
        if (command.getAction() == StaffCommand.Action.SEAT) {
            return seat(restaurant, command.getTargetId());
        }
        return super.handle(command, restaurant, turn);
    }

    public ActionResult seat(Restaurant restaurant, int customerId) {
        return perform(() -> restaurant.seatCustomer(customerId));
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
        tasks.add(TaskOption.waitOption());
        return tasks;
    }
}
