package restaurantrush.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Menu {
    private final List<MenuItem> items = new ArrayList<>();

    public static Menu standard() {
        Menu menu = new Menu();
        menu.add(new MenuItem("Salad", 900, 1));
        menu.add(new MenuItem("Burger", 1500, 1));
        menu.add(new MenuItem("Pasta", 1800, 2));
        return menu;
    }

    public static Menu withCombos() {
        Menu menu = standard();
        int dishCount = menu.size();
        for (int i = 0; i < dishCount; i++) {
            for (int j = i + 1; j < dishCount; j++) {
                menu.add(new ComboMeal(menu.get(i), menu.get(j)));
            }
        }
        return menu;
    }

    public void add(MenuItem item) {
        items.add(item);
    }

    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public MenuItem get(int index) {
        return items.get(index);
    }

    public int size() {
        return items.size();
    }
}
