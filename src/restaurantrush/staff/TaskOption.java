package restaurantrush.staff;

public final class TaskOption {
    private final StaffCommand command;

    private final String description;

    public TaskOption(StaffCommand command, String description) {
        this.command = command;
        this.description = description;
    }

    public static TaskOption waitOption() {
        return new TaskOption(StaffCommand.waitCommand(), "Do nothing this turn");
    }

    public StaffCommand getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public boolean isWait() {
        return command.getAction() == StaffCommand.Action.WAIT;
    }
}
