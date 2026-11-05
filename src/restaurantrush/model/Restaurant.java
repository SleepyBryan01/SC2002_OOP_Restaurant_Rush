package restaurantrush.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import restaurantrush.enums.CustomerState;
import restaurantrush.model.customer.Customer;

public class Restaurant {
    private final List<Table> tables = new ArrayList<>();

    private final List<Customer> waitingQueue = new ArrayList<>();

    private final List<Customer> customers = new ArrayList<>();

    private final List<Integer> paidSatisfactions = new ArrayList<>();

    private int revenueCents;

    private int servedCount;

    private int unhappyDepartures;

    public Restaurant(int tableCount) {
        for (int i = 1; i <= tableCount; i++) {
            tables.add(new Table(i));
        }
    }

    public void admit(Customer customer) {
        customers.add(customer);
        waitingQueue.add(customer);
    }

    public Optional<Customer> findCustomer(int id) {
        return customers.stream().filter(c -> c.getId() == id).findFirst();
    }

    public Optional<Table> findFreeTable() {
        return tables.stream().filter(Table::isFree).findFirst();
    }

    public ActionResult seatCustomer(int customerId) {
        Optional<Customer> found = findCustomer(customerId);
        if (found.isEmpty()) {
            return ActionResult.fail("There is no customer #" + customerId + ".");
        }
        Customer customer = found.get();
        if (customer.getState() != CustomerState.WAITING) {
            return ActionResult.fail("Customer " + customer.getLabel() + " is not in the waiting queue (" + customer.getState() + ").");
        }
        Optional<Table> table = findFreeTable();
        if (table.isEmpty()) {
            return ActionResult.fail("No free table: every table is occupied until its customer pays or leaves.");
        }
        customer.seatAt(table.get());
        waitingQueue.remove(customer);
        return ActionResult.ok("Customer " + customer.getLabel() + " is seated at Table " + table.get().getNumber() + ".");
    }

    public void recordServed() {
        servedCount++;
    }

    public List<Customer> finishEating(int turn) {
        List<Customer> finished = new ArrayList<>();
        for (Customer c : customers) {
            if (c.finishEatingIfDone(turn)) {
                finished.add(c);
            }
        }
        return finished;
    }

    public Optional<Customer> nextCustomerToPay(int turn) {
        return customers.stream()
                .filter(c -> c.canPay(turn))
                .min(Comparator.comparingInt(Customer::getReadyToPayTurn).thenComparingInt(Customer::getId));
    }

    public int collectPayment(Customer customer) {
        int bill = customer.getOrder().getPriceCents();
        customer.pay();
        revenueCents += bill;
        paidSatisfactions.add(customer.getSatisfaction());
        return bill;
    }

    public List<Customer> applyEndOfTurnPatience() {
        List<Customer> leaving = new ArrayList<>();
        for (Customer c : customers) {
            if (c.losePatience()) {
                leaving.add(c);
            }
        }
        for (Customer c : leaving) {
            c.leave();
            waitingQueue.remove(c);
            unhappyDepartures++;
        }
        return leaving;
    }

    public List<Customer> recoverWaitingCustomers(int points) {
        List<Customer> recovered = new ArrayList<>();
        for (Customer c : customers) {
            if (c.recover(points)) {
                recovered.add(c);
            }
        }
        return recovered;
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public List<Customer> getWaitingQueue() {
        return Collections.unmodifiableList(waitingQueue);
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }

    public List<Customer> getSeatedCustomers() {
        List<Customer> seated = new ArrayList<>();
        for (Table t : tables) {
            if (!t.isFree()) {
                seated.add(t.getOccupant());
            }
        }
        return seated;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return paidSatisfactions.size();
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    public OptionalDouble getAveragePaidSatisfaction() {
        return paidSatisfactions.stream().mapToInt(Integer::intValue).average();
    }
}
