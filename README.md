# 🌍 Travel Agency App

This repository contains the **Travel Agency Management Application**.  
The application is built using a **serverless architecture on AWS** with Java (backend), JavaScript (frontend), and QA automation.

---

## 📖 Business Context
The Travel Agency application automates core processes of an online travel agency:
- **Customers** can register, browse available tours (flights, accommodations, activities), view tour details, and make bookings.
- **Managers** generate **TA operations reports** that track:
    - Tour diversity
    - Customer satisfaction
    - Staff performance

The system is designed to be:
- **Automated** → reduces manual work
- **Scalable** → serverless architecture grows with demand
- **Customizable** → adaptable to multiple agency networks

---

## 🔀 Git Workflow (Git Flow)
We follow **Git Flow** to ensure clean development:
- `main` → **production-ready**, protected branch
- `develop` → **active development**, protected branch
- `feature/*` → new features (e.g., `feature/user-login`)
- `fix/*` → bug fixes (e.g., `fix/login-error`)
- `release/*` → release preparation branches

⚠️ All changes must go through **Merge Requests (MRs)**.  
No direct pushes are allowed to `master` or `develop`.

---

## Spring Boot Migration (Java 21 + PostgreSQL)

A new backend module is available at `backend/travel-springboot-app` as the migration target from AWS Lambda/Syndicate.

### Included in the new backend

- Spring Boot 3 application on Java 21
- PostgreSQL persistence via Spring Data JPA
- Flyway schema migration (`V1__init_schema.sql`)
- JWT-based auth (`/auth/sign-up`, `/auth/sign-in`)
- Core migrated endpoints for tours, bookings, users, and admin travel-agent management

### Run the migrated backend

```powershell
cd D:\Projects\travel-agency-application\backend\travel-springboot-app
mvn spring-boot:run
```

### Database and auth environment variables

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`

For module-level details, see `backend/travel-springboot-app/README.md`.
