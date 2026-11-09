package restaurantrush.controller;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import restaurantrush.model.customer.RegularCustomer;

public final class GameConfig {
    private final int maxTurns;

    private final int tableCount;

    private final NavigableMap<Integer, CustomerFactory> arrivals;

    private final int minPaidCustomers;

    private final int minRevenueCents;

    private final int minAverageSatisfaction;

    private GameConfig(int maxTurns, int tableCount, Map<Integer, CustomerFactory> arrivals,
                       int minPaidCustomers, int minRevenueCents, int minAverageSatisfaction) {
        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.arrivals = Collections.unmodifiableNavigableMap(new TreeMap<>(arrivals));
        this.minPaidCustomers = minPaidCustomers;
        this.minRevenueCents = minRevenueCents;
        this.minAverageSatisfaction = minAverageSatisfaction;
    }

    public static GameConfig baseSetting() {
        Map<Integer, CustomerFactory> arrivals = new TreeMap<>();
        arrivals.put(1, RegularCustomer::new);
        arrivals.put(4, RegularCustomer::new);
        arrivals.put(7, RegularCustomer::new);
        arrivals.put(10, RegularCustomer::new);
        arrivals.put(13, RegularCustomer::new);
        arrivals.put(16, RegularCustomer::new);
        return new GameConfig(18, 2, arrivals, 4, 4500, 60);
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getTableCount() {
        return tableCount;
    }

    public Map<Integer, CustomerFactory> getArrivals() {
        return arrivals;
    }

    public String arrivalTypeName(int turn) {
        CustomerFactory factory = arrivals.get(turn);
        return factory == null ? "" : factory.create(0, turn).getTypeName();
    }

    public int nextArrivalAfter(int turn) {
        Integer next = arrivals.higherKey(turn);
        return next == null ? -1 : next;
    }

    public int getMinPaidCustomers() {
        return minPaidCustomers;
    }

    public int getMinRevenueCents() {
        return minRevenueCents;
    }

    public int getMinAverageSatisfaction() {
        return minAverageSatisfaction;
    }
}
