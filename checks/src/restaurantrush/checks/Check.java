package restaurantrush.checks;

final class Check {
    private static int passed;
    private static int failed;

    private Check() {
    }

    static void that(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }

    static void equal(String name, Object expected, Object actual) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        that(name + (ok ? "" : "  (expected " + expected + ", got " + actual + ")"), ok);
    }

    static void finish(String suite) {
        System.out.println(suite + ": " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
