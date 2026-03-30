-- ════════════════════════════════════════════════════
--  Aurum Hotel Management System — Database Schema
--  Run this script to (re)initialize the database.
-- ════════════════════════════════════════════════════

DROP DATABASE IF EXISTS hotel_management;
CREATE DATABASE hotel_management;
USE hotel_management;

DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS room_types;
DROP TABLE IF EXISTS customers;

-- ── Room Categories ──────────────────────────────────
CREATE TABLE IF NOT EXISTS room_types (
    type_id    INT PRIMARY KEY AUTO_INCREMENT,
    type_name  VARCHAR(80) UNIQUE NOT NULL,
    description TEXT,
    base_price DOUBLE NOT NULL
);

INSERT IGNORE INTO room_types (type_id, type_name, description, base_price) VALUES
(1, 'Standard Single',   'Cozy room for solo travellers. AC, TV, WiFi, attached bath.', 1500),
(2, 'Standard Double',   'Comfortable room for two. AC, TV, WiFi, minibar.', 2500),
(3, 'Deluxe Double',     'Upgraded furnishings. King bed, AC, Smart TV, work desk, minibar.', 3800),
(4, 'Executive Suite',   'Spacious suite with living area. King bed, jacuzzi, lounge, kitchenette.', 6500),
(5, 'Presidential Suite','Our flagship suite. Panoramic view, private butler, full kitchen, dining room.', 12000);

-- ── Rooms ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rooms (
    room_id      INT PRIMARY KEY,
    type_id      INT NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (type_id) REFERENCES room_types(type_id)
);

INSERT IGNORE INTO rooms (room_id, type_id, is_available) VALUES
-- Ground Floor (Standard Singles)
(101, 1, true), (102, 1, true), (103, 1, true),
-- 1st Floor (Standard Doubles)
(201, 2, true), (202, 2, true), (203, 2, true),
-- 2nd Floor (Deluxe)
(301, 3, true), (302, 3, true),
-- 3rd Floor (Executive Suites)
(401, 4, true), (402, 4, true),
-- Penthouse
(501, 5, true);

-- ── Customers / Guests ────────────────────────────────
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    contact     VARCHAR(20)  NOT NULL,
    email       VARCHAR(120) DEFAULT '',
    id_proof    VARCHAR(60)  DEFAULT ''
);

INSERT IGNORE INTO customers (customer_id, name, contact, email, id_proof) VALUES
(1, 'Arjun Mehta',   '+91-98765-43210', 'arjun.mehta@gmail.com',    'Aadhaar 1234 5678 9012'),
(2, 'Priya Sharma',  '+91-87654-32109', 'priya.sharma@outlook.com', 'Passport Z1234567'),
(3, 'Rohan Verma',   '+91-76543-21098', 'rohan.v@yahoo.com',        'DL MH-01-2019-00123');

-- ── Bookings ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bookings (
    booking_id                   INT PRIMARY KEY AUTO_INCREMENT,
    customer_id                  INT NOT NULL,
    room_id                      INT NOT NULL,
    check_in_date                DATE NOT NULL,
    check_out_date               DATE NOT NULL,
    original_check_out_date      DATE NOT NULL,
    actual_check_out_date        DATE,
    actual_check_out_time_category VARCHAR(20),
    -- Allowed values: 'BEFORE_12PM', '12PM_TO_2PM', '2PM_TO_6PM', 'AFTER_6PM'
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (room_id)     REFERENCES rooms(room_id)
);
