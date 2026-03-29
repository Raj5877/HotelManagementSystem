package com.hotel.controller;

import com.hotel.dao.CustomerDAO;
import com.hotel.model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerController {

    @FXML private TextField txtName;
    @FXML private TextField txtContact;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cmbIdType;
    @FXML private TextField txtOtherIdType;
    @FXML private TextField txtIdNumber;
    @FXML private TextField txtSearch;

    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableColumn<Customer, Integer> colCustomerId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colContact;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colIdType;
    @FXML private TableColumn<Customer, String> colIdNumber;

    private ObservableList<Customer> allCustomers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colCustomerId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCustomerId()));
        colName.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getName()));
        colContact.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getContact()));
        colEmail.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getEmail()));
        colIdType.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getIdType()));
        colIdNumber.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getIdNumber()));

        // ID type dropdown
        cmbIdType.setItems(FXCollections.observableArrayList(
            "Aadhaar Card", "Driving License", "Passport", "PAN Card", "Others"
        ));

        // Show/hide "Other" text field based on selection
        cmbIdType.setOnAction(e -> {
            boolean isOther = "Others".equals(cmbIdType.getValue());
            if (txtOtherIdType != null) {
                txtOtherIdType.setVisible(isOther);
                txtOtherIdType.setManaged(isOther);
            }
        });

        if (txtOtherIdType != null) {
            txtOtherIdType.setVisible(false);
            txtOtherIdType.setManaged(false);
        }

        loadAllCustomers();
    }

    @FXML
    public void handleAddCustomer() {
        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();
        String email = txtEmail != null ? txtEmail.getText().trim() : "";
        String idTypeSelected = cmbIdType != null ? cmbIdType.getValue() : "";

        String idType = "";
        if (idTypeSelected != null) {
            if ("Others".equals(idTypeSelected)) {
                idType = txtOtherIdType != null ? txtOtherIdType.getText().trim() : "Others";
                if (idType.isEmpty()) idType = "Others";
            } else {
                idType = idTypeSelected;
            }
        }

        String idNumber = txtIdNumber != null ? txtIdNumber.getText().trim() : "";

        if (name.isEmpty() || contact.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Name and Contact fields are required.");
            return;
        }

        try {
            Customer customer = new Customer(0, name, contact, email, idType, idNumber);
            CustomerDAO.addCustomer(customer);
            showAlert(Alert.AlertType.INFORMATION, "Guest Registered", "'" + name + "' has been successfully registered.");
            clearFields();
            loadAllCustomers();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to register guest: " + e.getMessage());
        }
    }

    @FXML
    public void loadAllCustomers() {
        try {
            List<Customer> customers = CustomerDAO.getAllCustomers();
            allCustomers = FXCollections.observableArrayList(customers);
            tblCustomers.setItems(allCustomers);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load guests.");
        }
    }

    @FXML
    public void handleSearch() {
        if (txtSearch == null) return;
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            tblCustomers.setItems(allCustomers);
            return;
        }
        List<Customer> filtered = allCustomers.stream()
                .filter(c -> c.getName().toLowerCase().contains(query)
                          || c.getContact().toLowerCase().contains(query)
                          || c.getEmail().toLowerCase().contains(query))
                .collect(Collectors.toList());
        tblCustomers.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    public void clearFields() {
        if (txtName != null) txtName.clear();
        if (txtContact != null) txtContact.clear();
        if (txtEmail != null) txtEmail.clear();
        if (cmbIdType != null) cmbIdType.getSelectionModel().clearSelection();
        if (txtOtherIdType != null) { txtOtherIdType.clear(); txtOtherIdType.setVisible(false); txtOtherIdType.setManaged(false); }
        if (txtIdNumber != null) txtIdNumber.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
