-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    role_type VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

-- Create permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    feature VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    path VARCHAR(255)
);

-- Create user_roles join table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create role_permissions join table
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Create departments table
CREATE TABLE IF NOT EXISTS departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)
);

-- Create designations table
CREATE TABLE IF NOT EXISTS designations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    department_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON role_permissions(permission_id);
CREATE INDEX IF NOT EXISTS idx_designations_department_id ON designations(department_id);
CREATE INDEX IF NOT EXISTS idx_departments_active ON departments(active);
CREATE INDEX IF NOT EXISTS idx_designations_active ON designations(active);

-- Create items table for inventory management
CREATE TABLE IF NOT EXISTS items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    sku VARCHAR(100) NOT NULL UNIQUE,
    price DECIMAL(10,2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create carts table for cart management
CREATE TABLE IF NOT EXISTS carts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create cart_items table for cart item management
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_time DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- Create indexes for inventory and cart management
CREATE UNIQUE INDEX IF NOT EXISTS idx_item_sku ON items(sku);
CREATE INDEX IF NOT EXISTS idx_items_active ON items(active);
CREATE INDEX IF NOT EXISTS idx_cart_user_status ON carts(user_id, status);
CREATE INDEX IF NOT EXISTS idx_cart_item_cart ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_item_item ON cart_items(item_id);

-- Create complaints table
CREATE TABLE IF NOT EXISTS complaints (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    category VARCHAR(20) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    priority_level VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample complaints
INSERT INTO complaints (type, full_name, email_address, phone_number, category, subject, priority_level, description, status, created_at, updated_at) VALUES
('COMPLAINT', 'John Smith', 'john.smith@email.com', '+1234567890', 'PAYMENT', 'Payment not processed', 'HIGH', 'My payment was deducted but order was not confirmed', 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('FEEDBACK', 'Sarah Johnson', 'sarah.j@email.com', '+1234567891', 'DELIVERY', 'Late delivery feedback', 'MEDIUM', 'Delivery was 2 days late but product quality was good', 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('COMPLAINT', 'Mike Wilson', 'mike.w@email.com', NULL, 'TECHNICAL', 'Website not loading', 'HIGH', 'Unable to access the website for the past 2 hours', 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SUGGESTION', 'Emma Davis', 'emma.d@email.com', '+1234567892', 'OTHER', 'Mobile app improvement', 'LOW', 'Please add dark mode feature to the mobile app', 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('COMPLAINT', 'Robert Brown', 'robert.b@email.com', '+1234567893', 'ACCOUNT', 'Cannot login to account', 'MEDIUM', 'Forgot password reset link is not working', 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('FEEDBACK', 'Lisa Garcia', 'lisa.g@email.com', NULL, 'DELIVERY', 'Excellent delivery service', 'LOW', 'Very satisfied with the quick delivery and packaging', 'RESOLVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('COMPLAINT', 'David Miller', 'david.m@email.com', '+1234567894', 'PAYMENT', 'Wrong amount charged', 'HIGH', 'Charged $150 instead of $100 for my order', 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SUGGESTION', 'Jennifer Taylor', 'jennifer.t@email.com', '+1234567895', 'TECHNICAL', 'Add search filters', 'MEDIUM', 'Please add more filter options in product search', 'OPEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('COMPLAINT', 'Christopher Lee', 'chris.l@email.com', NULL, 'DELIVERY', 'Damaged product received', 'HIGH', 'Product arrived with visible damage to packaging and contents', 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('FEEDBACK', 'Amanda White', 'amanda.w@email.com', '+1234567896', 'OTHER', 'Great customer service', 'LOW', 'Customer support team was very helpful and responsive', 'CLOSED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);