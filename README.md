# 🎟️ OptiLock - Real-Time Event Ticket Booking System

A full-stack event ticket booking platform built with **Spring Boot**, **React**, and **PostgreSQL** that prevents double booking using **Optimistic Locking** and provides **real-time seat availability updates** using **Server-Sent Events (SSE)**.

---

## 🚀 Features

- 🔐 JWT-based Authentication & Authorization
- 👤 User Registration & Login
- 🎫 Browse Available Events
- 💺 Interactive Seat Selection
- ⚡ Real-Time Seat Updates (Server-Sent Events)
- 🔄 Optimistic Locking to Prevent Double Booking
- 🛡️ Global Exception Handling
- 🗄️ PostgreSQL Database
- 📦 Flyway Database Migrations
- 🧪 Concurrency Testing with JUnit

---

## 🛠️ Tech Stack

### Backend
- Java 21+
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- JWT Authentication
- Lombok

### Frontend
- React
- Vite
- Axios
- React Router
- Tailwind CSS

### Tools
- IntelliJ IDEA
- Postman
- Git & GitHub
- pgAdmin

---

## 🏗️ System Architecture

```text
                 +---------------------+
                 |     React Frontend  |
                 +----------+----------+
                            |
                     REST APIs / SSE
                            |
                 +----------v----------+
                 |   Spring Boot API   |
                 +----------+----------+
                            |
                 Spring Data JPA / Hibernate
                            |
                 +----------v----------+
                 |    PostgreSQL DB    |
                 +---------------------+
```

---

## 📂 Project Structure

```text
OptiLock-Ticketing
│
├── backend
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── security
│   ├── exception
│   ├── dto
│   ├── event
│   └── sse
│
└── frontend
    ├── components
    ├── context
    ├── pages
    ├── api
    └── App.jsx
```

---

## 🔐 Authentication

- JWT-based authentication
- BCrypt password hashing
- Protected REST APIs using Spring Security
- Stateless session management

---

## ⚡ Concurrency Handling

To prevent multiple users from booking the same seat simultaneously, the application uses **Hibernate Optimistic Locking**.

Each seat entity contains a `@Version` field.

If multiple users attempt to reserve the same seat:

- ✅ First transaction succeeds
- ❌ Remaining transactions fail with **HTTP 409 Conflict**

This prevents race conditions without explicit database locks.

---

## 📡 Real-Time Updates

The application uses **Server-Sent Events (SSE)** to notify connected users whenever a seat status changes.

Benefits:

- No page refresh required
- Instant synchronization across browsers
- Lightweight compared to WebSockets for one-way updates

---

## 🧪 Concurrency Testing

A multithreaded JUnit test sends **50 concurrent booking requests** for the same seat.

Expected Result:

- ✅ Exactly one booking succeeds
- ✅ Remaining requests receive HTTP **409 Conflict**

This validates the optimistic locking implementation.

---

## 📷 Screenshots

### Login

> Add screenshot here

```
docs/images/login.png
```

---

### Dashboard

> Add screenshot here

```
docs/images/dashboard.png
```

---

### Seat Selection

> Add screenshot here

```
docs/images/event-page.png
```

---

### Real-Time Seat Updates

> Add screenshot here

```
docs/images/live-update.png
```

---

## 📡 REST API Endpoints

### Authentication

| Method | Endpoint |
|---------|----------|
| POST | `/api/auth/register` |
| POST | `/api/auth/login` |

### Events

| Method | Endpoint |
|---------|----------|
| GET | `/api/events` |
| GET | `/api/events/{id}` |

### Booking

| Method | Endpoint |
|---------|----------|
| POST | `/api/bookings` |

### Real-Time Updates

| Method | Endpoint |
|---------|----------|
| GET | `/api/events/{id}/stream` |

---

## ⚙️ Running the Project

### Clone Repository

```bash
git clone https://github.com/Anwesha0101/OptiLock-Ticketing.git
```

---

### Backend

```bash
cd backend
```

Configure PostgreSQL in `application.properties`.

Run:

```bash
./mvnw spring-boot:run
```

Backend runs on:

```
http://localhost:8080
```

---

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on:

```
http://localhost:5173
```

---

## 📈 Future Improvements

- Docker support
- Kubernetes deployment
- Redis caching
- Payment gateway integration
- Email confirmations
- Admin dashboard
- Seat hold timer
- QR Code ticket generation

---

## 👩‍💻 Author

**Anwesha Arya**

GitHub: https://github.com/Anwesha0101

---

## ⭐ If you found this project useful, consider giving it a star!