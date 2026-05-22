package app.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;



public class DBConnectionManager {
    private static DBConnectionManager instance;
    private Connection connection;

    private DBConnectionManager() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            Properties props = new Properties();
            props.load(in);
            String url  = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String pass = props.getProperty("db.password");
            connection = DriverManager.getConnection(url, user, pass);
            migrateSchema();
        } catch (IOException | SQLException e) {
            throw new RuntimeException("DB connection failed: " + e.getMessage(), e);
        }
    }

    private void migrateSchema() {
        String[] cols = {
            "ALTER TABLE diagram_elements ADD COLUMN IF NOT EXISTS src_anchor_x DOUBLE DEFAULT 0",
            "ALTER TABLE diagram_elements ADD COLUMN IF NOT EXISTS src_anchor_y DOUBLE DEFAULT 0",
            "ALTER TABLE diagram_elements ADD COLUMN IF NOT EXISTS tgt_anchor_x DOUBLE DEFAULT 0",
            "ALTER TABLE diagram_elements ADD COLUMN IF NOT EXISTS tgt_anchor_y DOUBLE DEFAULT 0"
        };
        try (java.sql.Statement st = connection.createStatement()) {
            for (String sql : cols) {
                try { st.execute(sql); } catch (SQLException ignored) {}
            }
        } catch (SQLException ignored) {}
    }

    public static DBConnectionManager getInstance() {
        if (instance == null) {
            instance = new DBConnectionManager();
        }
        return instance;
    }

    public Connection getConnection() { return connection; }
}
