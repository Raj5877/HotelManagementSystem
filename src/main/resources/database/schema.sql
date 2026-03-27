CREATE DATABASE IF NOT EXISTS hotel_management;
USE hotel_management;

CREATE TABLE IF NOT EXISTS room_types (
    type_id INT PRIMARY KEY AUTO_INCREMENT,
    type_name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    base_price DOUBLE NOT NULL
);

-- Insert some default room types
INSERT IGNORE INTO room_types (type_id, type_name, description, base_price) VALUES
(1, 'Standard Single', 'Basic room for one person.', 1500),
(2, 'Standard Double', 'Basic room for two people.', 2500),
(3, 'Luxury Suite', 'Premium room with all amenities.', 5000);

CREATE TABLE IF NOT EXISTS rooms (
    room_id INT PRIMARY KEY,
    type_id INT NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (type_id) REFERENCES room_types(type_id)
);

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    contact VARCHAR(15) NOT NULL
);

CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    actual_check_out_date DATE,
    actual_check_out_time_category VARCHAR(20), -- 'BEFORE_12PM', '12PM_TO_2PM', '2PM_TO_6PM', 'AFTER_6PM'
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);
