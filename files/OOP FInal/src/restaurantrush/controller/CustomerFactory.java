package restaurantrush.controller;

import restaurantrush.model.customer.Customer;

@FunctionalInterface
public interface CustomerFactory {
    Customer create(int id, int arrivalTurn);
}
