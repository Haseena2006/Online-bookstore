# Online Bookstore

A full-stack online bookstore application that allows users to browse books, search by title or author, manage their shopping cart, and place orders.

The application is built using **Vanilla HTML, CSS, and JavaScript** for the frontend, **Spring Web MVC** for the backend, **Spring JDBC (JdbcTemplate)** for database operations, and **PostgreSQL** for data storage.

---

## Project Overview

Page & Co. provides a simple online shopping experience for book lovers. Users can explore available books, view book details, create an account, log in, add books to their cart, and place orders using Cash on Delivery.

The backend follows a layered architecture with controllers, services, repositories, models, DTOs, and exception handling.

## Features

### User Features
- Browse all available books.
- Search books by title or author.
- Filter books by category.
- Sort books by price.
- View featured, popular, and newly added books.
- Register and log in securely.
- Manage shopping cart.
- Update or remove cart items.
- Place orders using Cash on Delivery.
- View personal order history.

### Backend Features
- RESTful API endpoints.
- Spring Web MVC architecture.
- Database operations using Spring JDBC and JdbcTemplate.
- Prepared statements for database queries.
- BCrypt password hashing.
- HTTP session-based authentication.
- Input validation and centralized exception handling.
- Transactional checkout process.
- Stock validation and stock updates during checkout.
- PostgreSQL database integration.

## Technologies Used

| Category | Technologies |
|---|---|
| Frontend | HTML5, CSS3, JavaScript |
| Backend | Java 17, Spring Web MVC |
| Database | PostgreSQL |
| Database Access | Spring JDBC, JdbcTemplate |
| Security | BCrypt, Spring Security Crypto |
| Server | Apache Tomcat 10.1+ |
| Build Tool | Maven |
| API Testing | Postman |
| IDE | IntelliJ IDEA |
| Version Control | Git and GitHub |

## Application Architecture

The application follows a layered architecture:

```text
Frontend (HTML / CSS / JavaScript)
                 |
                 v
          Spring Web MVC
                 |
                 v
             Controller
                 |
                 v
               Service
                 |
                 v
             Repository
                 |
                 v
          Spring JDBC / JdbcTemplate
                 |
                 v
             PostgreSQL
```

## Project Structure

```text
online-bookstore/
│
├── database/
│   └── bookstore.sql
│
├── postman/
│   └── online-bookstore.postman_collection.json
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/bookstore/
│       │       ├── config/
│       │       ├── controller/
│       │       ├── service/
│       │       ├── repository/
│       │       ├── model/
│       │       ├── dto/
│       │       └── exception/
│       │
│       ├── resources/
│       │   └── application.properties
│       │
│       └── webapp/
│           ├── css/
│           ├── js/
│           ├── images/
│           └── HTML pages
│
├── .gitignore
├── pom.xml
└── README.md
```

## Requirements

Before running the project, install:

- JDK 17 or later
- Apache Maven 3.9+
- PostgreSQL 14+
- Apache Tomcat 10.1+
- IntelliJ IDEA
- Postman (optional, for API testing)

**Note:** This project uses Jakarta Servlet 6, so Tomcat 10.1+ is required. Tomcat 9 is not supported.

## Database Setup

### 1. Create Database

Open pgAdmin or PostgreSQL Query Tool and execute:

```sql
CREATE DATABASE bookstore;
```

### 2. Import SQL File

1. Open the `bookstore` database in pgAdmin.
2. Open Query Tool.
3. Load `database/bookstore.sql`.
4. Execute the SQL script.

The script creates the required database tables and inserts 24 sample books.

### 3. Configure Database Connection

Open:

`src/main/resources/application.properties`

Configure the database using environment variables:

```properties
db.url=${DB_URL}
db.username=${DB_USERNAME}
db.password=${DB_PASSWORD}
```

Set the following environment variables in your IntelliJ Run Configuration:

| Variable | Description |
|---|---|
| DB_URL | PostgreSQL JDBC connection URL |
| DB_USERNAME | PostgreSQL database username |
| DB_PASSWORD | PostgreSQL database password |

**Security:** Never upload actual database passwords, API keys, or other secrets to GitHub.

## How to Run the Project

### Step 1: Clone the Repository

```bash
git clone https://github.com/Haseena2006/Online-bookstore.git
```

### Step 2: Open in IntelliJ IDEA

1. Open IntelliJ IDEA.
2. Select File → Open.
3. Choose the `online-bookstore` project folder.
4. Allow Maven to import dependencies from `pom.xml`.

### Step 3: Configure Tomcat

1. Install Apache Tomcat 10.1 or later.
2. Open Run → Edit Configurations.
3. Add a Smart Tomcat configuration.
4. Select the project module.
5. Configure the deployment directory as:

```text
src/main/webapp
```

6. Set the application context path:

```text
/online-bookstore
```

7. Configure the database environment variables.
8. Set the server port to `8081`.

### Step 4: Run the Application

Start the Smart Tomcat server.

Open the application in your browser:

**http://localhost:8081/online-bookstore/**

### Maven Alternative

Build the application using:

```bash
mvn clean package
```

Copy the generated WAR file from `target/` into the Tomcat `webapps/` directory and start the Tomcat server.

## REST API Endpoints

The application provides REST APIs for books, users, cart management, and orders.

### Books API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/books` | Get all books |
| GET | `/books/{id}` | Get book by ID |
| GET | `/books/search?keyword=` | Search books |
| GET | `/books/category/{category}` | Get books by category |
| GET | `/books/featured` | Get featured books |
| GET | `/books/popular` | Get popular books |
| GET | `/books/new` | Get newly added books |
| GET | `/books/categories` | Get available categories |

### User API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/users/register` | Register a new user |
| POST | `/users/login` | User login |
| POST | `/users/logout` | User logout |
| GET | `/users/me` | Get logged-in user details |

### Cart API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/cart` | Get cart items |
| POST | `/cart` | Add item to cart |
| PUT | `/cart/{id}` | Update cart item |
| DELETE | `/cart/{id}` | Remove cart item |

### Order API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/checkout` | Place an order |
| GET | `/orders/{id}` | Get order details |
| GET | `/users/{userId}/orders` | Get user order history |

**Authentication:** Cart and order operations require a valid HTTP session. Users can access only their own orders.

## Postman API Testing

The project includes a Postman collection for API testing.

Collection location:

```text
postman/online-bookstore.postman_collection.json
```

### Testing Steps

1. Open Postman.
2. Import the collection JSON file.
3. Set the `baseUrl` variable to:

```text
http://localhost:8081/online-bookstore
```

4. Start the application using Tomcat.
5. Run the API requests in the appropriate order.
6. Register and log in before testing authenticated endpoints.

Postman automatically manages the session cookie when requests use the same cookie domain.

## Checkout Process

The checkout process is handled within a database transaction.

1. Validate the user's session.
2. Retrieve the user's cart items.
3. Check available book stock.
4. Lock and validate stock during checkout.
5. Create the order.
6. Save order items with their purchase-time prices.
7. Reduce available book stock.
8. Clear the shopping cart.
9. Return the order confirmation and reference number.

Payment method: **Cash on Delivery (COD)**. No online payment gateway is integrated.

## Security

- Passwords are hashed using BCrypt.
- Prepared statements are used for database operations.
- Session-based authentication is implemented.
- Cart and order operations require authentication.
- Users can access only their own orders.
- Input validation and exception handling are included.

**Note:** This is a demonstration project. CSRF protection, rate limiting, HTTPS, and additional production security measures should be implemented before real-world deployment.

## Database Verification

You can verify the database operations using pgAdmin:

```sql
SELECT * FROM books;

SELECT * FROM cart;

SELECT * FROM orders;

SELECT * FROM order_items;
```

Check book stock after checkout:

```sql
SELECT id, title, stock, sold_count
FROM books
ORDER BY id;
```

## Future Enhancements

- Admin dashboard for book management.
- Online payment gateway integration.
- Product reviews and ratings.
- Wishlist functionality.
- Email order notifications.
- Advanced filtering and pagination.
- Deployment to a cloud hosting platform.

## Author

**Haseena Fathim**

GitHub: [Haseena2006](https://github.com/Haseena2006)

Project Repository: [Online Bookstore](https://github.com/Haseena2006/Online-bookstore)

---

*Page & Co. – Your next favourite book is waiting.*
