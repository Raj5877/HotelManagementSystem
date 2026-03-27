package com.hotel.model;

public class Room {
    private int roomId;
    private int typeId;
    private boolean isAvailable;

    // Transient fields joined from room_types for UI
    private String roomTypeName;
    private double price;

    public Room(int roomId, int typeId, boolean isAvailable) {
        this.roomId = roomId;
        this.typeId = typeId;
        this.isAvailable = isAvailable;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }

    public boolean isIsAvailable() { return isAvailable; }
    public boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(boolean available) { isAvailable = available; }

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // Alias for PropertyValueFactory to match old "roomType" binding if needed
    public String getRoomType() { return getRoomTypeName(); }

    @Override
    public String toString() {
        return "Room " + roomId + " (" + roomTypeName + ") - ₹" + price;
    }
}
