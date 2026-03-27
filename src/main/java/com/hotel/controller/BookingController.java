package com.hotel.controller;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class BookingController {

    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private ComboBox<Room> cmbRoom;
    @FXML private DatePicker dpCheckIn;
    @FXML private DatePicker dpCheckOut;

    @FXML private ComboBox<Booking> cmbBooking;
    @FXML private DatePicker dpActualCheckOut;
    @FXML private javafx.scene.control.TextField txtCustomDiscount;

    @FXML private TableView<Booking> tblBookings;

    @FXML
    public void initialize() {
        loadComboData();
        loadAllBookings();
    }

    private void loadComboData() {
        try {
            List<Customer> customers = CustomerDAO.getAllCustomers();
            cmbCustomer.setItems(FXCollections.observableArrayList(customers));

            List<Room> rooms = RoomDAO.getAvailableRooms();
            cmbRoom.setItems(FXCollections.observableArrayList(rooms));

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load dropdown data.");
        }
    }

    @FXML
    public void handleSuggestRoom() {
        try {
            Room cheapest = RoomDAO.getCheapestAvailableRoom();
            if (cheapest != null) {
                cmbRoom.setValue(cheapest);
                showAlert(Alert.AlertType.INFORMATION, "Suggestion", 
                        "Cheapest Available Room is Room " + cheapest.getRoomId() + " at $" + cheapest.getPrice());
            } else {
                showAlert(Alert.AlertType.WARNING, "No Rooms", "There are no available rooms.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBookRoom() {
        Customer c = cmbCustomer.getValue();
        Room r = cmbRoom.getValue();
        LocalDate in = dpCheckIn.getValue();
        LocalDate out = dpCheckOut.getValue();

        if (c == null || r == null || in == null || out == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Please fill all booking fields.");
            return;
        }

        if (out.isBefore(in) || out.isEqual(in)) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Check-out must be strictly after Check-in.");
            return;
        }

        try {
            Booking b = new Booking(0, c.getCustomerId(), r.getRoomId(), in, out, null);
            BookingDAO.addBooking(b);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Room successfully booked!");
            
            cmbCustomer.getSelectionModel().clearSelection();
            cmbRoom.getSelectionModel().clearSelection();
            dpCheckIn.setValue(null);
            dpCheckOut.setValue(null);

            // refresh
            loadComboData(); 
            loadAllBookings();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    @FXML
    public void loadAllBookings() {
        try {
            List<Booking> bookings = BookingDAO.getAllBookings();
            ObservableList<Booking> bs = FXCollections.observableArrayList(bookings);
            tblBookings.setItems(bs);

            // Filter out already checked-out bookings for the combobox
            ObservableList<Booking> active = FXCollections.observableArrayList();
            for (Booking b : bookings) {
                if (b.getActualCheckOutDate() == null) {
                    active.add(b);
                }
            }
            cmbBooking.setItems(active);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCheckout() {
        Booking b = cmbBooking.getValue();
        LocalDate actualOut = dpActualCheckOut.getValue();

        if (b == null || actualOut == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Please select a booking and actual check-out date.");
            return;
        }

        if (actualOut.isBefore(b.getCheckInDate())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Actual check-out cannot be before check-in.");
            return;
        }

        try {
            BookingDAO.checkoutBooking(b.getBookingId(), b.getRoomId(), actualOut);
            
            double extraDiscount = 0.0;
            if (txtCustomDiscount != null && txtCustomDiscount.getText() != null && !txtCustomDiscount.getText().isEmpty()) {
                try {
                    extraDiscount = Double.parseDouble(txtCustomDiscount.getText());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid discount value.");
                }
            }

            // Calculate Bill
            showBillingDialog(b, actualOut, extraDiscount);

            // Refresh UI
            cmbBooking.getSelectionModel().clearSelection();
            dpActualCheckOut.setValue(null);
            if (txtCustomDiscount != null) txtCustomDiscount.clear();
            loadComboData();
            loadAllBookings();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    private void showBillingDialog(Booking booking, LocalDate actualOut, double customDiscountRate) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/billing.fxml"));
            Parent root = loader.load();

            BillingController controller = loader.getController();
            controller.calculateAndSetBill(booking, actualOut, customDiscountRate);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Final Invoice");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
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
