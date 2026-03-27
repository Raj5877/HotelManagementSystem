package com.hotel.dao;

import com.hotel.model.RoomType;
import com.hotel.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomTypeDAO {

    public static void addRoomType(RoomType roomType) throws SQLException {
        String query = "INSERT INTO room_types (type_name, description, base_price) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, roomType.getTypeName());
            pstmt.setString(2, roomType.getDescription());
            pstmt.setDouble(3, roomType.getBasePrice());
            pstmt.executeUpdate();
        }
    }

    public static List<RoomType> getAllRoomTypes() throws SQLException {
        List<RoomType> types = new ArrayList<>();
        String query = "SELECT * FROM room_types";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                types.add(new RoomType(
                        rs.getInt("type_id"),
                        rs.getString("type_name"),
                        rs.getString("description"),
                        rs.getDouble("base_price")
                ));
            }
        }
        return types;
    }
}
