-- Create databases for different services
CREATE DATABASE catalog_db;
CREATE DATABASE cart_db;
CREATE DATABASE order_db;
CREATE DATABASE security_db;

-- Create users for each service (optional, can use postgres user for simplicity)
-- CREATE USER catalog_user WITH PASSWORD 'catalog_password';
-- CREATE USER cart_user WITH PASSWORD 'cart_password';
-- CREATE USER order_user WITH PASSWORD 'order_password';
-- CREATE USER security_user WITH PASSWORD 'security_password';

-- Grant privileges
-- GRANT ALL PRIVILEGES ON DATABASE catalog_db TO catalog_user;
-- GRANT ALL PRIVILEGES ON DATABASE cart_db TO cart_user;
-- GRANT ALL PRIVILEGES ON DATABASE order_db TO order_user;
-- GRANT ALL PRIVILEGES ON DATABASE security_db TO security_user;