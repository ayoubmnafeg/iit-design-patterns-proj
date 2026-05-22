package app.logging;



public class ConsoleLogger implements LoggerStrategy {
    @Override
    public void log(String action) {
        System.out.println("[CONSOLE] " + action);
    }

    @Override public String getName() { return "Console"; }
}
