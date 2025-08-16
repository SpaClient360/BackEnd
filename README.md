# 🚀 CRM Module - Customer Relationship Management System

## 📋 Overview

A comprehensive CRM system built with Spring Boot 3.5.4, featuring user management, authentication with JWT & OTP, and customer request handling with anti-spam protection.

## 🏗️ Architecture

```
src/main/java/com/htttql/crmmodule/
├── auth/           # Authentication & Authorization
├── user/           # User Management
├── crmrequest/     # Customer Request Handling
├── common/         # Shared Components (Exception Handling)
├── config/         # Configuration Classes
└── CrmModuleApplication.java
```

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.5.4
- **Java**: 17
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT
- **Email**: Spring Mail (SMTP)
- **Documentation**: OpenAPI/Swagger
- **Build**: Maven

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- SMTP account (Gmail recommended)

### 1. Database Setup

```sql
CREATE DATABASE droha_management;
CREATE SCHEMA crm;
```

### 2. Environment Variables

Copy `application-example.properties` to set up your environment:

```bash
# Database
export DB_URL=jdbc:postgresql://localhost:5432/droha_management
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password

# JWT (Generate new secret for production!)
export JWT_SECRET=your_jwt_secret_key_base64
export JWT_EXPIRATION=86400000
export JWT_REFRESH_EXPIRATION=604800000

# Email Configuration
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

# Rate Limiting
export RATE_LIMIT_RPM=5
export RATE_LIMIT_WINDOW=1
```

### 3. Build & Run

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Or run with JAR
java -jar target/crm-module-0.0.1-SNAPSHOT.jar
```

## 📚 API Documentation

Access Swagger UI at: `http://localhost:8081/swagger-ui/index.html`

### 🔐 Authentication Endpoints

| Method | Endpoint               | Description       |
| ------ | ---------------------- | ----------------- |
| POST   | `/api/auth/login`      | Standard login    |
| POST   | `/api/auth/login-otp`  | Login with OTP    |
| POST   | `/api/auth/verify-otp` | Verify OTP        |
| POST   | `/api/auth/register`   | User registration |
| POST   | `/api/auth/refresh`    | Refresh token     |
| POST   | `/api/auth/logout`     | Logout            |
| GET    | `/api/auth/me`         | Current user info |

### 👥 User Management

| Method | Endpoint             | Description            |
| ------ | -------------------- | ---------------------- |
| GET    | `/api/users`         | List users (paginated) |
| POST   | `/api/users`         | Create user            |
| GET    | `/api/users/{id}`    | Get user by ID         |
| PUT    | `/api/users/{id}`    | Update user            |
| DELETE | `/api/users/{id}`    | Delete user            |
| GET    | `/api/users/profile` | Current user profile   |

### 📝 Customer Requests

| Method | Endpoint                             | Description    |
| ------ | ------------------------------------ | -------------- |
| POST   | `/api/customer-requests`             | Create request |
| GET    | `/api/customer-requests`             | List requests  |
| GET    | `/api/customer-requests/{id}`        | Get request    |
| PUT    | `/api/customer-requests/{id}/status` | Update status  |

## 🔒 Security Features

### JWT Authentication

- Access tokens (24h expiration)
- Refresh tokens (7 days expiration)
- Token rotation on refresh
- Secure logout with token revocation

### OTP Verification

- Email-based OTP for enhanced security
- 5-minute expiration
- Anti-brute force protection

### Rate Limiting

- IP-based rate limiting
- Configurable limits (default: 5 requests/minute)
- Anti-spam protection for customer requests

### Security Headers

- CORS configuration
- CSRF protection disabled (stateless JWT)
- Secure JWT signing

## 🛡️ Error Handling

Centralized error handling with:

- Validation errors (400)
- Authentication errors (401)
- Authorization errors (403)
- Not found errors (404)
- Duplicate request errors (409)
- Rate limit errors (429)
- Internal server errors (500)

## 📊 Database Schema

### Users Table

```sql
users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(100) UNIQUE,
  phone VARCHAR(20) UNIQUE,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(100) NOT NULL,
  role_id BIGINT REFERENCES roles(id),
  status VARCHAR(20) DEFAULT 'ACTIVE',
  -- Additional profile fields...
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
```

### Customer Requests Table

```sql
customer_requests (
  id UUID PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  phone_number VARCHAR(32),
  customer_note TEXT,
  source VARCHAR(64),
  ip_address VARCHAR(45),
  status VARCHAR(32) DEFAULT 'NEW',
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);
```

## 🔧 Configuration

### Application Properties

Key configurations in `application.properties`:

```properties
# Security
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

# Database
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Email
spring.mail.host=${MAIL_HOST:smtp.gmail.com}

# Rate Limiting
rate.limit.requests.per.minute=${RATE_LIMIT_RPM:5}
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=CustomerRequestControllerValidationTest
```

## 📦 Deployment

### Production Checklist

1. **Environment Variables**: Set all required environment variables
2. **Database**: Create production database and schema
3. **JWT Secret**: Generate new secret key for production
4. **Email**: Configure production SMTP settings
5. **Logging**: Configure appropriate log levels
6. **SSL/TLS**: Enable HTTPS in production
7. **Rate Limiting**: Adjust based on production requirements

### Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/crm-module-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔍 Monitoring & Logging

- Structured logging with Logback
- Request/Response logging for security events
- Database query logging (configurable)
- Error tracking with stack traces

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:

- Create an issue in the repository
- Contact the development team
- Check the API documentation at `/swagger-ui/index.html`

---

**Built with ❤️ using Spring Boot & Java 17**
