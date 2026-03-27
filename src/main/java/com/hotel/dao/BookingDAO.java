package com.hotel.dao;

import com.hotel.model.Booking;
import com.hotel.util.DBConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public static void addBooking(Booking booking) throws SQLException {
        // We use boolean flag trick: if it's already booked, it throws or updates room status.
        // In a real application we'd use transactions.
        Connection conn = DBConnection.getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        
        try {
            conn.setAutoCommit(false);
            
            // 1. Insert into bookings
            String query = "INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, booking.getCustomerId());
                pstmt.setInt(2, booking.getRoomId());
                pstmt.setDate(3, Date.valueOf(booking.getCheckInDate()));
                pstmt.setDate(4, Date.valueOf(booking.getCheckOutDate()));
                pstmt.executeUpdate();
            }

            // 2. Update room availability
            String updateRoomQuery = "UPDATE rooms SET is_available = FALSE WHERE room_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateRoomQuery)) {
                updateStmt.setInt(1, booking.getRoomId());
                updateStmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    public static List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        // Includes joins to fetch customer name and room type for display
        String query = "SELECT b.*, c.name, r.room_type FROM bookings b " +
                       "JOIN customers c ON b.customer_id = c.customer_id " +
                       "JOIN rooms r ON b.room_id = r.room_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Date actualOutDateSql = rs.getDate("actual_check_out_date");
                LocalDate actualOutDate = (actualOutDateSql != null) ? actualOutDateSql.toLocalDate() : null;

                Booking b = new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("room_id"),
                        rs.getDate("check_in_date").toLocalDate(),
                        rs.getDate("check_out_date").toLocalDate(),
                        actualOutDate
                );
                b.setCustomerName(rs.getString("name"));
                b.setRoomInfo("Room " + b.getRoomId() + " (" + rs.getString("room_type") + ")");
                bookings.add(b);
            }
        }
        return bookings;
    }

    public static void checkoutBooking(int bookingId, int roomId, LocalDate actualCheckOutDate) throws SQLException {
        Connection conn = DBConnection.getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        
        try {
            conn.setAutoCommit(false);

            // 1. Update actual check-out date
            String query = "UPDATE bookings SET actual_check_out_date = ? WHERE booking_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setDate(1, Date.valueOf(actualCheckOutDate));
                pstmt.setInt(2, bookingId);
                pstmt.executeUpdate();
            }

            // 2. Set room to available
            String updateRoomQuery = "UPDATE rooms SET is_available = TRUE WHERE room_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateRoomQuery)) {
                pstmt.setInt(1, roomId);
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }
}
