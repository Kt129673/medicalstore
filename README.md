# Medical Store Management System

Full-stack application for managing medical store inventory, sales, and pharmacy operations.

## 🏗️ Architecture

- **Backend:** Spring Boot 3.5.6 (Java 17, Maven, H2 Database, JWT Authentication)
- **Frontend:** Angular 15 (TypeScript, npm, Reactive Forms)
- **Security:** JWT-based stateless authentication with Spring Security 6+

## 📚 Documentation

### For AI Agents & Developers
- 🤖 **[AGENT_INSTRUCTIONS.md](AGENT_INSTRUCTIONS.md)** ← **START HERE** (Primary instructions for AI agents)
- 🔧 **[.github/copilot-instructions.md](.github/copilot-instructions.md)** (Quick reference for GitHub Copilot)

### For Users
- 📖 **[SETUP_AND_RUN.md](SETUP_AND_RUN.md)** (How to install and run the application)
- 🧪 **[LOGIN_TEST_GUIDE.md](LOGIN_TEST_GUIDE.md)** (Testing login/logout functionality)

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 16+ and npm
- Maven 3.6+

### Start Backend
```powershell
cd medical-store-management-backend
mvn spring-boot:run
```
Backend runs on: http://localhost:8080

### Start Frontend
```powershell
cd medical-store-management-frontend
npm install  # First time only
npm start
```
Frontend runs on: http://localhost:4200

## 🔑 Default Access

- **URL:** http://localhost:4200
- **Register** a new user (username & password)
- **Login** with your credentials
- **H2 Console:** http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:medicalstoredb`
  - Username: `sa`
  - Password: (leave empty)

## 📁 Project Structure

```
medicalstore/
├── AGENT_INSTRUCTIONS.md                 # AI agent instructions
├── .github/
│   └── copilot-instructions.md          # GitHub Copilot reference
├── medical-store-management-backend/    # Spring Boot backend
│   ├── pom.xml
│   └── src/main/java/com/medicalstore/management/
│       ├── config/                      # Security, CORS configuration
│       ├── controller/                  # REST API endpoints
│       ├── model/                       # JPA entities
│       ├── repository/                  # Data access layer
│       ├── security/                    # JWT utilities
│       └── service/                     # Business logic
└── medical-store-management-frontend/   # Angular frontend
    ├── package.json
    ├── proxy.conf.json                  # API proxy configuration
    └── src/app/
        ├── core/                        # Services, guards, interceptors
        ├── features/                    # Feature modules
        │   ├── auth/                   # Login/Register
        │   └── dashboard/              # Dashboard
        └── shared/                      # Shared components

```

## 🛠️ Technology Stack

### Backend
- Spring Boot 3.5.6
- Spring Data JPA (Hibernate)
- Spring Security 6+
- H2 In-Memory Database
- JWT (jjwt 0.9.1)
- Maven

### Frontend
- Angular 15.2.0
- TypeScript 4.9.4
- RxJS 7.8.0
- Angular Material 15.2.9
- SCSS

## 🔐 Features

- ✅ JWT-based authentication
- ✅ User registration and login
- ✅ Stateless session management
- ✅ Password encryption (BCrypt)
- ✅ HTTP interceptor for automatic token attachment
- ✅ Spring Boot DevTools (auto-reload on code changes)
- ✅ Angular hot reload
- ✅ H2 database console
- ✅ CORS configuration
- ✅ REST API endpoints

## 🤝 Contributing

See [AGENT_INSTRUCTIONS.md](AGENT_INSTRUCTIONS.md) for development guidelines, terminal management, and debugging workflows.

## 📄 License

This project is for educational purposes.

---

**For detailed agent instructions:** See [AGENT_INSTRUCTIONS.md](AGENT_INSTRUCTIONS.md)
