package com.hotel.controller;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.dao.RoomTypeDAO;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class BookingController {

    // Tab 1: New Booking
    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private ComboBox<Room> cmbRoom;
    @FXML private DatePicker dpCheckIn;
    @FXML private DatePicker dpCheckOut;
    @FXML private TextField txtSpecialRequests;
    @FXML private Label lblEstimatedCost;
    @FXML private TableView<Booking> tblBookingsNew;
    @FXML private TableColumn<Booking, Integer> colBookingIdNew;
    @FXML private TableColumn<Booking, String> colCustomerNew;
    @FXML private TableColumn<Booking, String> colRoomNew;
    @FXML private TableColumn<Booking, String> colInDateNew;
    @FXML private TableColumn<Booking, String> colOutDateNew;
    @FXML private TableColumn<Booking, String> colStatusNew;

    // Room types side panel
    @FXML private VBox pnlRoomTypes;

    // Tab 2: Manage & Checkout
    @FXML private ComboBox<Booking> cmbManageBooking;
    @FXML private Label lblManageGuest;
    @FXML private Label lblManageRoom;
    @FXML private Label lblManageDaysLeft;
    @FXML private DatePicker dpExtendTo;
    @FXML private ComboBox<String> cmbCheckoutTime;
    @FXML private TextField txtManualDiscount;
    @FXML private TableView<Booking> tblBookingsManage;
    @FXML private TableColumn<Booking, Integer> colBookingIdManage;
    @FXML private TableColumn<Booking, String> colCustomerManage;
    @FXML private TableColumn<Booking, String> colRoomManage;
    @FXML private TableColumn<Booking, String> colInDateManage;
    @FXML private TableColumn<Booking, String> colOutDateManage;
    @FXML private TableColumn<Booking, String> colStatusManage;

    @FXML
    public void initialize() {
        // Tab 1 columns
        colBookingIdNew.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getBookingId()));
        colCustomerNew.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCustomerName()));
        colRoomNew.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getRoomInfo()));
        colInDateNew.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCheckInDate().toString()));
        colOutDateNew.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCheckOutDate().toString()));
        colStatusNew.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getStatus()));

        // Tab 2 columns
        colBookingIdManage.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getBookingId()));
        colCustomerManage.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCustomerName()));
        colRoomManage.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getRoomInfo()));
        colInDateManage.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCheckInDate().toString()));
        colOutDateManage.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getCheckOutDate().toString()));
        colStatusManage.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getStatus()));

        // Status column coloring for Tab 1
        colStatusNew.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.equals("Active")
                    ? "-fx-text-fill: #3fb950; -fx-font-weight: bold;"
                    : "-fx-text-fill: #388bfd; -fx-font-weight: bold;");
            }
        });

        // Status column coloring for Tab 2
        colStatusManage.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.equals("Active")
                    ? "-fx-text-fill: #3fb950; -fx-font-weight: bold;"
                    : "-fx-text-fill: #388bfd; -fx-font-weight: bold;");
            }
        });

        // Set today as default check-in, restrict past dates
        dpCheckIn.setValue(LocalDate.now());
        applyDatePickerRestrictions(dpCheckIn, LocalDate.now());
        applyDatePickerRestrictions(dpCheckOut, LocalDate.now().plusDays(1));
        applyDatePickerRestrictions(dpExtendTo, LocalDate.now().plusDays(1));

        // When check-in changes, update the minimum check-out date
        dpCheckIn.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applyDatePickerRestrictions(dpCheckOut, newVal.plusDays(1));
                if (dpCheckOut.getValue() != null && !dpCheckOut.getValue().isAfter(newVal)) {
                    dpCheckOut.setValue(null);
                }
            }
            updateCostEstimate();
        });

        cmbCheckoutTime.setItems(FXCollections.observableArrayList(
                "Before 12:00 PM (Standard — No Fee)",
                "12:00 PM – 2:00 PM (Small Delay — 25%)",
                "2:00 PM – 6:00 PM (Moderate Delay — 50%)",
                "After 6:00 PM (Late Checkout — Full Night)"
        ));

        // Live cost estimator
        cmbRoom.setOnAction(e -> updateCostEstimate());
        dpCheckOut.setOnAction(e -> updateCostEstimate());

        // Booking summary panel
        cmbManageBooking.setOnAction(e -> updateManageSummary());

        loadDropdownData();
        loadAllBookings();
        loadRoomTypesPanel();
    }

    /**
     * Apply day cell factory that:
     *  - Disables and dims past dates (before minDate)
     *  - Makes other-month days invisible
     */
    private void applyDatePickerRestrictions(DatePicker dp, LocalDate minDate) {
        dp.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                // Disable and dim past / before-minimum dates
                if (date.isBefore(minDate)) {
                    setDisable(true);
                    setStyle("-fx-background-color: transparent; " +
                             "-fx-text-fill: #2d333b; " +
                             "-fx-opacity: 0.4; " +
                             "-fx-cursor: default;");
                }

                // Hide other-month days (previous/next month overflow)
                // JavaFX DatePicker shows these as grayed-out; we make them invisible
                // by checking if the CSS pseudo-class "previous-month" or "next-month" applies.
                // We can't easily detect that here, so we rely on CSS for that.
            }
        });
    }

    private void loadRoomTypesPanel() {
        if (pnlRoomTypes == null) return;
        pnlRoomTypes.getChildren().clear();

        try {
            List<RoomType> types = RoomTypeDAO.getAllRoomTypes();
            for (RoomType rt : types) {
                VBox card = buildRoomTypeCard(rt);
                pnlRoomTypes.getChildren().add(card);
            }
            if (types.isEmpty()) {
                Label empty = new Label("No room categories defined yet.");
                empty.setStyle("-fx-text-fill: #484f58; -fx-font-size: 12px; -fx-padding: 16;");
                pnlRoomTypes.getChildren().add(empty);
            }
        } catch (SQLException e) {
            Label err = new Label("Could not load room types.");
            err.setStyle("-fx-text-fill: #f85149; -fx-font-size: 12px; -fx-padding: 16;");
            pnlRoomTypes.getChildren().add(err);
        }
    }

    private VBox buildRoomTypeCard(RoomType rt) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle("-fx-border-color: transparent transparent #30363d transparent; -fx-border-width: 0 0 1 0;");

        // Name + Rate row
        HBox nameRow = new HBox();
        Label name = new Label(rt.getTypeName());
        name.setStyle("-fx-text-fill: #e6edf3; -fx-font-size: 12px; -fx-font-weight: bold;");
        HBox.setHgrow(name, Priority.ALWAYS);

        Label rate = new Label(String.format("₹%.0f/night", rt.getBasePrice()));
        rate.setStyle("-fx-text-fill: #c9a84c; -fx-font-size: 12px; -fx-font-weight: bold;");

        nameRow.getChildren().addAll(name, rate);

        // Description
        String desc = rt.getDescription();
        if (desc != null && !desc.isBlank()) {
            Label descLabel = new Label(desc);
            descLabel.setStyle("-fx-text-fill: #8b949e; -fx-font-size: 11px;");
            descLabel.setWrapText(true);
            card.getChildren().addAll(nameRow, descLabel);
        } else {
            card.getChildren().add(nameRow);
        }

        return card;
    }

    private void updateCostEstimate() {
        Room room = cmbRoom.getValue();
        LocalDate in = dpCheckIn.getValue();
        LocalDate out = dpCheckOut.getValue();
        if (room == null || in == null || out == null || !out.isAfter(in)) {
            if (lblEstimatedCost != null) lblEstimatedCost.setText("—  (select room and dates)");
            return;
        }
        long nights = ChronoUnit.DAYS.between(in, out);
        double base = nights * room.getPrice();
        double gst = base * 0.12;
        String text = String.format("₹%.2f  (%d night%s × ₹%.0f + 12%% GST)",
                base + gst, nights, nights > 1 ? "s" : "", room.getPrice());
        if (lblEstimatedCost != null) lblEstimatedCost.setText(text);
    }

    private void updateManageSummary() {
        Booking b = cmbManageBooking.getValue();
        if (b == null) {
            if (lblManageGuest != null) lblManageGuest.setText("—");
            if (lblManageRoom != null) lblManageRoom.setText("—");
            if (lblManageDaysLeft != null) lblManageDaysLeft.setText("—");
            return;
        }
        if (lblManageGuest != null) lblManageGuest.setText(b.getCustomerName() != null ? b.getCustomerName() : "—");
        if (lblManageRoom != null) lblManageRoom.setText(b.getRoomInfo() != null ? b.getRoomInfo() : "—");
        if (lblManageDaysLeft != null) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), b.getCheckOutDate());
            if (daysLeft > 0) lblManageDaysLeft.setText(daysLeft + " day(s) remaining");
            else if (daysLeft == 0) lblManageDaysLeft.setText("Checkout due today");
            else lblManageDaysLeft.setText(Math.abs(daysLeft) + " day(s) overdue");
        }
    }

    private void loadDropdownData() {
        try {
            cmbCustomer.setItems(FXCollections.observableArrayList(CustomerDAO.getAllCustomers()));
            cmbRoom.setItems(FXCollections.observableArrayList(RoomDAO.getAvailableRooms()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load dropdown data: " + e.getMessage());
        }
    }

    @FXML
    public void loadAllBookings() {
        try {
            List<Booking> all = BookingDAO.getAllBookings();
            tblBookingsNew.setItems(FXCollections.observableArrayList(all));
            tblBookingsManage.setItems(FXCollections.observableArrayList(all));

            List<Booking> active = all.stream()
                    .filter(b -> b.getActualCheckOutDate() == null)
                    .collect(Collectors.toList());
            cmbManageBooking.setItems(FXCollections.observableArrayList(active));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load bookings: " + e.getMessage());
        }
        loadRoomTypesPanel();
    }

    @FXML
    public void handleAddBooking() {
        Customer guest = cmbCustomer.getValue();
        Room room = cmbRoom.getValue();
        LocalDate checkIn = dpCheckIn.getValue();
        LocalDate checkOut = dpCheckOut.getValue();

        if (guest == null || room == null || checkIn == null || checkOut == null) {
            showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all required fields before confirming.");
            return;
        }
        if (!checkOut.isAfter(checkIn)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Dates", "Check-out date must be after check-in date.");
            return;
        }

        try {
            Booking booking = new Booking(0, guest.getCustomerId(), room.getRoomId(), checkIn, checkOut, null, null);
            BookingDAO.addBooking(booking);
            showAlert(Alert.AlertType.INFORMATION, "Reservation Confirmed",
                    "Booking created for " + guest.getName() + " in Room " + room.getRoomId() + ".");
            clearBookingForm();
            loadDropdownData();
            loadAllBookings();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create reservation: " + e.getMessage());
        }
    }

    @FXML
    public void clearBookingForm() {
        if (cmbCustomer != null) cmbCustomer.getSelectionModel().clearSelection();
        if (cmbRoom != null) cmbRoom.getSelectionModel().clearSelection();
        if (dpCheckOut != null) dpCheckOut.setValue(null);
        if (txtSpecialRequests != null) txtSpecialRequests.clear();
        if (lblEstimatedCost != null) lblEstimatedCost.setText("—  (select room and dates)");
    }

    @FXML
    public void handleExtendStay() {
        Booking selected = cmbManageBooking.getValue();
        LocalDate extendedDate = dpExtendTo.getValue();

        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "No Booking Selected", "Please select an active booking to extend.");
            return;
        }
        if (extendedDate == null || !extendedDate.isAfter(selected.getCheckOutDate())) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date", "New date must be after the current check-out: " + selected.getCheckOutDate());
            return;
        }

        try {
            BookingDAO.extendStay(selected.getBookingId(), extendedDate);
            showAlert(Alert.AlertType.INFORMATION, "Stay Extended",
                    "Stay extended to " + extendedDate + " for " + selected.getCustomerName() + ".");
            dpExtendTo.setValue(null);
            loadAllBookings();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to extend stay.");
        }
    }

    @FXML
    public void handleCheckout() {
        Booking selected = cmbManageBooking.getValue();
        String timeSelection = cmbCheckoutTime.getValue();

        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "No Booking Selected", "Please select an active booking to check out.");
            return;
        }
        if (timeSelection == null) {
            showAlert(Alert.AlertType.ERROR, "Missing Field", "Please select the departure time window.");
            return;
        }

        double manualDiscount = 0.0;
        if (txtManualDiscount != null && !txtManualDiscount.getText().trim().isEmpty()) {
            try {
                manualDiscount = Double.parseDouble(txtManualDiscount.getText().trim());
                if (manualDiscount < 0 || manualDiscount > 100) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Discount", "Discount must be between 0 and 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Discount", "Please enter a valid number for discount.");
                return;
            }
        }

        String timeCategory = "BEFORE_12PM";
        if (timeSelection.contains("12:00 PM – 2:00 PM")) timeCategory = "12PM_TO_2PM";
        else if (timeSelection.contains("2:00 PM – 6:00 PM")) timeCategory = "2PM_TO_6PM";
        else if (timeSelection.contains("After 6:00 PM"))    timeCategory = "AFTER_6PM";

        LocalDate actualCheckout = LocalDate.now();
        final double finalDiscount = manualDiscount;

        try {
            BookingDAO.checkoutBooking(selected.getBookingId(), selected.getRoomId(), actualCheckout, timeCategory);

            Booking completed = BookingDAO.getAllBookings().stream()
                    .filter(b -> b.getBookingId() == selected.getBookingId())
                    .findFirst().orElse(null);

            if (completed != null) {
                showBillingDialog(completed, finalDiscount);
            }

            cmbCheckoutTime.getSelectionModel().clearSelection();
            if (txtManualDiscount != null) txtManualDiscount.clear();
            loadDropdownData();
            loadAllBookings();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to process checkout: " + e.getMessage());
        }
    }

    private void showBillingDialog(Booking booking, double customDiscount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/billing.fxml"));
            Parent root = loader.load();

            BillingController billingController = loader.getController();
            billingController.calculateAndSetBill(booking, booking.getActualCheckOutDate(), customDiscount);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Invoice — " + booking.getCustomerName());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
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
