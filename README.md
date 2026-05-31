# Secure Login System

A full-stack secure authentication system built with Spring Boot, MySQL, and a creative dark-themed frontend. This project demonstrates real-world cybersecurity concepts including encrypted password storage, stateless JWT sessions, and protection against common web attacks.

---

## About This Project

This system was built as part of a Web and Cybersecurity project portfolio. It covers the complete authentication flow from user registration to protected dashboard access, implementing industry-standard security practices used in real production applications.

---

## Features

- User registration with real-time password strength validation
- Secure login with JWT token generation
- BCrypt password hashing — passwords never stored in plain text
- SQL injection prevention using parameterized queries
- XSS protection via Content-Security-Policy headers
- Stateless session management using JWT
- Protected dashboard with token validation
- Creative dark-themed frontend with animations
- CORS configuration for frontend-backend communication

---

## Tech Stack

Backend
- Java 17
- Spring Boot 4.0.6
- Spring Security
- Spring Data JPA
- MySQL 9.7
- JWT (jjwt 0.11.5)
- BCrypt password encoder
- Lombok
- Maven

Frontend
- HTML5
- CSS3 with animations
- Vanilla JavaScript
- Google Fonts (Syne and DM Sans)

---

## Project Structure

src/
  main/
    java/com/example/securelogin/
      config/
        SecurityConfig.java         - Spring Security and XSS header configuration
        CorsConfig.java             - CORS rules for frontend communication
      controller/
        AuthController.java         - REST API endpoints for auth operations
      dto/
        LoginRequest.java           - Data object for login requests
        RegisterRequest.java        - Data object for registration requests
      model/
        User.java                   - User entity mapped to MySQL users table
      repository/
        UserRepository.java         - Database query interface
      service/
        JwtService.java             - JWT token generation and validation
        UserService.java            - User registration and authentication logic
      SecureloginApplication.java   - Main application entry point
    resources/
      application.properties        - Database and JWT configuration (not pushed to GitHub)

Frontend pages
  index.html       - Landing page with feature showcase
  login.html       - Login form with password toggle
  register.html    - Registration form with password strength meter
  dashboard.html   - Protected dashboard showing JWT token and security status

---

## Getting Started

Prerequisites
- Java 17 or higher
- MySQL 8 or higher
- Maven 3.6 or higher
- Any modern web browser

Step 1 - Clone the repository

  git clone https://github.com/tharushisandunika/Secure-Login-System.git
  cd Secure-Login-System

Step 2 - Create the MySQL database

  CREATE DATABASE secure_login_db;

Step 3 - Create the configuration file
Create a file at src/main/resources/application.properties and add the following:

  spring.datasource.url=jdbc:mysql://localhost:3306/secure_login_db
  spring.datasource.username=root
  spring.datasource.password=your_mysql_password
  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.show-sql=true
  jwt.secret=SecureKey1234567890ABCDEFabcdefGHIJKL
  jwt.expiration=86400000
  server.port=8080

Step 4 - Run the backend

  ./mvnw spring-boot:run

The server starts at http://localhost:8080
Spring Boot automatically creates the users table in MySQL.

Step 5 - Open the frontend
Open index.html in your browser. Make sure the backend is running first.

---

## API Endpoints

POST   /api/auth/register   - Create a new user account
POST   /api/auth/login      - Login and receive a JWT token
GET    /api/auth/validate   - Validate an existing JWT token
POST   /api/auth/logout     - Logout and clear the session

Register request body
  {
    "username": "tharushi",
    "email": "tharushi@email.com",
    "password": "MyPass@123"
  }

Login response
  {
    "success": true,
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "tharushi"
  }

---

## Security Implementation

Password hashing
  User registers with plain text password
  BCryptPasswordEncoder hashes it with strength 12
  Hashed string stored in database
  On login, BCrypt compares plain text with stored hash
  Original password is never recoverable

JWT authentication flow
  User logs in with correct credentials
  Server generates a JWT token signed with HS256
  Token contains username and expiry time (24 hours)
  Token sent to browser and stored in localStorage
  Every protected request includes the token in the header
  Server validates token signature and expiry before allowing access

SQL injection prevention
  All database queries use Spring Data JPA
  JPA generates parameterized queries automatically
  User input is never directly inserted into SQL strings
  Example - findByUsername(username) generates
  SELECT * FROM users WHERE username = ? instead of
  SELECT * FROM users WHERE username = 'user_input'

XSS protection
  Content-Security-Policy header applied to all responses
  Restricts which scripts can run on the page
  Prevents injected malicious scripts from executing

---

## How Authentication Works

  1. User fills in the registration form
  2. JavaScript sends POST request to /api/auth/register
  3. Spring Boot receives the request and hashes the password
  4. Hashed user data is saved to MySQL
  5. User fills in the login form
  6. JavaScript sends POST request to /api/auth/login
  7. Spring Boot finds the user and compares the password hash
  8. If correct, a JWT token is generated and returned
  9. JavaScript stores the token in localStorage
  10. Dashboard page reads the token and calls /api/auth/validate
  11. Spring Boot confirms the token is valid
  12. Dashboard displays the user information and security status

---

## Frontend Pages

index.html
  Landing page with animated background
  Feature cards describing each security component
  Buttons linking to register and login pages

register.html
  Registration form with username, email, and password fields
  Real-time password strength meter with four levels
  Requirement checklist showing uppercase, lowercase, and number rules
  Form validation before submission

login.html
  Login form with username and password fields
  Show and hide password toggle button
  Error messages for invalid credentials
  Redirects to dashboard on successful login

dashboard.html
  Protected page that redirects to login if no token found
  Displays logged-in username and active JWT token
  Security checklist showing all protection features
  Copy token button for testing API calls
  Logout button that clears the token and redirects

---

## Future Improvements

- Multi-factor authentication using Google Authenticator
- Password reset via email verification
- Admin dashboard to view and manage registered users
- Rate limiting to block brute force login attempts
- Docker containerization for easy deployment
- Deployment to cloud platform such as AWS or Railway
- Refresh token mechanism to extend sessions

---

## Author

Tharushi Sandunika
GitHub - https://github.com/tharushisandunika

---

## License

This project is open source and available under the MIT License.
