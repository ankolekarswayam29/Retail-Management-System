# Retail Management System

A comprehensive, full-stack retail management application built with Spring Boot, MySQL, and modern web technologies. This system helps retail shop owners manage their daily business activities including inventory, billing, customers, suppliers, and sales reporting.

## 🚀 Features

### Core Modules

- **Authentication & User Management**
  - Secure login with BCrypt password encryption
  - Role-based access control (ADMIN, STAFF)
  - Session management
  - Protected routes

- **Admin Dashboard**
  - Real-time statistics (products, customers, suppliers, sales)
  - Today's and monthly revenue tracking
  - Low-stock alerts
  - Recent transactions
  - Interactive sales charts with Chart.js

- **Product Management**
  - Add, view, update, and delete products
  - Search and filter products
  - Category-based organization
  - Supplier tracking
  - Purchase and selling price management
  - Stock quantity tracking
  - Product image support

- **Inventory Management**
  - Real-time stock tracking
  - Stock adjustment functionality
  - Low-stock alerts
  - Out-of-stock notifications
  - Complete stock transaction history
  - Automatic stock updates after sales

- **Category Management**
  - Create and manage product categories
  - Category-based product filtering

- **Supplier Management**
  - Supplier information management
  - Contact details and address
  - Product-supplier relationships

- **Customer Management**
  - Customer registration and management
  - Purchase history tracking
  - Customer statistics (total purchases, visits)
  - Search and filter customers

- **Billing & Invoice Management**
  - Create customer bills
  - Add multiple products to a bill
  - Automatic quantity and price calculations
  - Tax and discount support
  - Unique invoice number generation
  - Printable invoices
  - Automatic inventory updates

- **Sales Management**
  - View all sales transactions
  - Daily and monthly sales views
  - Date range filtering
  - Revenue calculations

- **Reports & Analytics**
  - Daily, weekly, and monthly sales reports
  - Revenue analysis
  - Inventory reports
  - Low-stock reports
  - Graphical reports with Chart.js

## 🛠️ Technology Stack

### Backend
- **Java 21** - Modern Java with latest features
- **Spring Boot 3.2** - Application framework
- **Spring MVC** - Web framework
- **Spring Data JPA** - Database abstraction
- **Spring Security** - Authentication and authorization
- **Hibernate ORM** - Object-relational mapping
- **Maven** - Dependency management
- **MySQL 8.0** - Relational database

### Frontend
- **HTML5/CSS3** - Structure and styling
- **JavaScript** - Client-side logic
- **Bootstrap 5** - Responsive UI framework
- **Chart.js** - Data visualization
- **Thymeleaf** - Server-side templating

## 📋 Prerequisites

Before running this application, ensure you have the following installed:

- **Java 21** or higher
- **Maven 3.6+** or use the Maven Wrapper included
- **MySQL 8.0+** or MariaDB
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

## 🗄️ Database Setup

### 1. Install MySQL

Download and install MySQL from [https://dev.mysql.com/downloads/mysql/](https://dev.mysql.com/downloads/mysql/)

### 2. Create Database

The application will automatically create the database `retail_management_db` on first run if configured correctly. However, you can also create it manually:

```sql
CREATE DATABASE retail_management_db;
```

### 3. Update Database Credentials

Edit `src/main/resources/application.properties` and update the database credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/retail_management_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
```

**⚠️ Important:** Replace `your_password` with your actual MySQL password.

## 🚀 Installation & Setup

### 1. Clone or Download the Project

```bash
git clone https://github.com/ankolekarswayam29/Retail-Management-System.git
cd Retail-Management-System
```

### 2. Build the Project

Using Maven Wrapper (recommended):

```bash
# On Windows
mvnw.cmd clean install

# On Linux/Mac
./mvnw clean install
```

Or using system Maven:

```bash
mvn clean install
```

### 3. Run the Application

Using Maven:

```bash
# On Windows
mvnw.cmd spring-boot:run

# On Linux/Mac
./mvnw spring-boot:run
```

Or using system Maven:

```bash
mvn spring-boot:run
```

Alternatively, you can run the JAR file after building:

```bash
java -jar target/retail-management-system-1.0.0.jar
```

### 4. Access the Application

Once the application starts successfully, open your browser and navigate to:

```
http://localhost:8080
```

## 🔐 Default Login Credentials

Initial Admin Setup: Configure the administrator credentials using the environment variables supported by the application. Change the initial password immediately after setup

## 📁 Project Structure

```
RetailPro/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/retailpro/retailmanagementsystem/
│   │   │       ├── config/           # Security and configuration
│   │   │       ├── controller/       # REST controllers
│   │   │       ├── entity/           # JPA entities
│   │   │       ├── repository/       # Data access layer
│   │   │       ├── service/          # Business logic layer
│   │   │       ├── security/         # Security components
│   │   │       └── RetailManagementSystemApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/              # Stylesheets
│   │       │   ├── js/               # JavaScript files
│   │       │   └── images/           # Images
│   │       ├── templates/            # Thymeleaf templates
│   │       │   ├── billing/
│   │       │   ├── category/
│   │       │   ├── customer/
│   │       │   ├── inventory/
│   │       │   ├── layout/
│   │       │   ├── product/
│   │       │   ├── reports/
│   │       │   ├── sales/
│   │       │   ├── supplier/
│   │       │   ├── dashboard.html
│   │       │   └── login.html
│   │       └── application.properties
├── pom.xml
├── ARCHITECTURE.md
├── DATABASE_SCHEMA.md
└── README.md
```

## 🎯 Usage Guide

### 1. First-Time Setup

1. **Login** using the default credentials
2. **Create Categories** - Add product categories (Electronics, Clothing, etc.)
3. **Add Suppliers** - Register your suppliers
4. **Add Products** - Add products to your inventory
5. **Register Customers** - Add customer information

### 2. Daily Operations

- **Create Bills** - Use the billing module to create sales
- **Manage Inventory** - Adjust stock levels as needed
- **View Sales** - Monitor daily and monthly sales
- **Check Reports** - Review sales and inventory reports

### 3. Inventory Management

- Navigate to **Inventory** from the sidebar
- View all products with current stock levels
- Adjust stock quantities
- View low-stock and out-of-stock items
- Check stock transaction history

### 4. Creating a Sale

1. Go to **Sales → New Bill**
2. Select a customer (or leave as walk-in)
3. Add products by selecting from the dropdown
4. Enter quantities
5. Add tax and discount if applicable
6. Click "Complete Sale"
7. View and print the invoice

## 🔧 Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Server Port
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/retail_management_db
spring.datasource.username=root
spring.datasource.password=your_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# File Upload
spring.servlet.multipart.max-file-size=10MB
```

### Security Configuration

Security settings in `SecurityConfig.java`:

- Login page: `/login`
- Default redirect after login: `/dashboard`
- Logout: `/logout`
- Protected routes: All except `/login`, `/css/**`, `/js/**`, `/images/**`

## 🧪 Testing

### Running Tests

```bash
mvn test
```

### Test Coverage

The application includes:
- Repository tests
- Service layer tests
- Controller tests
- Security tests

## 🐛 Troubleshooting

### Common Issues

**1. Database Connection Error**
```
Communications link failure
```
**Solution:** Ensure MySQL is running and credentials are correct in `application.properties`

**2. Port Already in Use**
```
Port 8080 is already in use
```
**Solution:** Change the port in `application.properties`:
```properties
server.port=8081
```

**3. Java Version Mismatch**
```
Unsupported class file major version
```
**Solution:** Ensure you're using Java 21 or higher

**4. Maven Build Failure**
```
Could not resolve dependencies
```
**Solution:** Run `mvn clean install -U` to force update dependencies

## 📊 Database Schema

The application uses the following tables:

- `users` - User accounts
- `roles` - User roles
- `products` - Product inventory
- `categories` - Product categories
- `suppliers` - Supplier information
- `customers` - Customer information
- `sales` - Sales transactions
- `sale_items` - Individual sale items
- `stock_transactions` - Stock movement history

For detailed schema information, see `DATABASE_SCHEMA.md`.

## 🏗️ Architecture

The application follows a clean layered MVC architecture:

- **Presentation Layer** - Controllers and Thymeleaf templates
- **Business Logic Layer** - Service classes
- **Data Access Layer** - Repository interfaces
- **Database Layer** - MySQL with Hibernate ORM

For detailed architecture information, see `ARCHITECTURE.md`.

## 🔒 Security Features

- BCrypt password hashing
- Role-based access control
- Session management
- CSRF protection
- SQL injection prevention (via JPA/Hibernate)
- Input validation

## 📈 Performance Considerations

- Database indexes on frequently queried columns
- Lazy loading for entity relationships
- Pagination for large datasets
- Optimized queries with @Query annotations
- Caching for dashboard statistics

## 🤝 Contributing

This is a learning project. Feel free to:
- Report bugs
- Suggest new features
- Improve documentation
- Submit pull requests

## 📝 License

This project is created for educational purposes.

## 👥 Author

Developed as a comprehensive retail management system for learning and practical use.

## 📞 Support

For issues or questions:
1. Check the Troubleshooting section
2. Review the ARCHITECTURE.md and DATABASE_SCHEMA.md files
3. Ensure all prerequisites are met

## 🎉 Acknowledgments

- Spring Boot team for the excellent framework
- Bootstrap for the UI framework
- Chart.js for data visualization
- The open-source community

---

**Version:** 1.0.0  
**Last Updated:** 2026  
**Java Version:** 21  
**Spring Boot Version:** 3.2.0
