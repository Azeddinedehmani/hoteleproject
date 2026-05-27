# 🏨 Hotel Management System

> Application complète de gestion hôtelière — Backend Spring Boot + Frontend React

---

## 📋 Description

Système de gestion hôtelière avec architecture Clean Architecture côté backend et React côté frontend. Permet la gestion des réservations, des chambres, des clients, des factures et des tarifs.

## 🧱 Stack Technique

| Couche      | Technologie                        |
|-------------|-----------------------------------|
| Backend     | Java 17 · Spring Boot 3.2.5 · Maven |
| Sécurité    | Spring Security · JWT             |
| Base de données | MySQL · JPA / Hibernate       |
| Frontend    | React · Vite · JSX                |
| Architecture | Clean Architecture (UseCases, Domain, Infrastructure) |

---

## 📁 Structure du Projet

```
hoteleproject/
├── src/                          # Backend Spring Boot
│   └── main/
│       ├── java/com/hotel/
│       │   ├── application/      # DTOs, UseCases
│       │   ├── domain/           # Entités, Repositories (interfaces)
│       │   └── infrastructure/   # Config, Persistence, Security
│       └── resources/
│           └── application.properties
│
└── srcbackgnd/                   # Frontend React
    ├── src/
│   ├── pages/
│   │   ├── admin/            # Dashboard admin, Chambres, Tarifs, Utilisateurs
│   │   ├── receptionist/     # Dashboard réceptionniste, Réservations
│   │   └── client/           # Dashboard client, Réservations, Factures
│   ├── services/             # Appels API (axios)
│   └── routes/               # Routing (React Router)
    └── pom.xml               # Dépendances frontend (Vite)
```

---

## ⚙️ Prérequis

- **Java 17+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Node.js 18+** et **npm**

---

## 🚀 Installation & Lancement

### 1. Cloner le projet

```bash
git clone https://github.com/Azeddinedehmani/hoteleproject.git
cd hoteleproject
```

### 2. Configurer la base de données MySQL

```sql
CREATE DATABASE hotel_db;
```

Modifier `src/main/resources/application.properties` si besoin :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_db
spring.datasource.username=root
spring.datasource.password=VOTRE_MOT_DE_PASSE
```

### 3. Lancer le Backend

```bash
# Depuis la racine du projet
mvn spring-boot:run
```

Le serveur démarre sur : `http://localhost:8080/api`

### 4. Lancer le Frontend

```bash
cd srcbackgnd
npm install
npm run dev
```

L'application est accessible sur : `http://localhost:5173`

---

## 🔐 Comptes par défaut

> ⚠️ **Changer ces mots de passe en production !**

| Rôle           | Email                  | Mot de passe  |
|----------------|------------------------|---------------|
| Admin          | admin@hotel.com        | Admin@1234    |
| Réceptionniste | reception@hotel.com    | Recep@1234    |
| Client (test)  | client@hotel.com       | Client@1234   |

---

## 🗂️ Fonctionnalités

### 👨‍💼 Admin
- Gestion des utilisateurs (CRUD)
- Gestion des chambres et équipements
- Configuration des tarifs
- Tableau de bord

### 🛎️ Réceptionniste
- Gestion des réservations
- Gestion des clients
- Génération de factures

### 👤 Client
- Réservation de chambres
- Consultation des réservations
- Accès aux factures

---

## 🌐 API REST — Endpoints principaux

| Méthode | Endpoint                  | Description              |
|---------|---------------------------|--------------------------|
| POST    | `/api/auth/login`         | Connexion                |
| POST    | `/api/auth/register`      | Inscription              |
| GET     | `/api/rooms`              | Liste des chambres       |
| POST    | `/api/reservations`       | Créer une réservation    |
| GET     | `/api/invoices`           | Liste des factures       |
| GET     | `/api/users`              | Liste des utilisateurs   |

---

## 📧 Configuration Email (optionnel)

Dans `application.properties`, configurer le serveur SMTP :

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre@email.com
spring.mail.password=VOTRE_APP_PASSWORD
```

---



## 📄 Licence

Ce projet est développé à des fins académiques / personnelles.
