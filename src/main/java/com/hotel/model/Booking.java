package com.hotel.model;

import java.time.LocalDate;

public class Booking {
    private int bookingId;
    private int customerId;
    private int roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDate actualCheckOutDate;

    // Additional fields for displaying in TableViews (joined data)
    private String customerName;
    private String roomInfo;

    public Booking(int bookingId, int customerId, int roomId, LocalDate checkInDate, LocalDate checkOutDate, LocalDate actualCheckOutDate) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.actualCheckOutDate = actualCheckOutDate;
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public LocalDate getActualCheckOutDate() { return actualCheckOutDate; }
    public void setActualCheckOutDate(LocalDate actualCheckOutDate) { this.actualCheckOutDate = actualCheckOutDate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getRoomInfo() { return roomInfo; }
    public void setRoomInfo(String roomInfo) { this.roomInfo = roomInfo; }

    public String getStatus() {
        return (actualCheckOutDate == null) ? "Active" : "Completed";
    }
}
