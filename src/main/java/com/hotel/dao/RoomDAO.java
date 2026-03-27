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
        String query = "INSERT INTO rooms (room_id, type_id, is_available) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, room.getRoomId());
            pstmt.setInt(2, room.getTypeId());
            pstmt.setBoolean(3, room.getIsAvailable());
            pstmt.executeUpdate();
        }
    }

    private static Room mapRowToRoom(ResultSet rs) throws SQLException {
        Room r = new Room(
                rs.getInt("room_id"),
                rs.getInt("type_id"),
                rs.getBoolean("is_available")
        );
        r.setRoomTypeName(rs.getString("type_name"));
        r.setPrice(rs.getDouble("base_price"));
        return r;
    }

    public static List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT r.*, rt.type_name, rt.base_price FROM rooms r " +
                       "JOIN room_types rt ON r.type_id = rt.type_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapRowToRoom(rs));
            }
        }
        return rooms;
    }

    public static List<Room> getAvailableRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT r.*, rt.type_name, rt.base_price FROM rooms r " +
                       "JOIN room_types rt ON r.type_id = rt.type_id " +
                       "WHERE r.is_available = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapRowToRoom(rs));
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
        String query = "SELECT r.*, rt.type_name, rt.base_price FROM rooms r " +
                       "JOIN room_types rt ON r.type_id = rt.type_id " +
                       "WHERE r.is_available = TRUE ORDER BY rt.base_price ASC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return mapRowToRoom(rs);
            }
        }
        return null;
    }

    public static Room getRoomById(int roomId) throws SQLException {
        String query = "SELECT r.*, rt.type_name, rt.base_price FROM rooms r " +
                       "JOIN room_types rt ON r.type_id = rt.type_id " +
                       "WHERE r.room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, roomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToRoom(rs);
                }
            }
        }
        return null;
    }
}
