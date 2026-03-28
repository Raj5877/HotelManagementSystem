package com.hotel.controller;

import com.hotel.dao.RoomDAO;
import com.hotel.dao.RoomTypeDAO;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.sql.SQLException;
import java.util.List;

public class RoomController {

    // Tab 1 UI
    @FXML private TextField txtRoomId;
    @FXML private ComboBox<RoomType> cmbRoomType;
    @FXML private TableView<Room> tblRooms;
    @FXML private TableColumn<Room, Integer> colRoomId;
    @FXML private TableColumn<Room, String> colRoomTypeName;
    @FXML private TableColumn<Room, Double> colPrice;
    @FXML private TableColumn<Room, Boolean> colIsAvailable;

    // Tab 2 UI
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
        // Init Room columns
        colRoomId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getRoomId()));
        colRoomTypeName.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getRoomTypeName()));
        colPrice.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPrice()));
        colIsAvailable.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().isIsAvailable()));

        // Init RoomType columns
        colTypeId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTypeId()));
        colTypeName.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTypeName()));
        colBasePrice.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getBasePrice()));
        colDescription.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDescription()));

        loadAllRoomTypes();
        loadAllRooms();
    }

    // --- TAB 1: MANAGE ROOMS ---

    @FXML
    public void handleAddRoom() {
        try {
            int roomId = Integer.parseInt(txtRoomId.getText());
            RoomType selectedType = cmbRoomType.getValue();

            if (selectedType == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please select a room type.");
                return;
            }

            Room room = new Room(roomId, selectedType.getTypeId(), true);
            RoomDAO.addRoom(room);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Room added successfully!");
            txtRoomId.clear();
            cmbRoomType.getSelectionModel().clearSelection();
            loadAllRooms();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number for Room ID.");
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
            tblRooms.setItems(FXCollections.observableArrayList(rooms));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load rooms. Please make sure to reset your Database schema via schema.sql.");
        }
    }

    @FXML
    public void loadAvailableRooms() {
        try {
            List<Room> rooms = RoomDAO.getAvailableRooms();
            tblRooms.setItems(FXCollections.observableArrayList(rooms));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load available rooms.");
        }
    }

    // --- TAB 2: MANAGE ROOM TYPES ---

    @FXML
    public void handleAddRoomType() {
        String typeName = txtTypeName.getText().trim();
        String description = txtDescription.getText().trim();
        String priceText = txtBasePrice.getText().trim();

        if (typeName.isEmpty() || priceText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Type Name and Base Price cannot be empty.");
            return;
        }

        try {
            double basePrice = Double.parseDouble(priceText);
            RoomType rt = new RoomType(0, typeName, description, basePrice);
            RoomTypeDAO.addRoomType(rt);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Room Type added successfully!");
            txtTypeName.clear();
            txtBasePrice.clear();
            txtDescription.clear();
            
            // Reload both tables and the dropdown since a new type was added!
            loadAllRoomTypes();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number for Base Price.");
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Room Type name already exists.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add room type: " + e.getMessage());
            }
        }
    }

    @FXML
    public void loadAllRoomTypes() {
        try {
            List<RoomType> types = RoomTypeDAO.getAllRoomTypes();
            ObservableList<RoomType> observableList = FXCollections.observableArrayList(types);
            tblRoomTypes.setItems(observableList);
            // Additionally synchronize the Tab 1 combo box!
            cmbRoomType.setItems(observableList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load room types. Please ensure your schema.sql successfully reset the tables.");
        }
    }

    // --- UTILS ---

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
