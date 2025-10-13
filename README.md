# 🌍 Travel Agency App — Team 3 (Run 15)

This repository contains the **Travel Agency Management Application**, developed as part of **EPAM Project Education (Run 15)**.  
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
No direct pushes are allowed to `main` or `develop`.

---

## 🚀 Getting Started

### Clone the repository
```bash
git clone git@git.epam.com:epm-edai/project-runs/run-15/team-3/serverless/travel-agency-app.git
cd travel-agency-app