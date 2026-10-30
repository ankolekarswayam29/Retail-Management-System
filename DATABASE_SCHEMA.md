# Retail Management System - Database Schema Design

## 1. Database Overview
Database name: `retail_management_db`
Engine: InnoDB (for transaction support)

## 2. ER Diagram Overview

```
┌─────────────┐       ┌─────────────┐
│    users    │       │    roles    │
└─────────────┘       └─────────────┘
       │                     │
       │ many-to-one         │
       ↓                     ↓
┌─────────────────────────────────────┐
│          products                    │
│  ┌──────────┐    ┌──────────────┐   │
│  │category  │    │  supplier    │   │
│  └──────────┘    └──────────────┘   │
└─────────────────────────────────────┘
       │                     │
       │                     │
       ↓                     ↓
┌─────────────┐       ┌─────────────┐
│    sales    │───────│  customers  │
└─────────────┘       └─────────────┘
       │
       │ one-to-many
       ↓
┌─────────────┐
│  sale_items │
└─────────────┘
       │
       │ triggers
       ↓
┌──────────────────┐
│stock_transactions│
└──────────────────┘
```

## 3. Table Definitions

### 3.1 roles Table
Stores user roles for authorization.

```sql
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role_name (name)
) ENGINE=InnoDB;
```

**Sample Data:**
- ADMIN (Full system access)
- STAFF (Limited access)

---

### 3.2 users Table
Stores user authentication information.

```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role_id INT NOT NULL,
    phone VARCHAR(20),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB;
```

**Notes:**
- Password stored as BCrypt hash
- role_id references roles table
- active flag for soft delete/disable

---

### 3.3 categories Table
Stores product categories.

```sql
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_name (name)
) ENGINE=InnoDB;
```

**Sample Data:**
- Electronics
- Clothing
- Groceries
- Home & Kitchen
- Sports

---

### 3.4 suppliers Table
Stores supplier information.

```sql
CREATE TABLE suppliers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(10),
    country VARCHAR(50) DEFAULT 'USA',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_supplier_name (name),
    INDEX idx_supplier_phone (phone)
) ENGINE=InnoDB;
```

---

### 3.5 products Table
Stores product information.

```sql
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category_id INT,
    supplier_id INT,
    purchase_price DECIMAL(10, 2) NOT NULL,
    selling_price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    minimum_stock INT DEFAULT 10,
    unit VARCHAR(20) DEFAULT 'pcs',
    image_path VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL,
    INDEX idx_product_code (product_code),
    INDEX idx_product_name (name),
    INDEX idx_category (category_id),
    INDEX idx_supplier (supplier_id),
    INDEX idx_stock (stock_quantity)
) ENGINE=InnoDB;
```

**Notes:**
- product_code: Unique identifier (e.g., PRD-001)
- minimum_stock: Threshold for low-stock alerts
- stock_quantity: Current available quantity
- purchase_price: Cost price from supplier
- selling_price: Price to customers

---

### 3.6 customers Table
Stores customer information.

```sql
CREATE TABLE customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(10),
    total_purchases DECIMAL(12, 2) DEFAULT 0.00,
    total_visits INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_code (customer_code),
    INDEX idx_customer_name (name),
    INDEX idx_customer_phone (phone)
) ENGINE=InnoDB;
```

**Notes:**
- customer_code: Unique identifier (e.g., CUST-001)
- total_purchases: Lifetime spending
- total_visits: Number of transactions

---

### 3.7 sales Table
Stores sales/billing information.

```sql
CREATE TABLE sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT,
    user_id INT NOT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(12, 2) NOT NULL,
    tax_amount DECIMAL(12, 2) DEFAULT 0.00,
    discount_amount DECIMAL(12, 2) DEFAULT 0.00,
    total_amount DECIMAL(12, 2) NOT NULL,
    payment_method VARCHAR(50) DEFAULT 'CASH',
    payment_status VARCHAR(20) DEFAULT 'PAID',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_invoice_number (invoice_number),
    INDEX idx_sale_date (sale_date),
    INDEX idx_customer (customer_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB;
```

**Notes:**
- invoice_number: Unique (e.g., INV-2024-001)
- payment_method: CASH, CARD, UPI, etc.
- payment_status: PAID, PENDING, REFUNDED

---

### 3.8 sale_items Table
Stores individual items in a sale.

```sql
CREATE TABLE sale_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    INDEX idx_sale (sale_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB;
```

**Notes:**
- quantity: Number of units sold
- unit_price: Selling price at time of sale
- total_price: quantity × unit_price
- ON DELETE RESTRICT on product_id to prevent deleting products with sales history

---

### 3.9 stock_transactions Table
Stores all stock movement history.

```sql
CREATE TABLE stock_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    previous_quantity INT NOT NULL,
    new_quantity INT NOT NULL,
    reference_type VARCHAR(50),
    reference_id INT,
    notes TEXT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_product (product_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;
```

**Notes:**
- transaction_type: PURCHASE, SALE, RETURN, ADJUSTMENT, DAMAGE
- reference_type: SALE, PURCHASE_ORDER, MANUAL
- reference_id: ID of related record
- Tracks complete stock history for auditing

---

## 4. Relationships Summary

### Many-to-One
- users → roles
- products → categories
- products → suppliers
- sales → customers
- sales → users
- sale_items → sales
- sale_items → products
- stock_transactions → products
- stock_transactions → users

### One-to-Many
- roles → users
- categories → products
- suppliers → products
- customers → sales
- users → sales
- sales → sale_items
- products → sale_items
- products → stock_transactions

---

## 5. Indexes Strategy

### Primary Indexes
- All tables have PRIMARY KEY on id

### Unique Indexes
- users.username, users.email
- categories.name
- products.product_code
- suppliers.phone (unique per supplier)
- customers.customer_code, customers.phone
- sales.invoice_number

### Foreign Key Indexes
- All foreign key columns indexed for JOIN performance

### Performance Indexes
- products.stock_quantity (for low-stock queries)
- sales.sale_date (for date range queries)
- stock_transactions.created_at (for history queries)

---

## 6. Data Integrity

### Constraints
- NOT NULL on critical fields
- UNIQUE on business keys
- FOREIGN KEY constraints for referential integrity
- CHECK constraints (application level for complex validation)

### Cascade Rules
- DELETE CASCADE: users→roles, sales→sale_items
- DELETE SET NULL: products→category/supplier, sales→customer
- DELETE RESTRICT: sale_items→products (preserve sales history)

---

## 7. Sample Data Insertion

```sql
-- Insert Roles
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Full system access'),
('STAFF', 'Limited access for daily operations');

-- Insert Categories
INSERT INTO categories (name, description) VALUES 
('Electronics', 'Electronic devices and accessories'),
('Clothing', 'Apparel and fashion items'),
('Groceries', 'Food and daily essentials'),
('Home & Kitchen', 'Household items and kitchenware');

-- Insert Admin User (password: admin123 - will be BCrypt hashed)
INSERT INTO users (username, password, email, full_name, role_id, phone) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 
 'admin@retailpro.com', 'System Administrator', 1, '1234567890');

-- Insert Sample Supplier
INSERT INTO suppliers (name, contact_person, email, phone, address, city, state) VALUES 
('TechSupply Co.', 'John Smith', 'john@techsupply.com', '555-0101', 
 '123 Tech Street', 'San Francisco', 'CA');
```

---

## 8. Database Initialization

The application will use:
- `schema.sql` for table creation (if needed)
- `data.sql` for sample data insertion
- Or Hibernate `ddl-auto=update` for automatic schema generation

---

## 9. Backup and Recovery Strategy

### Recommended Backup Strategy
- Daily full backups
- Hourly transaction log backups
- Weekly full backup with retention

### Recovery
- Point-in-time recovery using transaction logs
- Test restore procedures monthly

---

## 10. Security Considerations

### Database Security
- Use environment variables for credentials
- Restrict database user permissions (least privilege)
- Enable SSL for database connections (production)
- Regular security updates

### Application Security
- Never expose database errors to users
- Use parameterized queries (Hibernate handles this)
- Validate all inputs before database operations
- Use transactions for multi-step operations
