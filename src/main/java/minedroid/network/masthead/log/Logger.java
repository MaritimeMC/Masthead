package minedroid.network.masthead.log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    public static void info(String msg) {
        System.out.println(" INFO: [" + formatCurrentTime() + "] " + msg);
    }

    public static void empty() {
        info("");
    }

    public static void warning(String msg) {
        System.out.println(" WARN: [" + formatCurrentTime() + "] " + msg);
    }

    public static void severe(String msg) {
        System.out.println(" SEVERE: [" + formatCurrentTime() + "] " + msg);
    }

    public static void fatal(String msg) {
        System.out.println(" FATAL: [" + formatCurrentTime() + "] " + msg);
    }

    private static String formatCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm:ss");
        return LocalDateTime.now().format(dtf);
    }
}
