package com.hotel.controller;

import com.hotel.dao.RoomDAO;
import com.hotel.model.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

public class RoomController {

    @FXML private TextField txtRoomId;
    @FXML private ComboBox<String> cmbRoomType;
    @FXML private TextField txtPrice;
    @FXML private TableView<Room> tblRooms;

    @FXML
    public void initialize() {
        // Dropdown items are predefined in FXML, so we just load table data
        loadAllRooms();
    }

    @FXML
    public void handleAddRoom() {
        try {
            int roomId = Integer.parseInt(txtRoomId.getText());
            String roomType = cmbRoomType.getValue();
            double price = Double.parseDouble(txtPrice.getText());

            if (roomType == null || roomType.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please select a room type.");
                return;
            }

            Room room = new Room(roomId, roomType, price, true);
            RoomDAO.addRoom(room);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Room added successfully!");
            clearFields();
            loadAllRooms();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numbers for Room ID and Price.");
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Room Number already exists.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            }
        }
    }

    @FXML
    public void loadAllRooms() {
        try {
            List<Room> rooms = RoomDAO.getAllRooms();
            populateTable(rooms);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load rooms.");
        }
    }

    @FXML
    public void loadAvailableRooms() {
        try {
            List<Room> rooms = RoomDAO.getAvailableRooms();
            populateTable(rooms);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load available rooms.");
        }
    }

    private void populateTable(List<Room> rooms) {
        ObservableList<Room> observableList = FXCollections.observableArrayList(rooms);
        tblRooms.setItems(observableList);
    }

    private void clearFields() {
        txtRoomId.clear();
        cmbRoomType.getSelectionModel().clearSelection();
        txtPrice.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
