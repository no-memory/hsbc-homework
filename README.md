# Transaction Management System

A high-performance banking transaction management application built with Java 21 and Spring Boot 3.2.

## Overview

This application provides a comprehensive REST API for managing financial transactions within a banking system. It emphasizes performance, scalability, and robust error handling while maintaining data consistency in an in-memory storage system.

## Features

- **Complete CRUD Operations**: Create, read, update, and delete transactions
- **Advanced Querying**: Filter transactions by account, type, amount range, and date range
- **Pagination**: Efficient pagination for large datasets
- **Caching**: High-performance caching with Caffeine
- **Validation**: Comprehensive input validation and error handling
- **Monitoring**: Health checks and metrics with Spring Actuator
- **Documentation**: Interactive API documentation with Swagger/OpenAPI
- **Containerization**: Docker and Kubernetes ready
- **Testing**: Comprehensive unit, integration, and stress tests

## Technology Stack

### Core Technologies
- **Java 21**: Latest LTS version with modern language features
- **Spring Boot 3.2**: Enterprise-grade application framework
- **Spring Web**: RESTful web services
- **Spring Validation**: Bean validation with JSR-380
- **Spring Cache**: Declarative caching abstraction

### Caching
- **Caffeine**: High-performance, near-optimal caching library
- **Configuration**: TTL-based cache with statistics

### Documentation
- **SpringDoc OpenAPI 3**: API documentation and testing interface
- **Swagger UI**: Interactive API explorer

### Testing
- **JUnit 5**: Modern testing framework
- **Mockito**: Mocking framework for unit tests
- **Spring Boot Test**: Integration testing support
- **Stress Testing**: Custom performance tests

### Containerization
- **Docker**: Multi-stage builds for optimization

### Transaction Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/transactions` | Create a new transaction |
| GET | `/api/v1/transactions/{id}` | Get transaction by ID |
| GET | `/api/v1/transactions` | Get all transactions (paginated) |
| PUT | `/api/v1/transactions/{id}` | Update existing transaction |
| DELETE | `/api/v1/transactions/{id}` | Delete transaction |
| DELETE | `/api/v1/transactions` | Delete all transactions |

### Query Operations

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/transactions/account/{accountNumber}` | Get transactions by account |
| GET | `/api/v1/transactions/type/{type}` | Get transactions by type |
| GET | `/api/v1/transactions/amount-range` | Get transactions by amount range |
| GET | `/api/v1/transactions/date-range` | Get transactions by date range |

### Statistics

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/transactions/statistics/count` | Get total transaction count |
| GET | `/api/v1/transactions/statistics/count-by-type` | Get count by transaction type |
| GET | `/api/v1/transactions/statistics/count-by-account` | Get count by account |
| GET | `/api/v1/transactions/statistics/total-amount` | Get total transaction amount |
| GET | `/api/v1/transactions/statistics/total-amount-by-type/{type}` | Get total amount by type |
| GET | `/api/v1/transactions/statistics/total-amount-by-account/{accountNumber}` | Get total amount by account |

## Transaction Types

- `CREDIT` - Credit transactions
- `DEBIT` - Debit transactions
- `TRANSFER` - Transfer transactions
- `PAYMENT` - Payment transactions
- `DEPOSIT` - Deposit transactions
- `WITHDRAWAL` - Withdrawal transactions
- `FEE` - Fee transactions
- `INTEREST` - Interest transactions
- `REFUND` - Refund transactions
- `ADJUSTMENT` - Adjustment transactions


### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/no-memory/hsbc-homework.git
   cd hsbc-homework
   ```

2. **Build the application**
   ```bash
   ./mvnw clean package
   ```

3. **Run tests**
   ```bash
   ./mvnw test
   ```

4. **Start the application**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Access the application**
   - API Base URL: http://localhost:8080/api/v1/transactions
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - Health Check: http://localhost:8080/actuator/health
   - Metrics: http://localhost:8080/actuator/metrics

### Docker Deployment

1. **Build Docker image**
   ```bash
   docker build -t hsbc/hsbc-homework:latest .
   ```

2. **Run with Docker**
   ```bash
   docker run --rm -p 8080:8080 hsbc/hsbc-homework:latest
   ```


## Testing

### Unit Tests
```bash
./mvnw test
```

### Integration Tests
```bash
./mvnw test -Dtest=*IntegrationTest
```

### Stress Tests
```bash
./mvnw test -Dtest=*StressTest
```

### Test Coverage
```bash
./mvnw jacoco:report
```
View coverage report at `target/site/jacoco/index.html`
