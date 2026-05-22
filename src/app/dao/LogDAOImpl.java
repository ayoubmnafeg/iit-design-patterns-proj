package app.dao;

import app.config.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LogDAOImpl implements LogDAO {
    private final Connection conn = DBConnectionManager.getInstance().getConnection();

    @Override
    public void insertLog(String actionText) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO logs (action_text) VALUES (?)");
            ps.setString(1, actionText);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("LogDAO error: " + e.getMessage());
        }
    }
}
