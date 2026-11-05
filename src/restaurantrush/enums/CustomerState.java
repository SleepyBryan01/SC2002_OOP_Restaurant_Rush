package restaurantrush.enums;

public enum CustomerState {
    WAITING,
    SEATED,
    ORDERED,
    EATING,
    READY_TO_PAY,
    PAID,
    LEFT;

    public boolean isAwaitingService() {
        return this == WAITING || this == SEATED || this == ORDERED;
    }
}
