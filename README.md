# Books E-Commerce Microservices

A microservices-based e-commerce application for managing books, built with Spring Boot and MySQL and data extracted from books.toscrape.com 

## Technologies Used

- **Java 17+** - Programming Language
- **Spring Boot 3.x** - Application Framework
- **Spring Cloud** - Microservices Infrastructure
  - Eureka Server - Service Discovery
  - API Gateway - Routing & Load Balancing
  - OpenFeign - Inter-service Communication
- **MySQL 8.0** - Database
- **Hibernate/JPA** - ORM
- **Swagger/OpenAPI** - API Documentation
- **Maven** - Build Tool

## Architecture

The application consists of 5 microservices:

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| Eureka Server | 8761 | - | Service Registry |
| API Gateway | 9090 | - | Single Entry Point |
| Book Service | 8000 | books_db | Book Management |
| Category Service | 8100 | category_db | Category Management |
| Price Service | 8200 | price_db | Pricing Information |
| Inventory Service | 8300 | inventory_db | Stock Management |

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0
- Git

## Database Setup

1. **Create MySQL Databases:**
```sql
CREATE DATABASE books_db;
CREATE DATABASE category_db;
CREATE DATABASE inventory_db;
CREATE DATABASE price_db;
```

2. **Configure MySQL Credentials:**
   - Default: `username: root`, `password: root`
   - Update `application.yaml` in each service if different

## Getting Book Data (Optional)

Use the [BookScraper](https://github.com/TBanda27/BookScrapper.git) to scrape fresh book data:

```bash
git clone https://github.com/TBanda27/BookScrapper.git
cd BookScrapper
# Follow BookScraper instructions to generate books.csv
```

Then use the provided `generate_sql.py` script to populate databases:

```bash
python generate_sql.py
mysql -u root -proot < 1_category_insert.sql
mysql -u root -proot < 2_books_insert.sql
mysql -u root -proot < 3_inventory_insert.sql
mysql -u root -proot < 4_price_insert.sql
```

## Running the Application

### Start Services in Order:

```bash
# 1. Start Eureka Server
cd eureka
mvn spring-boot:run

# 2. Start all microservices (in separate terminals)
cd book-service
mvn spring-boot:run

cd Category
mvn spring-boot:run

cd price
mvn spring-boot:run

cd inventory
mvn spring-boot:run

# 3. Start API Gateway
cd api-gateway
mvn spring-boot:run
```

**Wait 30-60 seconds** for all services to register with Eureka.

## API Documentation (Swagger)

Access unified Swagger UI through the API Gateway:

**URL:** `http://localhost:9090/swagger-ui.html`

Select service from dropdown to explore endpoints:
- Book Service
- Category Service
- Price Service
- Inventory Service

### Individual Service Swagger:
- Book: `http://localhost:8000/swagger-ui.html`
- Category: `http://localhost:8100/swagger-ui.html`
- Price: `http://localhost:8200/swagger-ui.html`
- Inventory: `http://localhost:8300/swagger-ui.html`

## Service Monitoring

**Eureka Dashboard:** `http://localhost:8761`

View registered services and their health status.

## Configuration

Key configuration files:
- `*/src/main/resources/application.yaml` - Service configuration
- Database URL, port, and Eureka settings

## Project Structure

```
Books-Ecommerce-Project/
├── eureka/              # Service Discovery
├── api-gateway/         # API Gateway
├── book-service/        # Book Management
├── Category/            # Category Management
├── price/              # Price Management
├── inventory/          # Inventory Management
├── books.csv           # Sample Book Data
└── generate_sql.py     # SQL Generator Script
```

## API Access

All requests should go through the API Gateway:

```
Base URL: http://localhost:9090
```

Example endpoints:
- `GET /book-service/api/books`
- `GET /category-service/api/categories`
- `GET /price-service/api/prices`
- `GET /inventory-service/api/inventory`

## Troubleshooting

**Services not visible in Eureka:**
- Wait 30-60 seconds for registration
- Check Eureka is running on port 8761
- Verify `eureka.client.service-url.defaultZone` in application.yaml

**Database connection errors:**
- Verify MySQL is running
- Check database credentials in application.yaml
- Ensure all 4 databases are created

**Port conflicts:**
- Check ports 8000, 8100, 8200, 8300, 8761, 9090 are available
- Update `server.port` in application.yaml if needed

## License

This project is open source and available under the MIT License.
