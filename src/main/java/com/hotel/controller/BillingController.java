package com.hotel.controller;

import com.hotel.dao.RoomDAO;
import com.hotel.model.Booking;
import com.hotel.model.Room;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class BillingController {

    @FXML private Label lblInvoiceNo;
    @FXML private Label lblCustomerName;
    @FXML private Label lblRoomInfo;
    @FXML private Label lblDaysStayed;
    @FXML private Label lblRoomRate;
    @FXML private Label lblBaseCost;
    @FXML private Label lblCustomDiscount;
    @FXML private Label lblLateFee;
    @FXML private Label lblGst;
    @FXML private Label lblTotal;

    private static final double GST_RATE = 0.12;

    // Store data for receipt printing
    private String receiptText = "";

    public void calculateAndSetBill(Booking b, LocalDate actualOut, double customDiscountRate) {
        try {
            Room r = RoomDAO.getRoomById(b.getRoomId());
            if (r == null) return;

            long actualDays = ChronoUnit.DAYS.between(b.getCheckInDate(), actualOut);
            if (actualDays == 0) actualDays = 1;
            long plannedDays = ChronoUnit.DAYS.between(b.getCheckInDate(), b.getCheckOutDate());
            if (plannedDays == 0) plannedDays = 1;

            double pricePerNight = r.getPrice();
            double baseCost = actualDays * pricePerNight;

            // Custom manual discount only (loyalty discount removed)
            double customDiscount = baseCost * (customDiscountRate / 100.0);

            // Late checkout fee
            double lateFeeRate = 0.0;
            String timeCat = b.getActualCheckOutTimeCategory();
            if (timeCat != null) {
                if (timeCat.equals("2PM_TO_6PM")) lateFeeRate = 0.50;
                else if (timeCat.equals("AFTER_6PM")) lateFeeRate = 1.00;
                else if (timeCat.equals("12PM_TO_2PM")) lateFeeRate = 0.25;
            }
            double lateFee = lateFeeRate * pricePerNight;

            double subTotal = baseCost - customDiscount + lateFee;
            double gst = subTotal * GST_RATE;
            double finalTotal = subTotal + gst;

            // Invoice number
            String invoiceNo = "INV-" + String.format("%04d", b.getBookingId())
                    + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("MMdd"));
            if (lblInvoiceNo != null) lblInvoiceNo.setText(invoiceNo);

            // Guest & Room
            String guestName = b.getCustomerName() != null ? b.getCustomerName() : "Guest #" + b.getCustomerId();
            String roomInfo = "Room " + r.getRoomId() + "  |  " + r.getRoomTypeName();
            lblCustomerName.setText(guestName);
            lblRoomInfo.setText(roomInfo);

            // Breakdown
            String daysText = actualDays + " night(s)  [Planned: " + plannedDays + "]";
            String rateText = String.format("₹%.2f / night", pricePerNight);
            String baseText = String.format("₹%.2f", baseCost);
            String discountText = customDiscountRate > 0
                    ? String.format("-₹%.2f  (%.1f%% applied)", customDiscount, customDiscountRate)
                    : "₹0.00";
            String lateFeeText = lateFeeRate > 0
                    ? String.format("+₹%.2f  (%.0f%% of night rate)", lateFee, lateFeeRate * 100)
                    : "₹0.00  (Standard checkout)";
            String gstText = String.format("₹%.2f  (12%%)", gst);
            String totalText = String.format("₹%.2f", finalTotal);

            lblDaysStayed.setText(daysText);
            if (lblRoomRate != null) lblRoomRate.setText(rateText);
            lblBaseCost.setText(baseText);
            lblCustomDiscount.setText(discountText);
            lblLateFee.setText(lateFeeText);
            if (lblGst != null) lblGst.setText(gstText);
            lblTotal.setText(totalText);

            // Build receipt text for file saving
            receiptText = buildReceiptText(invoiceNo, guestName, roomInfo,
                    daysText, rateText, baseText, discountText, lateFeeText, gstText, totalText,
                    b.getCheckInDate(), actualOut);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String buildReceiptText(String invoiceNo, String guestName, String roomInfo,
                                     String days, String rate, String base, String discount,
                                     String lateFee, String gst, String total,
                                     LocalDate checkIn, LocalDate checkOut) {
        String line = "=".repeat(52);
        String thin = "-".repeat(52);
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return line + "\n" +
               "          ✦  AURUM HOTEL MANAGEMENT SUITE\n" +
               "                  TAX INVOICE\n" +
               line + "\n\n" +
               "  Invoice No. : " + invoiceNo + "\n" +
               "  Printed On  : " + ts + "\n\n" +
               thin + "\n" +
               "  GUEST  : " + guestName + "\n" +
               "  ROOM   : " + roomInfo + "\n" +
               thin + "\n\n" +
               "  Duration           : " + days + "\n" +
               "  Room Rate          : " + rate + "\n" +
               "  Base Amount        : " + base + "\n" +
               "  Additional Discount: " + discount + "\n" +
               "  Late Checkout Fee  : " + lateFee + "\n" +
               "  GST (12%)          : " + gst + "\n\n" +
               line + "\n" +
               "  TOTAL PAYABLE      : " + total + "\n" +
               line + "\n\n" +
               "       Thank you for choosing Aurum.\n" +
               "           We hope to see you again!\n\n" +
               line + "\n";
    }

    @FXML
    public void handlePrint() {
        if (receiptText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nothing to Print", "No invoice data to save.");
            return;
        }

        try {
            // Determine receipts folder relative to the running JAR / project root
            String receiptsPath = getReceiptsFolder();
            File folder = new File(receiptsPath);
            if (!folder.exists()) folder.mkdirs();

            // File name: invoice number + timestamp
            String invoiceNo = lblInvoiceNo != null ? lblInvoiceNo.getText() : "INV";
            String fileName = invoiceNo + "_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
            File receiptFile = new File(folder, fileName);

            try (FileWriter fw = new FileWriter(receiptFile)) {
                fw.write(receiptText);
            }

            showAlert(Alert.AlertType.INFORMATION, "Receipt Saved",
                    "Receipt saved successfully:\n" + receiptFile.getAbsolutePath());

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Failed", "Could not save receipt: " + e.getMessage());
        }
    }

    private String getReceiptsFolder() {
        // Try to resolve the receipts folder relative to the project/JAR location
        String jarDir = "";
        try {
            File jarFile = new File(
                getClass().getProtectionDomain().getCodeSource().getLocation().toURI()
            );
            File parent = jarFile.getParentFile();
            // Walk up to find the project root (has pom.xml or receipts folder)
            while (parent != null) {
                if (new File(parent, "receipts").exists() || new File(parent, "pom.xml").exists()) {
                    jarDir = parent.getAbsolutePath();
                    break;
                }
                parent = parent.getParentFile();
            }
        } catch (Exception ignored) {}

        if (!jarDir.isEmpty()) {
            return jarDir + File.separator + "receipts";
        }
        // Fallback: user home directory
        return System.getProperty("user.home") + File.separator + "AurumReceipts";
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) lblTotal.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
