package com.hotel;

import com.hotel.dao.CustomerDAO;
import com.hotel.model.Customer;

public class TestDB {
    public static void main(String[] args) {
        try {
            System.out.println("Fetching customers...");
            var list = CustomerDAO.getAllCustomers();
            System.out.println("Count: " + list.size());
            for (Customer c : list) {
                System.out.println(" - " + c.getName() + " | " + c.getContact());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
