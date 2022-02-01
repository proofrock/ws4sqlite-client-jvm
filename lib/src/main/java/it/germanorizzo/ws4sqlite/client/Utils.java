package it.germanorizzo.ws4sqlite.client;

/**
 * Internal utility class
 */
public class Utils {
    private Utils() {
    }

    static void check(boolean toCheck, String err) {
        if (!toCheck)
            throw new IllegalArgumentException(err);
    }

    static void require(boolean toCheck, String err) {
        if (!toCheck)
            throw new IllegalStateException(err);
    }
}
