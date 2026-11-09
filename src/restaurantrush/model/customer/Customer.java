package restaurantrush.model.customer;

import restaurantrush.enums.CustomerState;
import restaurantrush.model.Menu;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Order;
import restaurantrush.model.Table;

public abstract class Customer {
    public static final int MAX_SATISFACTION = 100;

    private final int id;

    private final int arrivalTurn;

    private int satisfaction = MAX_SATISFACTION;

    private CustomerState state = CustomerState.WAITING;

    private Table table;

    private Order order;

    private int servedTurn = -1;

    private int readyToPayTurn = -1;

    protected Customer(int id, int arrivalTurn) {
        this.id = id;
        this.arrivalTurn = arrivalTurn;
    }

    public abstract String getTypeName();

    public abstract int getSatisfactionLossPerTurn();

    public MenuItem chooseDish(Menu menu) {
        return menu.get((id - 1) % menu.size());
    }

    public int billFor(MenuItem item) {
        return item.getPriceCents();
    }

    protected String reactToService(Order servedOrder, int turn) {
        return "";
    }

    public void seatAt(Table newTable) {
        requireState(CustomerState.WAITING);
        newTable.seat(this);
        this.table = newTable;
        state = CustomerState.SEATED;
    }

    public void placeOrder(Order newOrder) {
        requireState(CustomerState.SEATED);
        this.order = newOrder;
        state = CustomerState.ORDERED;
    }

    public String serve(int turn) {
        requireState(CustomerState.ORDERED);
        order.markServed();
        servedTurn = turn;
        state = CustomerState.EATING;
        return reactToService(order, turn);
    }

    public boolean finishEatingIfDone(int turn) {
        if (state == CustomerState.EATING && servedTurn < turn) {
            state = CustomerState.READY_TO_PAY;
            readyToPayTurn = turn;
            return true;
        }
        return false;
    }

    public boolean canPay(int turn) {
        return state == CustomerState.READY_TO_PAY && readyToPayTurn < turn;
    }

    public void pay() {
        requireState(CustomerState.READY_TO_PAY);
        order.markPaid();
        releaseTable();
        state = CustomerState.PAID;
    }

    public boolean losePatience() {
        if (!state.isAwaitingService()) {
            return false;
        }
        reduceSatisfaction(getSatisfactionLossPerTurn());
        return satisfaction == 0;
    }

    public void leave() {
        if (order != null) {
            order.cancel();
        }
        releaseTable();
        state = CustomerState.LEFT;
    }

    public int turnsUntilLeaving() {
        if (!state.isAwaitingService()) {
            return -1;
        }
        int loss = getSatisfactionLossPerTurn();
        return (satisfaction + loss - 1) / loss;
    }

    public boolean recover(int points) {
        if (!state.isAwaitingService()) {
            return false;
        }
        satisfaction = Math.min(MAX_SATISFACTION, satisfaction + points);
        return true;
    }

    protected void reduceSatisfaction(int points) {
        satisfaction = Math.max(0, satisfaction - points);
    }

    private void releaseTable() {
        if (table != null) {
            table.vacate();
            table = null;
        }
    }

    private void requireState(CustomerState expected) {
        if (state != expected) {
            throw new IllegalStateException("Customer #" + id + " is " + state + ", expected " + expected);
        }
    }

    public int getId() {
        return id;
    }

    public int getArrivalTurn() {
        return arrivalTurn;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerState getState() {
        return state;
    }

    public Table getTable() {
        return table;
    }

    public Order getOrder() {
        return order;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    public String getLabel() {
        return "#" + id + " " + getTypeName();
    }
}
