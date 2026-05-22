package app.logging;

// Design Pattern: Strategy
// Purpose: Selectable logging that relies on the in-app log list only (no file/console/database sink in this strategy).
public class UILogger implements LoggerStrategy {
    @Override
    public void log(String action) {
        // Side sinks are disabled; AppLogger still records the line in the observable log list.
    }

    @Override
    public String getName() {
        return "UI";
    }
}
