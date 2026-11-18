-- Run the whole script in a connected MySQL Workbench session as an administrator.
-- This local-development account is reset to echo_password each time this script runs.
CREATE DATABASE IF NOT EXISTS echo_waste
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'echo_user'@'localhost'
  IDENTIFIED BY 'echo_password';

ALTER USER 'echo_user'@'localhost'
  IDENTIFIED BY 'echo_password';

GRANT ALL PRIVILEGES ON echo_waste.* TO 'echo_user'@'localhost';

USE echo_waste;

SELECT DATABASE() AS selected_database;
SHOW GRANTS FOR 'echo_user'@'localhost';