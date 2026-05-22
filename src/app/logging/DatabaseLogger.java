package app.logging;

import app.dao.LogDAO;
import app.dao.LogDAOImpl;

// Design Pattern: Strategy
// Purpose: Logger implementation that persists log lines to the database.
public class DatabaseLogger implements LoggerStrategy {
    private final LogDAO logDAO = new LogDAOImpl();

    @Override
    public void log(String action) {
        logDAO.insertLog(action);
    }

    @Override public String getName() { return "Database"; }
}
