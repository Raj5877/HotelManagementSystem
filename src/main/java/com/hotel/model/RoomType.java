package com.hotel.model;

public class RoomType {
    private int typeId;
    private String typeName;
    private String description;
    private double basePrice;

    public RoomType(int typeId, String typeName, String description, double basePrice) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.description = description;
        this.basePrice = basePrice;
    }

    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    @Override
    public String toString() {
        return typeName + " (₹" + basePrice + ")";
    }
}
