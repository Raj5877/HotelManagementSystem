package com.hotel.controller;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.RoomDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.sql.SQLException;

public class DashboardController {

    @FXML private StackPane contentArea;

    // Dashboard stat labels
    @FXML private Label lblTotalRooms;
    @FXML private Label lblAvailableRooms;
    @FXML private Label lblActiveBookings;
    @FXML private Label lblTotalGuests;

    // Track the dashboard welcome content so we can restore it
    private Node dashboardContent;

    @FXML
    public void initialize() {
        // Capture the initial welcome content
        if (contentArea != null && !contentArea.getChildren().isEmpty()) {
            dashboardContent = contentArea.getChildren().get(0);
        }
        refreshStats();
    }

    private void refreshStats() {
        try {
            int totalRooms = RoomDAO.getAllRooms().size();
            int availableRooms = RoomDAO.getAvailableRooms().size();
            int activeBookings = (int) BookingDAO.getAllBookings().stream()
                    .filter(b -> b.getActualCheckOutDate() == null).count();
            int totalGuests = CustomerDAO.getAllCustomers().size();

            if (lblTotalRooms != null) lblTotalRooms.setText(String.valueOf(totalRooms));
            if (lblAvailableRooms != null) lblAvailableRooms.setText(String.valueOf(availableRooms));
            if (lblActiveBookings != null) lblActiveBookings.setText(String.valueOf(activeBookings));
            if (lblTotalGuests != null) lblTotalGuests.setText(String.valueOf(totalGuests));
        } catch (SQLException e) {
            // Stat cards will show "—" if DB is not connected
        }
    }

    @FXML
    public void showDashboard() {
        // Restore the dashboard welcome screen
        if (dashboardContent != null) {
            contentArea.getChildren().setAll(dashboardContent);
        }
        refreshStats();
    }

    @FXML
    public void showRooms() {
        loadView("/fxml/room.fxml");
    }

    @FXML
    public void showCustomers() {
        loadView("/fxml/customer.fxml");
    }

    @FXML
    public void showBookings() {
        loadView("/fxml/booking.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
