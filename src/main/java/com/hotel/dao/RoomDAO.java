package com.hotel.dao;

import com.hotel.model.Room;
import com.hotel.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public static void addRoom(Room room) throws SQLException {
        String query = "INSERT INTO rooms (room_id, room_type, price, is_available) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, room.getRoomId());
            pstmt.setString(2, room.getRoomType());
            pstmt.setDouble(3, room.getPrice());
            pstmt.setBoolean(4, room.getIsAvailable());
            pstmt.executeUpdate();
        }
    }

    public static List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM rooms";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                rooms.add(new Room(
                        rs.getInt("room_id"),
                        rs.getString("room_type"),
                        rs.getDouble("price"),
                        rs.getBoolean("is_available")
                ));
            }
        }
        return rooms;
    }

    public static List<Room> getAvailableRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM rooms WHERE is_available = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                rooms.add(new Room(
                        rs.getInt("room_id"),
                        rs.getString("room_type"),
                        rs.getDouble("price"),
                        rs.getBoolean("is_available")
                ));
            }
        }
        return rooms;
    }

    public static void updateRoomAvailability(int roomId, boolean isAvailable) throws SQLException {
        String query = "UPDATE rooms SET is_available = ? WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBoolean(1, isAvailable);
            pstmt.setInt(2, roomId);
            pstmt.executeUpdate();
        }
    }

    public static Room getCheapestAvailableRoom() throws SQLException {
        String query = "SELECT * FROM rooms WHERE is_available = TRUE ORDER BY price ASC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return new Room(
                        rs.getInt("room_id"),
                        rs.getString("room_type"),
                        rs.getDouble("price"),
                        rs.getBoolean("is_available")
                );
            }
        }
        return null;
    }

    public static Room getRoomById(int roomId) throws SQLException {
        String query = "SELECT * FROM rooms WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, roomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Room(
                            rs.getInt("room_id"),
                            rs.getString("room_type"),
                            rs.getDouble("price"),
                            rs.getBoolean("is_available")
                    );
                }
            }
        }
        return null;
    }
}
