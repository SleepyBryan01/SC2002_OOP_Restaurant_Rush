package restaurantrush.model.pricing;

import restaurantrush.util.Money;

public class HappyHour {
    public static final int DURATION = 2;

    public static final int PRICE_PERCENT = 80;

    public static final int RECOVERY = 15;

    private int activatedTurn = -1;

    public boolean canActivate() {
        return activatedTurn < 0;
    }

    public void activate(int turn) {
        if (!canActivate()) {
            throw new IllegalStateException("Happy Hour can only be used once per game");
        }
        activatedTurn = turn;
    }

    public boolean isActive(int turn) {
        return activatedTurn > 0 && turn >= activatedTurn && turn < activatedTurn + DURATION;
    }

    public int turnsRemaining(int turn) {
        return isActive(turn) ? activatedTurn + DURATION - turn : 0;
    }

    public int apply(int cents, int turn) {
        return isActive(turn) ? Money.percentOf(cents, PRICE_PERCENT) : cents;
    }

    public String describe(int turn) {
        if (isActive(turn)) {
            int left = turnsRemaining(turn);
            return "Happy Hour ACTIVE - " + left + " turn" + (left > 1 ? "s" : "") + " remaining (including this one)";
        }
        return canActivate() ? "Happy Hour available (can be used once)" : "Happy Hour already used";
    }
}
