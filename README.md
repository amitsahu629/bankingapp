# Online Banking Application (bankingapp)

A comprehensive, secure online banking system built with Spring Boot, featuring modern web technologies and enterprise-grade security practices.

## 🏦 Overview

This online banking application provides a complete digital banking solution with user authentication, account management, transaction processing, and real-time notifications. Built with industry best practices for security, scalability, and maintainability.

## ✨ Features

### 🔐 Security & Authentication
- **JWT-based Authentication** - Stateless token-based security
- **Password Encryption** - BCrypt hashing with salt
- **Role-based Access Control** - User and admin role management
- **Input Validation** - Comprehensive data validation
- **Audit Logging** - Complete activity tracking

### 🏦 Account Management
- **Multiple Account Types** - Savings, Checking, Credit accounts
- **Account Creation & Management** - Full CRUD operations
- **Balance Tracking** - Real-time balance updates
- **Account Status Management** - Activate/deactivate accounts
- **Account Statistics** - Comprehensive analytics

### 💳 Transaction Processing
- **Deposit Operations** - Money deposits to accounts
- **Withdrawal Operations** - Secure money withdrawals
- **Transfer Operations** - Account-to-account transfers
- **Transaction History** - Complete transaction records
- **Real-time Processing** - Immediate balance updates

### 🔔 Notifications & Events
- **Kafka Integration** - Asynchronous event processing
- **Real-time Notifications** - Transaction alerts
- **Email Notifications** - Account activity updates
- **Event Streaming** - Scalable message processing

### 📊 Analytics & Reporting
- **Transaction Analytics** - Spending and income tracking
- **Account Statistics** - Performance metrics
- **Financial Reports** - Comprehensive reporting
- **Trend Analysis** - Historical data analysis

### 📱 Responsive Web Interface
- **Bootstrap 5** - Modern, responsive design
- **Interactive Dashboard** - Real-time data display
- **Mobile-friendly** - Optimized for all devices
- **User-friendly UI** - Intuitive navigation

## 🛠️ Technology Stack

### Backend
- **Java 8** - Core programming language
- **Spring Boot 2.7.x** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **Hibernate ORM** - Object-relational mapping
- **MySQL 8.0** - Primary database
- **Apache Kafka** - Message streaming
- **JWT** - JSON Web Tokens for authentication
- **Log4j2** - Comprehensive logging
- **Maven** - Build and dependency management

### Frontend
- **HTML5, CSS3, JavaScript** - Core web technologies
- **Bootstrap 5** - Responsive CSS framework
- **jQuery** - JavaScript library
- **Chart.js** - Data visualization

### Testing & Quality
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing
- **H2 Database** - In-memory testing database

## 🚀 Quick Start

### Prerequisites
- **Java 8 or higher**
- **Maven 3.6+**
- **MySQL 8.0+**
- **Apache Kafka 2.8+**
- **Git**

### Installation Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-username/online-banking-app.git
   cd online-banking-app
   ```

2. **Setup MySQL Database**
   ```sql
   CREATE DATABASE online_banking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'banking_user'@'localhost' IDENTIFIED BY 'banking_password';
   GRANT ALL PRIVILEGES ON online_banking.* TO 'banking_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Setup Kafka**
   ```bash
   # Start Zookeeper
   bin/zookeeper-server-start.sh config/zookeeper.properties
   
   # Start Kafka Server
   bin/kafka-server-start.sh config/server.properties
   
   # Create Topics
   bin/kafka-topics.sh --create --topic transaction-events --bootstrap-server localhost:9092
   bin/kafka-topics.sh --create --topic notification-events --bootstrap-server localhost:9092
   ```

4. **Configure Application**
   ```bash
   cp src/main/resources/application.yml.example src/main/resources/application.yml
   # Edit application.yml with your database credentials
   ```

5. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

6. **Access Application**
   - **Web Interface**: http://localhost:8080
   - **API Documentation**: http://localhost:8080/swagger-ui.html
   - **H2 Console** (if enabled): http://localhost:8080/h2-console

## ⚙️ Configuration

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/online_banking
    username: banking_user
    password: banking_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### JWT Configuration
```yaml
app:
  jwt:
    secret: mySecretKey
    expiration: 86400000 # 24 hours
```

### Kafka Configuration
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

## 📖 API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| POST | `/api/auth/login` | User login | `{"username": "user", "password": "pass"}` |
| POST | `/api/auth/signup` | User registration | `{"username": "user", "email": "email", "password": "pass", "firstName": "John", "lastName": "Doe"}` |

### Account Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/accounts` | Get user accounts | ✅ |
| POST | `/api/accounts` | Create new account | ✅ |
| GET | `/api/accounts/{id}` | Get account details | ✅ |
| PUT | `/api/accounts/{id}` | Update account | ✅ |
| DELETE | `/api/accounts/{id}` | Deactivate account | ✅ |
| GET | `/api/accounts/{id}/balance` | Get account balance | ✅ |
| GET | `/api/accounts/{id}/statistics` | Get account statistics | ✅ |

### Transaction Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/transactions/deposit` | Deposit money | ✅ |
| POST | `/api/transactions/withdraw` | Withdraw money | ✅ |
| POST | `/api/transactions/transfer` | Transfer money | ✅ |
| GET | `/api/transactions/history/{accountId}` | Get transaction history | ✅ |
| GET | `/api/transactions/{id}` | Get transaction details | ✅ |

### Example API Requests

**Login Request:**
```json
POST /api/auth/login
{
  "username": "johndoe",
  "password": "password123"
}
```

**Create Account Request:**
```json
POST /api/accounts
{
  "accountType": "SAVINGS"
}
```

**Deposit Request:**
```json
POST /api/transactions/deposit
{
  "accountId": 1,
  "amount": 500.00,
  "description": "Salary deposit"
}
```

**Transfer Request:**
```json
POST /api/transactions/transfer
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 250.00,
  "description": "Transfer to savings"
}
```

## 🏗️ Project Structure

```
online-banking-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── bank/
│   │   │           ├── OnlineBankingApplication.java
│   │   │           ├── config/           # Configuration classes
│   │   │           ├── controller/       # REST controllers
│   │   │           ├── service/          # Business logic
│   │   │           ├── repository/       # Data access layer
│   │   │           ├── entity/           # JPA entities
│   │   │           ├── dto/              # Data transfer objects
│   │   │           ├── exception/        # Custom exceptions
│   │   │           ├── security/         # Security components
│   │   │           └── util/             # Utility classes
│   │   └── resources/
│   │       ├── application.yml          # Application configuration
│   │       ├── log4j2.xml              # Logging configuration
│   │       └── static/                 # Web assets
│   │           ├── css/
│   │           ├── js/
│   │           └── index.html
│   └── test/                           # Test classes
├── docs/                               # Documentation
├── scripts/                            # Deployment scripts
├── pom.xml                            # Maven configuration
└── README.md
```

## 🧪 Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run integration tests
mvn test -Dtest=*IntegrationTest

# Generate test coverage report
mvn jacoco:report
```

### Test Categories
- **Unit Tests** - Individual component testing
- **Integration Tests** - End-to-end testing
- **Security Tests** - Authentication and authorization
- **Performance Tests** - Load and stress testing

## 🔒 Security Features

### Authentication & Authorization
- **JWT Token-based Authentication**
- **Password Strength Validation**
- **Account Lockout Protection**
- **Role-based Access Control**

### Data Protection
- **Input Validation & Sanitization**
- **SQL Injection Prevention**
- **XSS Protection**
- **CSRF Protection**

### Audit & Monitoring
- **Complete Audit Trails**
- **Security Event Logging**
- **Failed Login Monitoring**
- **Suspicious Activity Detection**

## 📊 Performance & Scalability

### Database Optimization
- **Connection Pooling** (HikariCP)
- **Query Optimization**
- **Proper Indexing**
- **Pagination Support**

### Caching Strategy
- **Application-level Caching**
- **Database Query Caching**
- **Static Asset Caching**

### Asynchronous Processing
- **Kafka Message Processing**
- **Async Notifications**
- **Background Task Processing**

## 🚀 Deployment

### Development Environment
```bash
# Start development server
mvn spring-boot:run

# Enable development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production Deployment

#### Docker Deployment
```dockerfile
FROM openjdk:8-jre-alpine
VOLUME /tmp
COPY target/online-banking-app-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
# Build Docker image
docker build -t online-banking-app .

# Run container
docker run -p 8080:8080 online-banking-app
```

#### Traditional Deployment
```bash
# Build production JAR
mvn clean package -Pprod

# Run with production profile
java -jar target/online-banking-app-1.0.0.jar --spring.profiles.active=prod
```

### Environment Variables
```bash
export DB_URL=jdbc:mysql://localhost:3306/online_banking
export DB_USERNAME=banking_user
export DB_PASSWORD=banking_password
export JWT_SECRET=your-secret-key
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## 📈 Monitoring & Observability

### Application Metrics
- **Spring Boot Actuator** - Health checks and metrics
- **Custom Metrics** - Business-specific monitoring
- **Performance Monitoring** - Response times and throughput

### Logging
- **Structured Logging** - JSON format logs
- **Log Aggregation** - Centralized log management
- **Error Tracking** - Exception monitoring

### Health Checks
- **Database Connectivity**
- **Kafka Connectivity**
- **Memory Usage**
- **Disk Space**

## 🛠️ Development Setup

### IDE Configuration

#### IntelliJ IDEA
1. Import as Maven project
2. Set Project SDK to Java 8
3. Enable annotation processing
4. Install required plugins: Spring Boot, Lombok

#### Eclipse/STS
1. Import as existing Maven project
2. Set compliance level to Java 8
3. Enable Spring Boot nature
4. Configure build path

### Code Style
- **Google Java Style Guide**
- **Checkstyle Configuration**
- **PMD Rules**
- **SpotBugs Analysis**

### Git Workflow
```bash
# Feature development
git checkout -b feature/account-management
git add .
git commit -m "feat: add account creation functionality"
git push origin feature/account-management

# Create pull request for code review
```

## 🐛 Troubleshooting

### Common Issues

#### Database Connection Issues
```bash
# Check MySQL service
sudo service mysql status

# Verify database exists
mysql -u root -p -e "SHOW DATABASES;"

# Test connection
mysql -u banking_user -p online_banking
```

#### Kafka Connection Issues
```bash
# Check Kafka topics
bin/kafka-topics.sh --list --bootstrap-server localhost:9092

# Verify consumer groups
bin/kafka-consumer-groups.sh --list --bootstrap-server localhost:9092
```

#### Memory Issues
```bash
# Increase JVM heap size
export JAVA_OPTS="-Xms512m -Xmx2048m"

# Monitor memory usage
jstat -gc [PID]
```

### Debug Mode
```bash
# Enable debug logging
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.bank=DEBUG"

# Remote debugging
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## 📚 Additional Resources

### Documentation
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Reference](https://spring.io/projects/spring-security)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [MySQL Documentation](https://dev.mysql.com/doc/)

### Learning Resources
- [Spring Boot Guides](https://spring.io/guides)
- [Building REST APIs with Spring](https://spring.io/guides/tutorials/rest/)
- [Securing Spring Boot Applications](https://spring.io/guides/topicals/spring-security-architecture/)

## 🤝 Contributing

### Getting Started
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

### Development Guidelines
- Follow existing code style
- Write comprehensive tests
- Update documentation
- Use meaningful commit messages

### Code Review Process
1. Automated tests must pass
2. Code coverage > 80%
3. Security review for sensitive changes
4. Performance impact assessment

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Lead Developer** - System architecture and backend development
- **Frontend Developer** - UI/UX and responsive design
- **DevOps Engineer** - Deployment and infrastructure
- **Security Specialist** - Security implementation and auditing

## 📞 Support

### Getting Help
- **Issues** - Report bugs and feature requests on GitHub
- **Discussions** - Community support and questions
- **Wiki** - Detailed documentation and guides
- **Email** - support@bankingapp.com

### Reporting Security Issues
- **Email** - security@bankingapp.com
- **Encrypted Communication** - Use GPG key for sensitive reports
- **Response Time** - Within 24 hours for critical issues

---

## 🎯 Roadmap

### Version 1.1 (Q2 2024)
- [ ] Mobile application (iOS/Android)
- [ ] Advanced fraud detection
- [ ] Multi-factor authentication
- [ ] Cryptocurrency support

### Version 1.2 (Q3 2024)
- [ ] Microservices architecture
- [ ] GraphQL API
- [ ] Real-time analytics dashboard
- [ ] Machine learning insights

### Version 2.0 (Q4 2024)
- [ ] Open Banking API compliance
- [ ] Third-party integrations
- [ ] Advanced reporting tools
- [ ] International transfers

---


