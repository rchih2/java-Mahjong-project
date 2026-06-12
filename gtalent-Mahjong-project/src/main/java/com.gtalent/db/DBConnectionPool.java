package com.gtalent.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnectionPool {
    private static DBConnectionPool instance;
    private Properties properties;

    private DBConnectionPool() {
        loadProperties();
    }

    public static synchronized DBConnectionPool getInstance() {
        if (instance == null) {
            instance = new DBConnectionPool();
        }
        return instance;
    }

    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (input == null) {
                System.out.println("無法找到 db.properties 檔案");
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        String driver = properties.getProperty("db.driver");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("無法載入資料庫驅動", e);
        }

        return DriverManager.getConnection(url, username, password);
    }

    public void returnConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
