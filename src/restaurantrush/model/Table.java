package restaurantrush.model;

import restaurantrush.model.customer.Customer;

public class Table {
    private final int number;

    private Customer occupant;

    public Table(int number) {
        this.number = number;
    }

    public void seat(Customer customer) {
        if (occupant != null) {
            throw new IllegalStateException("Table " + number + " is occupied");
        }
        occupant = customer;
    }

    public void vacate() {
        occupant = null;
    }

    public boolean isFree() {
        return occupant == null;
    }

    public int getNumber() {
        return number;
    }

    public Customer getOccupant() {
        return occupant;
    }
}
