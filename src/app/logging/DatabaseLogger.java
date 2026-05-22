package app.logging;

import app.dao.LogDAO;
import app.dao.LogDAOImpl;



public class DatabaseLogger implements LoggerStrategy {
    private final LogDAO logDAO = new LogDAOImpl();

    @Override
    public void log(String action) {
        logDAO.insertLog(action);
    }

    @Override public String getName() { return "Database"; }
}
