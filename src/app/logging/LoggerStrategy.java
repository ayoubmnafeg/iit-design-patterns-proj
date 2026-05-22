package app.logging;



public interface LoggerStrategy {
    void log(String action);
    String getName();
}
