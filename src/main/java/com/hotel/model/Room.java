package com.hotel.model;

public class Room {
    private int roomId;
    private String roomType;
    private double price;
    private boolean isAvailable;

    public Room(int roomId, String roomType, double price, boolean isAvailable) {
        this.roomId = roomId;
        this.roomType = roomType;
        this.price = price;
        this.isAvailable = isAvailable;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isIsAvailable() { return isAvailable; } // PropertyValueFactory uses isIsAvailable for boolean usually, but getIsAvailable or available is also good.
    public boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(boolean available) { isAvailable = available; }

    @Override
    public String toString() {
        return "Room " + roomId + " (" + roomType + ") - $" + price;
    }
}
