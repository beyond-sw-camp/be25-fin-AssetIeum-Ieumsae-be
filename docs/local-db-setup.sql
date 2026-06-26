CREATE DATABASE IF NOT EXISTS assetieum
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'assetieum'@'localhost' IDENTIFIED BY 'root';

GRANT ALL PRIVILEGES ON assetieum.* TO 'assetieum'@'localhost';

FLUSH PRIVILEGES;
