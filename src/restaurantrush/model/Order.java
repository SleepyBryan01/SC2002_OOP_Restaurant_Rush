package restaurantrush.model;

import restaurantrush.enums.OrderStatus;
import restaurantrush.model.customer.Customer;

public class Order {
    private final Customer customer;

    private final MenuItem item;

    private final int priceCents;

    private final int takenTurn;

    private int unitsDone;

    private OrderStatus status = OrderStatus.PREPARING;

    private int readyTurn = -1;

    public Order(Customer customer, MenuItem item, int priceCents, int takenTurn) {
        this.customer = customer;
        this.item = item;
        this.priceCents = priceCents;
        this.takenTurn = takenTurn;
    }

    public boolean prepareOneUnit(int turn) {
        if (status != OrderStatus.PREPARING) {
            throw new IllegalStateException("Order is not being prepared");
        }
        unitsDone++;
        if (unitsDone == item.getPrepUnits()) {
            status = OrderStatus.READY;
            readyTurn = turn;
            return true;
        }
        return false;
    }

    public boolean canBeServed(int turn) {
        return status == OrderStatus.READY && readyTurn < turn;
    }

    public void markServed() {
        if (status != OrderStatus.READY) {
            throw new IllegalStateException("Only READY orders can be served");
        }
        status = OrderStatus.SERVED;
    }

    public void markPaid() {
        if (status != OrderStatus.SERVED) {
            throw new IllegalStateException("Only SERVED orders can be paid");
        }
        status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status == OrderStatus.PREPARING || status == OrderStatus.READY) {
            status = OrderStatus.CANCELLED;
        }
    }

    public int getId() {
        return customer.getId();
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getTakenTurn() {
        return takenTurn;
    }

    public int getUnitsDone() {
        return unitsDone;
    }

    public int getReadyTurn() {
        return readyTurn;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String describeStatus() {
        if (status == OrderStatus.PREPARING) {
            return status + " " + unitsDone + "/" + item.getPrepUnits();
        }
        return status.toString();
    }
}
