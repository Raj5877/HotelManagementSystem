package com.hotel.controller;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BookingController {

    // Tab 1: New Booking fields
    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private ComboBox<Room> cmbRoom;
    @FXML private DatePicker dpCheckIn;
    @FXML private DatePicker dpCheckOut;
    @FXML private TableView<Booking> tblBookingsNew;
    @FXML private TableColumn<Booking, Integer> colBookingIdNew;
    @FXML private TableColumn<Booking, String> colCustomerNew;
    @FXML private TableColumn<Booking, String> colRoomNew;
    @FXML private TableColumn<Booking, String> colInDateNew;
    @FXML private TableColumn<Booking, String> colOutDateNew;
    @FXML private TableColumn<Booking, String> colStatusNew;

    // Tab 2: Manage & Checkout fields
    @FXML private ComboBox<Booking> cmbManageBooking;
    @FXML private DatePicker dpExtendTo;
    @FXML private ComboBox<String> cmbCheckoutTime;
    @FXML private TableView<Booking> tblBookingsManage;
    @FXML private TableColumn<Booking, Integer> colBookingIdManage;
    @FXML private TableColumn<Booking, String> colCustomerManage;
    @FXML private TableColumn<Booking, String> colRoomManage;
    @FXML private TableColumn<Booking, String> colInDateManage;
    @FXML private TableColumn<Booking, String> colOutDateManage;
    @FXML private TableColumn<Booking, String> colStatusManage;

    @FXML
    public void initialize() {
        // Init table columns for Tab 1
        colBookingIdNew.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getBookingId()));
        colCustomerNew.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCustomerName()));
        colRoomNew.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getRoomInfo()));
        colInDateNew.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCheckInDate().toString()));
        colOutDateNew.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCheckOutDate().toString()));
        colStatusNew.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStatus()));

        // Init table columns for Tab 2
        colBookingIdManage.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getBookingId()));
        colCustomerManage.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCustomerName()));
        colRoomManage.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getRoomInfo()));
        colInDateManage.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCheckInDate().toString()));
        colOutDateManage.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCheckOutDate().toString()));
        colStatusManage.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStatus()));

        dpCheckIn.setValue(LocalDate.now());

        cmbCheckoutTime.setItems(FXCollections.observableArrayList(
                "Before 12:00 PM (Standard)",
                "12:00 PM - 2:00 PM (Small Delay)",
                "2:00 PM - 6:00 PM (Moderate Delay)",
                "After 6:00 PM (Full Night)"
        ));

        // Sync dropdowns whenever lists load
        loadDropdownData();
        loadAllBookings();
    }

    private void loadDropdownData() {
        try {
            List<Customer> customers = CustomerDAO.getAllCustomers();
            cmbCustomer.setItems(FXCollections.observableArrayList(customers));

            List<Room> availableRooms = RoomDAO.getAvailableRooms();
            cmbRoom.setItems(FXCollections.observableArrayList(availableRooms));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load dropdown data: " + e.getMessage());
        }
    }

    @FXML
    public void loadAllBookings() {
        try {
            List<Booking> bookings = BookingDAO.getAllBookings();
            tblBookingsNew.setItems(FXCollections.observableArrayList(bookings));
            tblBookingsManage.setItems(FXCollections.observableArrayList(bookings));

            // Only populate Active Bookings into the manage combo box
            List<Booking> activeBookings = bookings.stream()
                    .filter(b -> b.getActualCheckOutDate() == null)
                    .collect(Collectors.toList());
            cmbManageBooking.setItems(FXCollections.observableArrayList(activeBookings));

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load bookings: " + e.getMessage());
        }
    }

    @FXML
    public void handleAddBooking() {
        Customer selectedCustomer = cmbCustomer.getValue();
        Room selectedRoom = cmbRoom.getValue();
        LocalDate checkIn = dpCheckIn.getValue();
        LocalDate checkOut = dpCheckOut.getValue();

        if (selectedCustomer == null || selectedRoom == null || checkIn == null || checkOut == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill in all fields to book.");
            return;
        }

        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Check-Out date must be strictly after Check-In date.");
            return;
        }

        try {
            Booking booking = new Booking(0, selectedCustomer.getCustomerId(), selectedRoom.getRoomId(), checkIn, checkOut, null, null);
            BookingDAO.addBooking(booking);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Booking created successfully!");
            dpCheckOut.setValue(null);
            
            loadDropdownData(); // remove the newly booked room from dropdown
            loadAllBookings();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create booking.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleExtendStay() {
        Booking selectedBooking = cmbManageBooking.getValue();
        LocalDate extendedDate = dpExtendTo.getValue();

        if (selectedBooking == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a booking to extend.");
            return;
        }
        if (extendedDate == null || !extendedDate.isAfter(selectedBooking.getCheckOutDate())) {
            showAlert(Alert.AlertType.ERROR, "Date Error", "Extended date must be AFTER the current checkout date.");
            return;
        }

        try {
            BookingDAO.extendStay(selectedBooking.getBookingId(), extendedDate);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Stay officially extended to " + extendedDate.toString());
            dpExtendTo.setValue(null);
            loadAllBookings();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to extend stay.");
        }
    }

    @FXML
    public void handleCheckout() {
        Booking selectedBooking = cmbManageBooking.getValue();
        String timeCategoryIndex = cmbCheckoutTime.getValue();

        if (selectedBooking == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select an active booking to checkout.");
            return;
        }
        if (timeCategoryIndex == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select the departure time window.");
            return;
        }

        // The exact moment of checking out is logically considered 'today'.
        LocalDate actualCheckoutDate = LocalDate.now();

        // Standardize the time category key for Billing
        String timeCategory = "BEFORE_12PM";
        if (timeCategoryIndex.contains("12:00 PM - 2:00 PM")) timeCategory = "12PM_TO_2PM";
        else if (timeCategoryIndex.contains("2:00 PM - 6:00 PM")) timeCategory = "2PM_TO_6PM";
        else if (timeCategoryIndex.contains("After 6:00 PM")) timeCategory = "AFTER_6PM";

        try {
            BookingDAO.checkoutBooking(selectedBooking.getBookingId(), selectedBooking.getRoomId(), actualCheckoutDate, timeCategory);
            
            // Re-fetch the newly finalized Booking to pass into the bill
            Booking completedBooking = BookingDAO.getAllBookings().stream()
                    .filter(b -> b.getBookingId() == selectedBooking.getBookingId())
                    .findFirst().orElse(null);

            if (completedBooking != null) {
                showBillingDialog(completedBooking);
            }
            
            cmbCheckoutTime.getSelectionModel().clearSelection();
            loadDropdownData();
            loadAllBookings();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to process checkout.");
            e.printStackTrace();
        }
    }

    private void showBillingDialog(Booking booking) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/billing.fxml"));
            Parent root = loader.load();

            BillingController billingController = loader.getController();
            billingController.calculateAndSetBill(booking, booking.getActualCheckOutDate(), 0.0);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Final Invoice");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "UI Error", "Could not load the billing screen.");
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
