package app.logging;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;



public class TimestampPrefixLoggerDecorator implements LoggerStrategy {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final LoggerStrategy inner;

    public TimestampPrefixLoggerDecorator(LoggerStrategy inner) {
        this.inner = inner;
    }

    @Override
    public void log(String action) {
        inner.log("[" + LocalTime.now().format(FMT) + "] " + action);
    }

    @Override
    public String getName() {
        return inner.getName() + " +ts";
    }
}
