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
        Connection conn = DBConnection.getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        
        try {
            conn.setAutoCommit(false);
            
            String query = "INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date, original_check_out_date) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, booking.getCustomerId());
                pstmt.setInt(2, booking.getRoomId());
                pstmt.setDate(3, Date.valueOf(booking.getCheckInDate()));
                pstmt.setDate(4, Date.valueOf(booking.getCheckOutDate()));
                pstmt.setDate(5, Date.valueOf(booking.getCheckOutDate())); // original, never changed
                pstmt.executeUpdate();
            }

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
            conn.close(); // FIXED LEAK
        }
    }

    public static List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        // Modified query to gracefully skip if customer or room was deleted
        String query = "SELECT b.*, c.name, r.type_id, rt.type_name FROM bookings b " +
                       "JOIN customers c ON b.customer_id = c.customer_id " +
                       "JOIN rooms r ON b.room_id = r.room_id " +
                       "JOIN room_types rt ON r.type_id = rt.type_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
             
            while (rs.next()) {
                Date actualOutDateSql = rs.getDate("actual_check_out_date");
                LocalDate actualOutDate = (actualOutDateSql != null) ? actualOutDateSql.toLocalDate() : null;

                Date origOutSql = rs.getDate("original_check_out_date");
                LocalDate origOutDate = (origOutSql != null) ? origOutSql.toLocalDate() : rs.getDate("check_out_date").toLocalDate();
                Booking b = new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("room_id"),
                        rs.getDate("check_in_date").toLocalDate(),
                        rs.getDate("check_out_date").toLocalDate(),
                        origOutDate,
                        actualOutDate,
                        rs.getString("actual_check_out_time_category")
                );
                b.setCustomerName(rs.getString("name"));
                b.setRoomInfo("Room " + b.getRoomId() + " (" + rs.getString("type_name") + ")");
                bookings.add(b);
            }
        }
        return bookings;
    }

    /**
     * Returns all booked date ranges for a given room (excluding a specific bookingId so the current stay isn't blocked).
     * Each entry is a LocalDate[] of {checkIn, checkOut}.
     */
    public static List<LocalDate[]> getBookedRangesForRoom(int roomId, int excludeBookingId) throws SQLException {
        List<LocalDate[]> ranges = new ArrayList<>();
        String query = "SELECT check_in_date, check_out_date FROM bookings " +
                       "WHERE room_id = ? AND booking_id != ? AND actual_check_out_date IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, roomId);
            pstmt.setInt(2, excludeBookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate in  = rs.getDate("check_in_date").toLocalDate();
                    LocalDate out = rs.getDate("check_out_date").toLocalDate();
                    ranges.add(new LocalDate[]{in, out});
                }
            }
        }
        return ranges;
    }

    public static void extendStay(int bookingId, LocalDate newCheckOutDate) throws SQLException {
        String query = "UPDATE bookings SET check_out_date = ? WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(newCheckOutDate));
            pstmt.setInt(2, bookingId);
            pstmt.executeUpdate();
        }
    }

    public static void checkoutBooking(int bookingId, int roomId, LocalDate actualCheckOutDate, String timeCategory) throws SQLException {
        Connection conn = DBConnection.getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        
        try {
            conn.setAutoCommit(false);

            String query = "UPDATE bookings SET actual_check_out_date = ?, actual_check_out_time_category = ? WHERE booking_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setDate(1, Date.valueOf(actualCheckOutDate));
                pstmt.setString(2, timeCategory);
                pstmt.setInt(3, bookingId);
                pstmt.executeUpdate();
            }

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
            conn.close(); // FIXED LEAK
        }
    }
}
