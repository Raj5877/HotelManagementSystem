package com.hotel.model;

public class Customer {
    private int customerId;
    private String name;
    private String contact;
    private String email;
    private String idType;    // e.g. "Aadhaar Card", "Passport", etc.
    private String idNumber;  // the actual ID number

    public Customer(int customerId, String name, String contact) {
        this.customerId = customerId;
        this.name = name;
        this.contact = contact;
        this.email = "";
        this.idType = "";
        this.idNumber = "";
    }

    public Customer(int customerId, String name, String contact, String email, String idType, String idNumber) {
        this.customerId = customerId;
        this.name = name;
        this.contact = contact;
        this.email = email != null ? email : "";
        this.idType = idType != null ? idType : "";
        this.idNumber = idNumber != null ? idNumber : "";
    }

    // Legacy constructor for backward compat (idProof stored as "type|number")
    public Customer(int customerId, String name, String contact, String email, String idProof) {
        this.customerId = customerId;
        this.name = name;
        this.contact = contact;
        this.email = email != null ? email : "";
        if (idProof != null && idProof.contains("|")) {
            String[] parts = idProof.split("\\|", 2);
            this.idType = parts[0];
            this.idNumber = parts[1];
        } else {
            this.idType = "";
            this.idNumber = idProof != null ? idProof : "";
        }
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email : ""; }

    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType != null ? idType : ""; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber != null ? idNumber : ""; }

    /** For backward compat – stored as "type|number" in a single column */
    public String getIdProof() {
        if (idType.isEmpty() && idNumber.isEmpty()) return "";
        return idType + "|" + idNumber;
    }

    /** Display string shown in table: "Aadhaar Card: XXXX" */
    public String getIdDisplay() {
        if (idType.isEmpty() && idNumber.isEmpty()) return "";
        if (idType.isEmpty()) return idNumber;
        if (idNumber.isEmpty()) return idType;
        return idType + ": " + idNumber;
    }

    @Override
    public String toString() {
        return name + " (" + contact + ")";
    }
}
