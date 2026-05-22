package app.logging;

// Design Pattern: Strategy
// Purpose: Interchangeable logging behavior selected at runtime (console, file, database, UI, or decorated variants).
public interface LoggerStrategy {
    void log(String action);
    String getName();
}
