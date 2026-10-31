package restaurantrush.model;

import restaurantrush.util.Money;

public class ComboMeal extends MenuItem {
    private static final int COMBO_PRICE_PERCENT = 90;

    private final MenuItem first;

    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(first.getName() + " + " + second.getName() + " Combo",
                Money.percentOf(first.getPriceCents() + second.getPriceCents(), COMBO_PRICE_PERCENT),
                first.getPrepUnits() + second.getPrepUnits());
        this.first = first;
        this.second = second;
    }

    public MenuItem getFirst() {
        return first;
    }

    public MenuItem getSecond() {
        return second;
    }
}
