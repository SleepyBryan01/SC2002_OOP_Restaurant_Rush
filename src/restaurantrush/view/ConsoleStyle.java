package restaurantrush.view;

import java.lang.management.ManagementFactory;

final class ConsoleStyle {
    private static final String ESC = "\u001B[";
    private static final String RESET = ESC + "0m";

    private final boolean enabled;

    ConsoleStyle(boolean enabled) {
        this.enabled = enabled;
    }

    static ConsoleStyle detect(boolean scripted) {
        String force = System.getenv("RR_COLOR");
        if (force != null) {
            return new ConsoleStyle(!force.equals("0"));
        }
        return new ConsoleStyle(!scripted && terminalSupportsColour());
    }

    private static boolean terminalSupportsColour() {
        if (System.getenv("NO_COLOR") != null) {
            return false;
        }
        if (System.getenv("WT_SESSION") != null) {
            return true;
        }
        try {
            if (ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("idea_rt")) {
                return true;
            }
        } catch (RuntimeException | LinkageError ignored) {
        }
        String term = System.getenv("TERM");
        return term != null && !term.equals("dumb");
    }

    String bold(String s) {
        return wrap("1", s);
    }

    String dim(String s) {
        return wrap("2", s);
    }

    String red(String s) {
        return wrap("1;31", s);
    }

    String green(String s) {
        return wrap("32", s);
    }

    String yellow(String s) {
        return wrap("33", s);
    }

    String cyan(String s) {
        return wrap("1;36", s);
    }

    String magenta(String s) {
        return wrap("1;35", s);
    }

    String bySatisfaction(int satisfaction, String s) {
        if (satisfaction >= 60) {
            return green(s);
        }
        return satisfaction >= 30 ? yellow(s) : red(s);
    }

    private String wrap(String code, String s) {
        return enabled ? ESC + code + "m" + s + RESET : s;
    }

    static int visibleLength(String s) {
        return s.replaceAll("\u001B\\[[0-9;]*m", "").length();
    }
}
