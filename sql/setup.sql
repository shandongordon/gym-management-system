-- ELEVATE FITNESS — LOCAL CLASS DEMO ONLY
-- Copy this file to sql/setup.local.sql and replace CHANGE_THIS_PASSWORD
-- with a private, strong password BEFORE running. Never commit setup.local.sql.
-- Run as a MySQL admin: mysql -u root -p < sql/setup.local.sql
CREATE DATABASE IF NOT EXISTS elevate_fitness
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'elevate_app'@'localhost'
    IDENTIFIED BY 'CHANGE_THIS_PASSWORD';
-- Also updates the password if this demo user already exists.
ALTER USER 'elevate_app'@'localhost'
    IDENTIFIED BY 'CHANGE_THIS_PASSWORD';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
    ON elevate_fitness.* TO 'elevate_app'@'localhost';
-- Spring Data JPA creates the employees/members tables on first start.
-- Sample anime member records are inserted ONLY when members is empty.
