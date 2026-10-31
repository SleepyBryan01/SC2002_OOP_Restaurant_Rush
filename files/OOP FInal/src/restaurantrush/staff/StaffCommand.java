package restaurantrush.staff;

public final class StaffCommand {
    public enum Action {
        SEAT("seat"),
        ORDER("order"),
        SERVE("serve"),
        COOK("cook"),
        WAIT("wait");

        private final String keyword;

        Action(String keyword) {
            this.keyword = keyword;
        }

        public String getKeyword() {
            return keyword;
        }
    }

    private final Action action;

    private final int targetId;

    public StaffCommand(Action action, int targetId) {
        this.action = action;
        this.targetId = targetId;
    }

    public static StaffCommand waitCommand() {
        return new StaffCommand(Action.WAIT, 0);
    }

    public Action getAction() {
        return action;
    }

    public int getTargetId() {
        return targetId;
    }

    public String toCommandText() {
        return action == Action.WAIT ? action.getKeyword() : action.getKeyword() + " " + targetId;
    }

    @Override
    public String toString() {
        return toCommandText();
    }
}
