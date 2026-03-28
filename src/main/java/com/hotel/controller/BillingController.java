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
            if (plannedDays == 0) plannedDays = 1;

            long actualDays = ChronoUnit.DAYS.between(b.getCheckInDate(), actualOut);
            if (actualDays == 0) actualDays = 1;
            
            // Base Cost calculation
            double baseCost = actualDays * r.getPrice();

            // Discount based on duration
            double discountRate = 0;
            if (actualDays > 10) {
                discountRate = 0.20; // 20%
            } else if (actualDays > 5) {
                discountRate = 0.10; // 10%
            }
            double durationDiscountAmount = baseCost * discountRate;

            // Custom manual discount
            double customDiscountAmount = baseCost * (customDiscountRate / 100.0);

            // Time Category Late Fee (Based on prompt constraints)
            double timePenaltyRate = 0.0;
            String timeCat = b.getActualCheckOutTimeCategory();
            if (timeCat != null) {
                if (timeCat.contains("2:00 PM - 6:00 PM")) {
                    timePenaltyRate = 0.5; // 50% of one night
                } else if (timeCat.contains("After 6:00 PM")) {
                    timePenaltyRate = 1.0; // 100% of one night
                }
            }
            double lateFee = timePenaltyRate * r.getPrice();

            double finalTotal = baseCost - durationDiscountAmount - customDiscountAmount + lateFee;

            // Update UI (Using ₹ heavily as requested)
            lblCustomerName.setText(b.getCustomerName() != null ? b.getCustomerName() : "Customer ID: " + b.getCustomerId());
            lblRoomInfo.setText("Room " + r.getRoomId() + " (" + r.getRoomType() + ") - ₹" + r.getPrice() + "/day");
            lblDaysStayed.setText(actualDays + " (Planned Limit: " + plannedDays + ")");
            lblBaseCost.setText(String.format("₹%.2f", baseCost));
            lblDiscount.setText(String.format("-₹%.2f (loyalty)", durationDiscountAmount));
            lblCustomDiscount.setText(String.format("-₹%.2f (%.1f%%)", customDiscountAmount, customDiscountRate));
            
            if (timePenaltyRate > 0) {
                lblLateFee.setText(String.format("+₹%.2f (%.0f%% room rate)", lateFee, timePenaltyRate * 100));
            } else {
                lblLateFee.setText("₹0.00 (No checkout delay penalty)");
            }
            
            lblTotal.setText(String.format("₹%.2f", finalTotal));

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
