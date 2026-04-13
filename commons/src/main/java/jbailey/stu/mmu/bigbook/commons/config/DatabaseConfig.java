package jbailey.stu.mmu.bigbook.commons.config;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
public class DatabaseConfig {
    public static final String URL = "jdbc:mysql://10.0.0.10:3306/booksepdb";
    public static final String USER = "epuser";
    public static final String PASSWORD = "dh7f9uri837s!et345";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log.error("MySQL JDBC Driver not found: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
