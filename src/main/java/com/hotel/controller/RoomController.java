package com.hotel.controller;

import com.hotel.dao.RoomDAO;
import com.hotel.dao.RoomTypeDAO;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.sql.SQLException;
import java.util.List;

public class RoomController {

    // Tab 1 – Rooms
    @FXML private TextField txtRoomId;
    @FXML private ComboBox<RoomType> cmbRoomType;
    @FXML private ComboBox<String> cmbFloor;
    @FXML private TableView<Room> tblRooms;
    @FXML private TableColumn<Room, Integer> colRoomId;
    @FXML private TableColumn<Room, String> colFloor;
    @FXML private TableColumn<Room, String> colRoomTypeName;
    @FXML private TableColumn<Room, Double> colPrice;
    @FXML private TableColumn<Room, Boolean> colIsAvailable;

    // Tab 2 – Room Types (no occupancy)
    @FXML private TextField txtTypeName;
    @FXML private TextField txtBasePrice;
    @FXML private TextArea txtDescription;
    @FXML private TableView<RoomType> tblRoomTypes;
    @FXML private TableColumn<RoomType, Integer> colTypeId;
    @FXML private TableColumn<RoomType, String> colTypeName;
    @FXML private TableColumn<RoomType, Double> colBasePrice;
    @FXML private TableColumn<RoomType, String> colDescription;

    @FXML
    public void initialize() {
        // Room columns
        colRoomId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getRoomId()));
        colFloor.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(deriveFloor(data.getValue().getRoomId())));
        colRoomTypeName.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getRoomTypeName()));
        colPrice.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPrice()));
        colIsAvailable.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().isIsAvailable()));

        colIsAvailable.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item ? "✓ Available" : "✗ Occupied");
                setStyle(item
                    ? "-fx-text-fill: #3fb950; -fx-font-weight: bold;"
                    : "-fx-text-fill: #f85149; -fx-font-weight: bold;");
            }
        });

        // Room type columns
        colTypeId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTypeId()));
        colTypeName.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTypeName()));
        colBasePrice.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getBasePrice()));
        colDescription.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDescription()));

        if (cmbFloor != null) {
            cmbFloor.setItems(FXCollections.observableArrayList(
                "Ground Floor", "1st Floor", "2nd Floor", "3rd Floor",
                "4th Floor", "5th Floor", "6th Floor", "Penthouse"
            ));
        }

        loadAllRoomTypes();
        loadAllRooms();
    }

    private String deriveFloor(int roomId) {
        int floor = roomId / 100;
        if (floor == 0) return "Ground";
        if (floor == 1) return "1st Floor";
        if (floor == 2) return "2nd Floor";
        if (floor == 3) return "3rd Floor";
        return floor + "th Floor";
    }

    @FXML
    public void handleAddRoom() {
        try {
            int roomId = Integer.parseInt(txtRoomId.getText().trim());
            RoomType selectedType = cmbRoomType.getValue();
            if (selectedType == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please select a room category.");
                return;
            }
            Room room = new Room(roomId, selectedType.getTypeId(), true);
            RoomDAO.addRoom(room);
            showAlert(Alert.AlertType.INFORMATION, "Room Added", "Room " + roomId + " added successfully.");
            clearRoomFields();
            loadAllRooms();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Room Number must be a valid integer (e.g. 101).");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                showAlert(Alert.AlertType.ERROR, "Duplicate", "Room " + txtRoomId.getText() + " already exists.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            }
        }
    }

    @FXML
    public void clearRoomFields() {
        if (txtRoomId != null) txtRoomId.clear();
        if (cmbRoomType != null) cmbRoomType.getSelectionModel().clearSelection();
        if (cmbFloor != null) cmbFloor.getSelectionModel().clearSelection();
    }

    @FXML
    public void loadAllRooms() {
        try {
            tblRooms.setItems(FXCollections.observableArrayList(RoomDAO.getAllRooms()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load rooms.");
        }
    }

    @FXML
    public void loadAvailableRooms() {
        try {
            tblRooms.setItems(FXCollections.observableArrayList(RoomDAO.getAvailableRooms()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load available rooms.");
        }
    }

    @FXML
    public void loadOccupiedRooms() {
        try {
            List<Room> all = RoomDAO.getAllRooms();
            all.removeIf(Room::isIsAvailable);
            tblRooms.setItems(FXCollections.observableArrayList(all));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load occupied rooms.");
        }
    }

    @FXML
    public void handleAddRoomType() {
        String typeName = txtTypeName.getText().trim();
        String priceText = txtBasePrice.getText().trim();
        String description = txtDescription != null ? txtDescription.getText().trim() : "";

        if (typeName.isEmpty() || priceText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Category Name and Rate are required.");
            return;
        }
        try {
            double basePrice = Double.parseDouble(priceText);
            RoomType rt = new RoomType(0, typeName, description, basePrice);
            RoomTypeDAO.addRoomType(rt);
            showAlert(Alert.AlertType.INFORMATION, "Category Added", "'" + typeName + "' category created.");
            clearTypeFields();
            loadAllRoomTypes();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Rate must be a valid number.");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                showAlert(Alert.AlertType.ERROR, "Duplicate", "Category '" + typeName + "' already exists.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            }
        }
    }

    @FXML
    public void clearTypeFields() {
        if (txtTypeName != null) txtTypeName.clear();
        if (txtBasePrice != null) txtBasePrice.clear();
        if (txtDescription != null) txtDescription.clear();
    }

    @FXML
    public void loadAllRoomTypes() {
        try {
            ObservableList<RoomType> list = FXCollections.observableArrayList(RoomTypeDAO.getAllRoomTypes());
            tblRoomTypes.setItems(list);
            cmbRoomType.setItems(list);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load room categories.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
