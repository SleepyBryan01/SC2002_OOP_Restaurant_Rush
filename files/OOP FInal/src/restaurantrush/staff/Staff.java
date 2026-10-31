package restaurantrush.staff;

import java.util.List;
import java.util.function.Supplier;

import restaurantrush.model.ActionResult;
import restaurantrush.model.Restaurant;

public abstract class Staff {
    private boolean taskUsed;

    public abstract String getRole();

    public abstract List<TaskOption> availableTasks(Restaurant restaurant, int turn);

    public String summariseTasks(Restaurant restaurant, int turn) {
        StringBuilder sb = new StringBuilder();
        for (TaskOption option : availableTasks(restaurant, turn)) {
            sb.append(sb.length() == 0 ? "" : " | ").append(option.getCommand().toCommandText());
        }
        return sb.toString();
    }

    public void startTurn() {
        taskUsed = false;
    }

    public boolean hasActed() {
        return taskUsed;
    }

    public final ActionResult carryOut(StaffCommand command, Restaurant restaurant, int turn) {
        if (command.getAction() == StaffCommand.Action.WAIT) {
            return waitTurn();
        }
        return handle(command, restaurant, turn);
    }

    protected ActionResult handle(StaffCommand command, Restaurant restaurant, int turn) {
        return ActionResult.fail("The " + getRole() + " cannot '" + command.getAction().getKeyword()
                + "'. " + describeCommands());
    }

    public abstract String describeCommands();

    public ActionResult waitTurn() {
        return perform(() -> ActionResult.ok(getRole() + " waits this turn."));
    }

    protected ActionResult perform(Supplier<ActionResult> task) {
        if (taskUsed) {
            return ActionResult.fail(getRole() + " has already completed a task this turn.");
        }
        ActionResult result = task.get();
        if (result.isSuccess()) {
            taskUsed = true;
        }
        return result;
    }
}
