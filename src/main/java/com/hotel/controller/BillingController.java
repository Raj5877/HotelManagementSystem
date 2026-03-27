package com.hotel.controller;

import com.hotel.dao.RoomDAO;
import com.hotel.model.Booking;
import com.hotel.model.Room;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BillingController {

    @FXML private Label lblCustomerName;
    @FXML private Label lblRoomInfo;
    @FXML private Label lblDaysStayed;
    @FXML private Label lblBaseCost;
    @FXML private Label lblDiscount;
    @FXML private Label lblCustomDiscount;
    @FXML private Label lblLateFee;
    @FXML private Label lblTotal;

    public void calculateAndSetBill(Booking b, LocalDate actualOut, double customDiscountRate) {
        try {
            Room r = RoomDAO.getRoomById(b.getRoomId());
            if (r == null) return;

            long plannedDays = ChronoUnit.DAYS.between(b.getCheckInDate(), b.getCheckOutDate());
            if (plannedDays == 0) plannedDays = 1; // Minimum 1 day

            long actualDays = ChronoUnit.DAYS.between(b.getCheckInDate(), actualOut);
            if (actualDays == 0) actualDays = 1;
            
            // Base Cost calculation (up to actual days or planned days, whichever is smaller, the rest is penalty)
            long standardDaysStayed = Math.min(plannedDays, actualDays);
            double baseCost = standardDaysStayed * r.getPrice();

            // Discount
            double discountRate = 0;
            if (standardDaysStayed > 10) {
                discountRate = 0.20; // 20%
            } else if (standardDaysStayed > 5) {
                discountRate = 0.10; // 10%
            }
            double discountAmount = baseCost * discountRate;

            // Custom manual discount
            double customDiscountAmount = baseCost * (customDiscountRate / 100.0);

            // Late Fee
            long lateDays = actualDays - plannedDays;
            double lateFee = 0;
            if (lateDays > 0) {
                lateFee = lateDays * (r.getPrice() * 1.5); // 150% penalty per extra day
            }

            double finalTotal = baseCost - discountAmount - customDiscountAmount + lateFee;

            // Update UI
            lblCustomerName.setText(b.getCustomerName() != null ? b.getCustomerName() : "Customer ID: " + b.getCustomerId());
            lblRoomInfo.setText("Room " + r.getRoomId() + " (" + r.getRoomType() + ") - $" + r.getPrice() + "/day");
            lblDaysStayed.setText(actualDays + " (Planned: " + plannedDays + ")");
            lblBaseCost.setText(String.format("$%.2f", baseCost));
            lblDiscount.setText(String.format("-$%.2f", discountAmount));
            lblCustomDiscount.setText(String.format("-$%.2f (%.1f%%)", customDiscountAmount, customDiscountRate));
            lblLateFee.setText(String.format("+$%.2f", lateFee));
            lblTotal.setText(String.format("$%.2f", finalTotal));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) lblTotal.getScene().getWindow();
        stage.close();
    }
}
