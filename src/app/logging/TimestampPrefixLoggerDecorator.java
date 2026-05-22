package app.logging;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// Design Pattern: Decorator
// Purpose: Wraps a LoggerStrategy to prepend a time-of-day prefix to each message without changing the inner implementation.
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
