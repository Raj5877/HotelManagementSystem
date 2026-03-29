package com.hotel.dao;

import com.hotel.model.Customer;
import com.hotel.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public static void addCustomer(Customer customer) throws SQLException {
        String query = "INSERT INTO customers (name, contact, email, id_proof) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getContact());
            pstmt.setString(3, customer.getEmail() != null ? customer.getEmail() : "");
            pstmt.setString(4, customer.getIdProof());
            pstmt.executeUpdate();
        }
    }

    public static List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers ORDER BY customer_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String idProof = safeGet(rs, "id_proof");
                String idType = "", idNumber = "";
                if (idProof != null && idProof.contains("|")) {
                    String[] parts = idProof.split("\\|", 2);
                    idType = parts[0];
                    idNumber = parts[1];
                } else {
                    idNumber = idProof != null ? idProof : "";
                }
                Customer c = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("contact"),
                        safeGet(rs, "email"),
                        idType,
                        idNumber
                );
                customers.add(c);
            }
        }
        return customers;
    }

    private static String safeGet(ResultSet rs, String col) {
        try { return rs.getString(col); } catch (SQLException e) { return ""; }
    }
}
