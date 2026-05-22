package app.logging;

// Design Pattern: Strategy
// Purpose: Logger implementation that writes to standard output.
public class ConsoleLogger implements LoggerStrategy {
    @Override
    public void log(String action) {
        System.out.println("[CONSOLE] " + action);
    }

    @Override public String getName() { return "Console"; }
}
