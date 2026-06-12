CREATE DATABASE IF NOT EXISTS mahjong_db;
USE mahjong_db;

CREATE TABLE IF NOT EXISTS users (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    score INT DEFAULT 25000,
    wins INT DEFAULT 0,
    losses INT DEFAULT 0,
    register_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
