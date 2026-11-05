package restaurantrush.controller;

import java.util.OptionalDouble;

import restaurantrush.model.Restaurant;

public final class GameResult {
    private final int paidCustomers;

    private final int revenueCents;

    private final int servedCount;
    private final int unhappyDepartures;

    private final OptionalDouble averageSatisfaction;

    private final boolean paidTargetMet;
    private final boolean revenueTargetMet;
    private final boolean satisfactionTargetMet;

    private final GameConfig config;

    private GameResult(GameConfig config, Restaurant restaurant) {
        this.config = config;
        this.paidCustomers = restaurant.getPaidCount();
        this.revenueCents = restaurant.getRevenueCents();
        this.averageSatisfaction = restaurant.getAveragePaidSatisfaction();
        this.servedCount = restaurant.getServedCount();
        this.unhappyDepartures = restaurant.getUnhappyDepartures();
        this.paidTargetMet = paidCustomers >= config.getMinPaidCustomers();
        this.revenueTargetMet = revenueCents >= config.getMinRevenueCents();
        this.satisfactionTargetMet = averageSatisfaction.isPresent()
                && averageSatisfaction.getAsDouble() >= config.getMinAverageSatisfaction();
    }

    public static GameResult evaluate(GameConfig config, Restaurant restaurant) {
        return new GameResult(config, restaurant);
    }

    public boolean isVictory() {
        return paidTargetMet && revenueTargetMet && satisfactionTargetMet;
    }

    public int getPaidCustomers() {
        return paidCustomers;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    public OptionalDouble getAverageSatisfaction() {
        return averageSatisfaction;
    }

    public boolean isPaidTargetMet() {
        return paidTargetMet;
    }

    public boolean isRevenueTargetMet() {
        return revenueTargetMet;
    }

    public boolean isSatisfactionTargetMet() {
        return satisfactionTargetMet;
    }

    public GameConfig getConfig() {
        return config;
    }
}
