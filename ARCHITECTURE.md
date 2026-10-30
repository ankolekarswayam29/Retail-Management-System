# Retail Management System - Architecture Design

## 1. Project Overview
A full-stack retail management system built with Spring Boot and MySQL to help retail shop owners manage daily business activities including inventory, billing, customers, suppliers, and sales reporting.

## 2. Technology Stack

### Backend
- **Java 21**: Modern Java with latest features
- **Spring Boot 3.2**: Framework for building production-ready applications
- **Spring MVC**: Web framework for REST APIs
- **Spring Data JPA**: Database abstraction layer
- **Spring Security**: Authentication and authorization
- **Hibernate ORM**: Object-relational mapping
- **Maven**: Dependency management and build tool

### Frontend
- **HTML5/CSS3**: Structure and styling
- **JavaScript**: Client-side logic
- **Bootstrap 5**: Responsive UI framework
- **Chart.js**: Data visualization and analytics

### Database
- **MySQL 8.0**: Relational database

## 3. Layered MVC Architecture

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Controllers + Thymeleaf Templates)   │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│         Business Logic Layer            │
│         (Service Classes)               │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│         Data Access Layer               │
│         (Repository Interfaces)         │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│         Database Layer                  │
│         (MySQL + Hibernate)             │
└─────────────────────────────────────────┘
```

## 4. Package Structure

```
com.retailpro.retailmanagementsystem/
├── RetailManagementSystemApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── WebConfig.java
├── controller/
│   ├── AuthController.java
│   ├── DashboardController.java
│   ├── ProductController.java
│   ├── InventoryController.java
│   ├── CategoryController.java
│   ├── SupplierController.java
│   ├── CustomerController.java
│   ├── BillingController.java
│   ├── SalesController.java
│   └── ReportController.java
├── service/
│   ├── UserService.java
│   ├── ProductService.java
│   ├── InventoryService.java
│   ├── CategoryService.java
│   ├── SupplierService.java
│   ├── CustomerService.java
│   ├── BillingService.java
│   ├── SalesService.java
│   └── ReportService.java
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── ProductRepository.java
│   ├── CategoryRepository.java
│   ├── SupplierRepository.java
│   ├── CustomerRepository.java
│   ├── SaleRepository.java
│   ├── SaleItemRepository.java
│   └── StockTransactionRepository.java
├── entity/
│   ├── User.java
│   ├── Role.java
│   ├── Product.java
│   ├── Category.java
│   ├── Supplier.java
│   ├── Customer.java
│   ├── Sale.java
│   ├── SaleItem.java
│   └── StockTransaction.java
├── dto/
│   ├── LoginRequest.java
│   ├── ProductDTO.java
│   ├── BillItemDTO.java
│   └── SaleReportDTO.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── InsufficientStockException.java
│   └── GlobalExceptionHandler.java
└── security/
    ├── JwtTokenProvider.java (if using JWT)
    └── CustomUserDetailsService.java
```

## 5. Frontend Structure

```
src/main/resources/
├── templates/
│   ├── login.html
│   ├── layout/
│   │   ├── header.html
│   │   ├── sidebar.html
│   │   └── footer.html
│   ├── dashboard.html
│   ├── product/
│   │   ├── list.html
│   │   ├── form.html
│   │   └── details.html
│   ├── inventory/
│   │   ├── list.html
│   │   └── transactions.html
│   ├── category/
│   │   ├── list.html
│   │   └── form.html
│   ├── supplier/
│   │   ├── list.html
│   │   └── form.html
│   ├── customer/
│   │   ├── list.html
│   │   └── form.html
│   ├── billing/
│   │   ├── create.html
│   │   └── invoice.html
│   ├── sales/
│   │   └── list.html
│   └── reports/
│       ├── sales.html
│       └── inventory.html
└── static/
    ├── css/
    │   ├── style.css
    │   └── dashboard.css
    ├── js/
    │   ├── app.js
    │   ├── billing.js
    │   └── charts.js
    └── images/
        └── (product images)
```

## 6. Security Architecture

### Authentication Flow
1. User submits login credentials
2. Spring Security validates credentials
3. BCrypt verifies hashed password
4. Session created and maintained
5. User redirected based on role

### Authorization
- **ADMIN**: Full access to all modules
- **STAFF**: Limited access (products, billing, sales)

### Protected Routes
- All pages except login require authentication
- Role-based access control for sensitive operations

## 7. Database Transaction Management

### Critical Transactions
- **Billing Process**: 
  - Create sale record
  - Create sale items
  - Update inventory stock
  - All operations in single transaction

- **Stock Updates**:
  - Record stock transaction
  - Update product quantity
  - Atomic operation to prevent data inconsistency

## 8. API Design Patterns

### RESTful Endpoints
- GET /api/products - List all products
- POST /api/products - Create new product
- PUT /api/products/{id} - Update product
- DELETE /api/products/{id} - Delete product
- GET /api/products/{id} - Get product details

### Response Format
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful",
  "timestamp": "2024-01-01T12:00:00"
}
```

## 9. Error Handling

### Global Exception Handler
- ResourceNotFoundException (404)
- InsufficientStockException (400)
- ValidationException (400)
- AuthenticationException (401/403)
- GlobalExceptionHandler (500)

## 10. Key Design Decisions

### Why Thymeleaf?
- Server-side rendering for better SEO
- Natural templates (HTML-like syntax)
- Easy integration with Spring Boot
- Good for form handling

### Why Spring Security with BCrypt?
- Industry-standard security
- Built-in session management
- Role-based authorization
- Secure password hashing

### Why JPA/Hibernate?
- Automatic ORM mapping
- Database-agnostic code
- Built-in caching
- Transaction management

### Why Bootstrap 5?
- Responsive design out-of-the-box
- Professional UI components
- Easy to customize
- Good documentation

## 11. Performance Considerations

- Database indexes on frequently queried columns
- Lazy loading for entity relationships
- Pagination for large datasets
- Caching for dashboard statistics
- Optimized queries with @Query annotations

## 12. Scalability

- Service layer allows easy business logic updates
- Repository layer enables database changes
- DTOs separate API from entity models
- Configuration externalized in application.properties
