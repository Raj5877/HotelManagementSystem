package com.hotel.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_management";
    private static final String USER = "root"; // Default XAMPP/MySQL user
    private static final String PASSWORD = "mysql_pass_123"; // Default XAMPP/MySQL password

    public static Connection getConnection() {
        try {
            // Register JDBC driver (optional for newer versions, but good practice)
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Failed to connect to the database.");
            e.printStackTrace();
            return null;
        }
    }

    public static void closeConnection() {
        // No longer caching connection globally. Connections are safely closed via try-with-resources in DAOs.
    }
}
