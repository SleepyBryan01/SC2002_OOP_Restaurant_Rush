package restaurantrush.view;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Scanner;

import restaurantrush.controller.GameConfig;
import restaurantrush.controller.GameResult;
import restaurantrush.controller.TurnStep;
import restaurantrush.model.Menu;
import restaurantrush.model.MenuItem;
import restaurantrush.model.Order;
import restaurantrush.model.Restaurant;
import restaurantrush.model.Table;
import restaurantrush.model.customer.Customer;
import restaurantrush.staff.Staff;
import restaurantrush.staff.StaffCommand;
import restaurantrush.staff.TaskOption;
import restaurantrush.util.Money;

public class ConsoleView implements GameView {
    private static final int WIDTH = 78;
    private static final int INNER = WIDTH - 4;
    private static final String DOUBLE_LINE = "=".repeat(WIDTH);
    private static final String BOX_EDGE = "+" + "-".repeat(WIDTH - 2) + "+";
    private static final String BAR_EDGE = "+" + "=".repeat(WIDTH - 2) + "+";

    private static final int AT_RISK_TURNS = 2;
    private static final int BAR_WIDTH = 10;

    private final Scanner in;

    private final PrintStream out;

    private final boolean echoInput;

    private final ConsoleStyle style;

    private boolean inputEnded;

    private GameConfig config;

    private int eventsInStep = -1;

    private TurnStep currentStep;

    private final Map<Integer, Integer> satisfactionAtStart = new HashMap<>();

    public ConsoleView(Scanner in, PrintStream out, boolean echoInput) {
        this.in = in;
        this.out = out;
        this.echoInput = echoInput;
        this.style = ConsoleStyle.detect(echoInput);
    }

    @Override
    public void showWelcome(GameConfig config, Menu menu) {
        this.config = config;
        out.println(BAR_EDGE);
        boxRow("");
        boxRow(centreIn(style.bold("R E S T A U R A N T     R U S H"), INNER));
        boxRow(centreIn(style.dim("a turn-based restaurant management game"), INNER));
        boxRow("");
        out.println(BAR_EDGE);
        out.println(" You are the manager. Staff time is limited, so each turn you decide what");
        out.println(" each member of staff does. Customers choose their own food and the Cashier");
        out.println(" charges customers automatically.");

        heading("GOAL", "checked after turn " + config.getMaxTurns() + "; the game always plays all "
                + config.getMaxTurns() + " turns");
        out.println("   [ ] at least " + config.getMinPaidCustomers() + " paid customers");
        out.println("   [ ] revenue of at least " + Money.format(config.getMinRevenueCents()));
        out.println("   [ ] average satisfaction of paid customers of at least " + config.getMinAverageSatisfaction());

        heading("MENU", "customers choose their own dish");
        out.printf("   %-3s %-30s %9s   %s%n", "#", "Item", "Price", "Prep units");
        int position = 1;
        for (MenuItem item : menu.getItems()) {
            out.printf("   %-3s %-30s %9s   %s%n", position++, item.getName(), Money.format(item.getPriceCents()),
                    "*".repeat(item.getPrepUnits()));
        }

        heading("ARRIVALS", "one customer at the start of each turn below (satisfaction "
                + Customer.MAX_SATISFACTION + ")");
        StringBuilder turns = new StringBuilder("   Turn     ");
        StringBuilder types = new StringBuilder("   Customer ");
        for (int t : config.getArrivals().keySet()) {
            turns.append(String.format("%-9s", t));
            types.append(String.format("%-9s", config.arrivalTypeName(t)));
        }
        out.println(turns.toString().stripTrailing());
        out.println(types.toString().stripTrailing());

        heading("CUSTOMERS", "loss = satisfaction lost per turn while not yet served");
        out.println("   Regular  loss 8    pays full price");

        printTurnGuide();
        printHelp();
    }

    private void printTurnGuide() {
        heading("HOW A TURN WORKS", "");
        out.println("   Step 1    Arrivals: the scheduled customer joins the waiting queue.");
        out.println("   Step 2-3  Your choices: the Waiter does ONE task (seat, take an order or");
        out.println("             serve), then the Chef does ONE preparation unit.");
        out.println("   Step 4    Customers served on an earlier turn finish eating; the Cashier");
        out.println("             charges one READY_TO_PAY customer (oldest first).");
        out.println("   Step 5    Every customer not yet served loses satisfaction. A customer at");
        out.println("             0 leaves without paying and frees the table.");
        out.println("   Timing    A dish finished this turn can be served next turn at the");
        out.println("             earliest; a customer pays the turn after finishing eating.");
    }

    private void printHelp() {
        heading("HOW TO CHOOSE", "n = customer number; an order has its customer's number");
        out.println("   Type the option number shown in the list");
        out.println("   or type a command:");
        printCommandList();
        out.println("   Short forms: s = seat, o = order, v = serve, c = cook, w = wait.");
        out.println("   When 'wait' is the only option, just press Enter.");
        out.println("   Type 'help' at any prompt to see this again.");
    }

    private void printCommandList() {
        out.println("     Waiter     : seat n | order n | serve n | wait");
        out.println("     Chef       : cook n | wait");
    }

    @Override
    public void showTurnHeader(int turn, int maxTurns) {
        out.println();
        out.println();
        out.println(style.cyan(DOUBLE_LINE));
        String left = " TURN " + turn + " of " + maxTurns + "   " + bar(turn, maxTurns, maxTurns);
        String right = turn == maxTurns ? "LAST TURN"
                : (maxTurns - turn) + " turn" + (maxTurns - turn == 1 ? "" : "s") + " left after this";
        int gap = Math.max(1, WIDTH - 1 - left.length() - right.length());
        out.println(style.cyan(left + " ".repeat(gap) + right));
        out.println(style.cyan(DOUBLE_LINE));
        eventsInStep = -1;
    }

    @Override
    public void showStep(TurnStep step) {
        closeStep();
        out.println();
        String title = "-- STEP " + step.getNumber() + "  " + step.getTitle().toUpperCase() + " ";
        out.println(" " + style.cyan(title + "-".repeat(Math.max(0, WIDTH - 1 - title.length()))));
        eventsInStep = 0;
        currentStep = step;
    }

    @Override
    public void showStatus(Restaurant restaurant, int turn, List<Staff> staff) {
        closeStep();
        eventsInStep = -1;
        satisfactionAtStart.clear();
        for (Customer c : restaurant.getCustomers()) {
            satisfactionAtStart.put(c.getId(), c.getSatisfaction());
        }

        out.println();
        out.println(BOX_EDGE);
        boxRow(spread(style.bold("RESTAURANT AT THE START OF TURN " + turn), style.dim(nextArrivalText(turn))));
        out.println(BOX_EDGE);
        printTargets(restaurant);
        out.println(BOX_EDGE);

        boxRow(style.bold("WAITING QUEUE"));
        if (restaurant.getWaitingQueue().isEmpty()) {
            boxRow(style.dim("  (empty)"));
        }
        for (Customer c : restaurant.getWaitingQueue()) {
            boxRow(String.format("  %-12s ", c.getLabel()) + satisfactionBar(c) + "  " + patienceText(c));
        }

        boxRow(style.bold("TABLES"));
        for (Table t : restaurant.getTables()) {
            if (t.isFree()) {
                boxRow("  Table " + t.getNumber() + "   " + style.green("free"));
                continue;
            }
            Customer c = t.getOccupant();
            boxRow("  Table " + t.getNumber() + "   " + String.format("%-12s ", c.getLabel())
                    + satisfactionBar(c) + "  " + c.getState());
            boxRow("            Order: " + orderText(c));
            String patience = patienceText(c);
            if (!patience.isEmpty()) {
                boxRow("            " + patience);
            }
        }
        out.println(BOX_EDGE);

        boxRow(style.bold("STAFF TASKS AVAILABLE"));
        for (Staff s : staff) {
            boxRow(String.format("  %-8s ", s.getRole()) + s.summariseTasks(restaurant, turn));
        }
        out.println(BOX_EDGE);
    }

    private void printTargets(Restaurant restaurant) {
        if (config == null) {
            return;
        }
        int paid = restaurant.getPaidCount();
        int revenue = restaurant.getRevenueCents();
        OptionalDouble avg = restaurant.getAveragePaidSatisfaction();
        boxRow(targetRow("Paid customers", paid + " / " + config.getMinPaidCustomers(),
                bar(paid, config.getMinPaidCustomers(), BAR_WIDTH), paid >= config.getMinPaidCustomers()));
        boxRow(targetRow("Revenue", Money.format(revenue) + " / " + Money.format(config.getMinRevenueCents()),
                bar(revenue, config.getMinRevenueCents(), BAR_WIDTH), revenue >= config.getMinRevenueCents()));
        boolean avgMet = avg.isPresent() && avg.getAsDouble() >= config.getMinAverageSatisfaction();
        boxRow(targetRow("Avg satisfaction (paid)", formatAverage(avg) + " / " + config.getMinAverageSatisfaction(),
                avg.isPresent() ? bar((int) Math.round(avg.getAsDouble()), 100, BAR_WIDTH) : bar(0, 1, BAR_WIDTH),
                avgMet));
    }

    private String targetRow(String name, String value, String bar, boolean met) {
        return String.format("%-24s %-18s ", name, value) + bar + "  "
                + (met ? style.green("target met") : style.yellow("not yet"));
    }

    @Override
    public StaffCommand askCommand(Staff staff, List<TaskOption> options) {
        if (eventsInStep >= 0) {
            eventsInStep++;
        }
        out.println();
        out.println("   " + style.yellow(style.bold(staff.getRole().toUpperCase())) + " - choose one task:");
        int width = 0;
        for (TaskOption option : options) {
            width = Math.max(width, option.getCommand().toCommandText().length());
        }
        int number = 1;
        for (TaskOption option : options) {
            out.println("     " + style.bold(number++ + ")") + " "
                    + String.format("%-" + width + "s", option.getCommand().toCommandText()) + "   "
                    + (option.isWait() ? style.dim(option.getDescription()) : option.getDescription()));
        }
        while (true) {
            out.print("   " + style.bold(staff.getRole() + " > "));
            String line = readLine();
            if (line == null) {
                return StaffCommand.waitCommand();
            }
            if (CommandParser.isHelp(line)) {
                printHelp();
                continue;
            }
            try {
                return CommandParser.parse(line, options);
            } catch (IllegalArgumentException e) {
                printWrapped("   !! ", e.getMessage(), true);
            }
        }
    }

    @Override
    public void showEvent(String message) {
        if (eventsInStep >= 0) {
            eventsInStep++;
        }
        printWrapped("   >> ", message, false);
    }

    @Override
    public void showRejected(Staff staff, String reason) {
        printWrapped("   !! ", "Not allowed: " + reason, true);
        out.println("      " + style.dim("Choose again - the " + staff.getRole() + "'s task has not been used up."));
    }

    @Override
    public void showTurnEnd(Restaurant restaurant, int turn, int maxTurns) {
        closeStep();
        eventsInStep = -1;
        out.println();
        out.println(BOX_EDGE);
        boxRow(spread(style.bold("END OF TURN " + turn + " - RESULTS"), "Turns used " + turn + "/" + maxTurns));
        out.println(BOX_EDGE);
        boxRow("Served " + restaurant.getServedCount()
                + "   Unhappy departures " + restaurant.getUnhappyDepartures()
                + "   Revenue " + Money.format(restaurant.getRevenueCents())
                + "   Avg sat. (paid) " + formatAverage(restaurant.getAveragePaidSatisfaction()));
        boolean any = false;
        for (Customer c : restaurant.getCustomers()) {
            if (c.getState().isAwaitingService() || c.getTable() != null) {
                if (!any) {
                    boxRow(style.bold("Customers still in the restaurant"));
                    any = true;
                }
                boxRow(String.format("  %-12s %-13s ", c.getLabel(), c.getState()) + satisfactionBar(c) + changeText(c));
            }
        }
        if (!any) {
            boxRow(style.dim("Nobody is in the restaurant."));
        }
        out.println(BOX_EDGE);
    }

    @Override
    public void showResult(GameResult result) {
        GameConfig cfg = result.getConfig();
        boolean won = result.isVictory();
        out.println();
        out.println();
        out.println(BAR_EDGE);
        boxRow("");
        String banner = won ? "V I C T O R Y !" : "D E F E A T";
        boxRow(centreIn(won ? style.green(style.bold(banner)) : style.red(banner), INNER));
        boxRow("");
        out.println(BAR_EDGE);
        boxRow(String.format("%-4s %-26s %-10s %-12s", "", "Target", "Result", "Needed"));
        boxRow(resultRow(result.isPaidTargetMet(), "Paid customers", String.valueOf(result.getPaidCustomers()),
                ">= " + cfg.getMinPaidCustomers()));
        boxRow(resultRow(result.isRevenueTargetMet(), "Revenue", Money.format(result.getRevenueCents()),
                ">= " + Money.format(cfg.getMinRevenueCents())));
        boxRow(resultRow(result.isSatisfactionTargetMet(), "Avg satisfaction (paid)",
                formatAverage(result.getAverageSatisfaction()), ">= " + cfg.getMinAverageSatisfaction()));
        out.println(BOX_EDGE);
        boxRow("Dishes served " + result.getServedCount() + "    Unhappy departures " + result.getUnhappyDepartures()
                + "    Turns played " + cfg.getMaxTurns() + "/" + cfg.getMaxTurns());
        boxRow(won ? style.green("All three targets met - well managed!")
                : style.yellow("Not every target was met - see the NOT MET rows above."));
        out.println(BAR_EDGE);
    }

    private String resultRow(boolean met, String target, String value, String needed) {
        String mark = met ? style.green("[x]") : style.red("[ ]");
        String verdict = met ? style.green("MET") : style.red("NOT MET");
        return mark + "  " + String.format("%-26s %-10s %-12s ", target, value, needed) + verdict;
    }

    private String readLine() {
        while (!inputEnded && in.hasNextLine()) {
            String line = in.nextLine();
            if (line.trim().startsWith("#")) {
                continue;
            }
            if (echoInput) {
                out.println(line);
            }
            return line;
        }
        if (!inputEnded) {
            inputEnded = true;
            out.println();
            out.println("   (input ended - all remaining choices default to 'wait')");
        }
        return null;
    }

    private void heading(String title, String note) {
        out.println();
        out.println(" " + style.cyan(title) + (note.isEmpty() ? "" : style.dim("  (" + note + ")")));
    }

    private void boxRow(String content) {
        int pad = INNER - ConsoleStyle.visibleLength(content);
        out.println(pad >= 0 ? "| " + content + " ".repeat(pad) + " |" : "| " + content);
    }

    private static String spread(String left, String right) {
        int gap = INNER - ConsoleStyle.visibleLength(left) - ConsoleStyle.visibleLength(right);
        return left + " ".repeat(Math.max(1, gap)) + right;
    }

    private static String centreIn(String text, int width) {
        return " ".repeat(Math.max(0, (width - ConsoleStyle.visibleLength(text)) / 2)) + text;
    }

    private static String bar(int value, int max, int width) {
        int filled = max <= 0 ? 0 : Math.min(width, (int) Math.round((double) value * width / max));
        filled = Math.max(0, filled);
        return "[" + "#".repeat(filled) + ".".repeat(width - filled) + "]";
    }

    private String satisfactionBar(Customer c) {
        int s = c.getSatisfaction();
        return style.bySatisfaction(s, bar(s, Customer.MAX_SATISFACTION, BAR_WIDTH) + String.format(" %3d", s));
    }

    private void printWrapped(String prefix, String message, boolean warning) {
        String indent = " ".repeat(prefix.length());
        StringBuilder line = new StringBuilder(prefix);
        boolean first = true;
        for (String word : message.split(" ")) {
            if (line.length() + word.length() + 1 > WIDTH && line.length() > prefix.length()) {
                out.println(colourEvent(line.toString().stripTrailing(), warning, first));
                first = false;
                line = new StringBuilder(indent);
            }
            line.append(word).append(' ');
        }
        out.println(colourEvent(line.toString().stripTrailing(), warning, first));
    }

    private String colourEvent(String text, boolean warning, boolean firstLine) {
        if (warning) {
            return style.red(text);
        }
        return firstLine ? style.green(text.substring(0, 6)) + text.substring(6) : text;
    }

    private void closeStep() {
        if (eventsInStep == 0 && currentStep != null) {
            out.println("   " + style.dim(quietStepText(currentStep)));
        }
        eventsInStep = -1;
        currentStep = null;
    }

    private static String quietStepText(TurnStep step) {
        switch (step) {
            case ARRIVALS:
                return "(no customer arrives this turn)";
            case EATING_AND_PAYMENT:
                return "(nobody finished eating or paid this turn)";
            case WAITING_AND_ABANDONMENT:
                return "(nobody walked out)";
            default:
                return "(nothing happened in this step)";
        }
    }

    private String nextArrivalText(int turn) {
        int next = config == null ? -1 : config.nextArrivalAfter(turn);
        return next < 0 ? "No more arrivals"
                : "Next arrival: turn " + next + " (" + config.arrivalTypeName(next) + ")";
    }

    private String patienceText(Customer c) {
        int left = c.turnsUntilLeaving();
        if (left < 0) {
            return "";
        }
        if (left == 1) {
            return style.red("!! LEAVES at the end of this turn if not served");
        }
        String text = "leaves in " + left + " turns if not served";
        return left <= AT_RISK_TURNS ? style.red("!! " + text) : style.dim(text);
    }

    private static String orderText(Customer c) {
        Order o = c.getOrder();
        if (o == null) {
            return "no order yet";
        }
        return o.getItem().getName() + " " + Money.format(o.getPriceCents()) + " [" + o.describeStatus() + "]";
    }

    private String changeText(Customer c) {
        Integer before = satisfactionAtStart.get(c.getId());
        if (before == null || before == c.getSatisfaction()) {
            return "";
        }
        int change = c.getSatisfaction() - before;
        String text = "  (" + (change > 0 ? "+" : "") + change + " this turn)";
        return change > 0 ? style.green(text) : style.red(text);
    }

    private static String formatAverage(OptionalDouble avg) {
        return avg.isPresent() ? String.format("%.1f", avg.getAsDouble()) : "N/A";
    }
}
