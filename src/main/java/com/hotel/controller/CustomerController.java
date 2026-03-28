package com.hotel.controller;

import com.hotel.dao.CustomerDAO;
import com.hotel.model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;

public class CustomerController {

    @FXML private TextField txtName;
    @FXML private TextField txtContact;
    @FXML private TableView<Customer> tblCustomers;

    @FXML private TableColumn<Customer, Integer> colCustomerId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colContact;

    @FXML
    public void initialize() {
        colCustomerId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCustomerId()));
        colName.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getName()));
        colContact.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getContact()));

        loadAllCustomers();
    }

    @FXML
    public void handleAddCustomer() {
        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();

        if (name.isEmpty() || contact.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Name and Contact fields cannot be empty.");
            return;
        }

        try {
            Customer customer = new Customer(0, name, contact); // ID is auto-increment
            CustomerDAO.addCustomer(customer);
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Customer added successfully!");
            clearFields();
            loadAllCustomers();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add customer: " + e.getMessage());
        }
    }

    @FXML
    public void loadAllCustomers() {
        try {
            List<Customer> customers = CustomerDAO.getAllCustomers();
            ObservableList<Customer> observableList = FXCollections.observableArrayList(customers);
            tblCustomers.setItems(observableList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load customers.");
        }
    }

    private void clearFields() {
        txtName.clear();
        txtContact.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
