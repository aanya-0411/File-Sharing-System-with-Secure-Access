# 🔐 File Sharing System with Secure Access

A secure file-sharing web application developed using **Java and Spring Boot** that allows users to upload, download, and share files through unique password-protected links.

## 📌 Project Overview

The File Sharing System with Secure Access is designed to make file sharing simple and more secure. Users can register, log in, upload files, download files, and generate password-protected sharing links.

The system also provides a 24-hour expiry mechanism for shared links to limit access time.

## ✨ Features

- **User Registration & Login:** Register and log in to the application.
- **Secure Password Storage:** Passwords are stored using BCrypt hashing.
- **File Upload:** Upload files to the server.
- **File Download:** Download uploaded files easily.
- **Secure File Sharing:** Generate unique UUID-based sharing links.
- **Password Protection:** Recipients must enter the correct password to download shared files.
- **Link Expiry:** Shared links are configured to expire after 24 hours.
- **User Session Management:** Maintains login sessions.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Backend development |
| Spring Boot | Backend framework |
| HTML | Webpage structure |
| CSS | Styling and design |
| JavaScript | Frontend functionality |
| Maven | Dependency and build management |
| BCrypt | Password hashing |

## 🏗️ System Architecture

The application follows a frontend-backend architecture.

1. User interacts with the frontend.
2. Frontend sends HTTP requests to the Spring Boot backend.
3. Backend processes file upload, download, authentication and sharing requests.
4. Uploaded files are stored in the server's `uploads` folder.
5. A unique sharing link is generated for the selected file.

## 🚀 How to Run the Project

### Prerequisites

- Java (version compatible with the project configuration)
- Git
- VS Code or any Java IDE

### Installation Steps

**1. Clone the repository**

```bash
git clone https://github.com/aanya-0411/File-Sharing-System-with-Secure-Access.git
```

**2. Navigate to the project folder**

```bash
cd File-Sharing-System-with-Secure-Access
```

**3. Run the Spring Boot application**

For Mac/Linux:

```bash
./mvnw spring-boot:run
```

For Windows:

```bash
mvnw.cmd spring-boot:run
```

**4. Open the application**

```text
http://localhost:8081/
```

## 🔗 Main API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | User registration |
| POST | `/api/auth/login` | User login |
| GET | `/api/auth/me` | Get current user |
| POST | `/api/auth/logout` | User logout |
| POST | `/api/upload` | Upload a file |
| GET | `/api/download` | Download a file |
| POST | `/api/share` | Generate a sharing link |
| GET | `/api/share/{token}` | Open a shared file link |
| POST | `/api/share/{token}/download` | Verify password and download file |

## 🔒 Security Features

- BCrypt password hashing.
- Unique UUID-based sharing tokens.
- Password verification before shared file download.
- Configured 24-hour sharing-link expiry.

## ⚠️ Current Limitations

- User accounts and sharing-link information are stored in application memory.
- Uploaded files are stored locally on the server.
- No database integration currently.
- No complete user-wise file ownership and access-control system.
- The application currently runs locally and is not deployed on a cloud server.

## 🔮 Future Enhancements

- Database integration for persistent user and file information.
- User-wise file ownership and access permissions.
- Cloud deployment.
- Improved file validation and security.
- File access logs and activity tracking.

## 👥 Team Members

**Aanya Budhiraja**
- Java backend development.
- Spring Boot REST APIs.
- File upload, download and sharing functionality.

**Ayush Singh**
- Frontend development using HTML, CSS and JavaScript.
- Frontend-backend integration.

**Both team members:** Testing and project demonstration.

## 🎯 Project Objective

To develop a simple and secure file-sharing platform that demonstrates the practical implementation of Java, Spring Boot, REST APIs and frontend-backend integration.

---

Developed as an academic project | BTech Computer Science and Engineering

## 🎥 Project Demo



https://github.com/user-attachments/assets/ba0a0795-1816-4952-b495-70571ff8b359



