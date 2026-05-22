package app.logging;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// Design Pattern: Singleton
// Purpose: One shared application logger, strategy-backed sink, and observable log entries for the log console.
public class AppLogger {
    private volatile LoggerStrategy strategy = new ConsoleLogger();
    private final ObservableList<String> entries = FXCollections.observableArrayList();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private AppLogger() {}

    private static final class Holder {
        static final AppLogger INSTANCE = new AppLogger();
    }

    public static AppLogger getInstance() {
        return Holder.INSTANCE;
    }

    public void setStrategy(LoggerStrategy strategy) {
        this.strategy = strategy;
    }

    public void log(String action) {
        String name = strategy.getName();
        strategy.log(action);
        String entry = "[" + LocalTime.now().format(TIME_FMT) + "] [" + name + "] " + action;
        if (javafx.application.Platform.isFxApplicationThread()) {
            entries.add(entry);
        } else {
            javafx.application.Platform.runLater(() -> entries.add(entry));
        }
    }

    public String getStrategyName() {
        return strategy.getName();
    }

    public ObservableList<String> getEntries() {
        return entries;
    }
}
