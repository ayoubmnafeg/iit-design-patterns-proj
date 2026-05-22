package app.service;

import app.logging.AppLogger;
import app.logging.DatabaseLogger;
import app.logging.FileLogger;
import app.logging.LoggerStrategy;
import app.logging.UILogger;
import app.logging.TimestampPrefixLoggerDecorator;
import app.logging.ConsoleLogger;


public class LoggingService {
    private final AppLogger logger = AppLogger.getInstance();
    private int currentIndex = 0;
    private static final int STRATEGY_COUNT = 4;
    private final LoggerStrategy consoleLogger = new ConsoleLogger();
    private final LoggerStrategy fileLogger = new FileLogger();
    private final LoggerStrategy uiLogger = new UILogger();

    public LoggingService() {
        logger.setStrategy(wrapConsoleStrategy());
        currentIndex = 0;
    }

    public void log(String action) {
        logger.log(action);
    }

    public String cycleStrategy() {
        for (int attempt = 0; attempt < STRATEGY_COUNT; attempt++) {
            currentIndex = (currentIndex + 1) % STRATEGY_COUNT;
            LoggerStrategy nextStrategy = createStrategy(currentIndex);
            if (nextStrategy != null) {
                logger.setStrategy(nextStrategy);
                return logger.getStrategyName();
            }
        }
        logger.setStrategy(wrapConsoleStrategy());
        currentIndex = 0;
        return logger.getStrategyName();
    }

    public boolean setStrategyByIndex(int index) {
        LoggerStrategy next = createStrategy(index);
        if (next == null) return false;
        logger.setStrategy(next);
        currentIndex = index;
        return true;
    }

    public String getCurrentStrategyName() {
        return logger.getStrategyName();
    }

    private LoggerStrategy createStrategy(int index) {
        return switch (index) {
            case 0 -> wrapConsoleStrategy();
            case 1 -> fileLogger;
            case 2 -> createDatabaseLogger();
            case 3 -> uiLogger;
            default -> throw new IllegalArgumentException("Unknown logger index: " + index);
        };
    }


    private LoggerStrategy wrapConsoleStrategy() {
        return new TimestampPrefixLoggerDecorator(consoleLogger);
    }

    private LoggerStrategy createDatabaseLogger() {
        try {
            return new DatabaseLogger();
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
