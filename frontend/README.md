# ✈️ Travel Agency Frontend

This folder contains the **frontend application** for the Travel Agency project. The project is built with **React + TypeScript + Vite** and uses modern libraries and tools for UI, forms, state management, and API integration.

---

## 🛠️ Tech Stack

- **Framework:** `React` + `TypeScript` (via `Vite`)
- **Styling:** `TailwindCSS`
- **Routing:** `React Router DOM`
- **Forms & Validation:** `react-hook-form` + `Zod`
- **State Management:** `@reduxjs/toolkit` + `react-redux`
- **HTTP Requests:** `Axios`
- **Helper Libraries & Tools:**
  - `clsx` (conditional Tailwind classes)
  - `ESLint` + `Prettier` (code quality)

---

## 📁 Project Structure

```
src/
├── assets/         # 🖼️ Images, fonts, and other static assets
├── components/     # 🧩 Reusable UI components
├── ui/             # 🎨 Atomic UI elements (buttons, inputs, modals)
├── pages/          # 📄 Application pages (e.g. Home, Login)
├── services/       # 📞 API logic, Axios instances
├── store/          # 📦 Redux store & slices
├── hooks/          # 🎣 Custom React hooks
├── models/         # 🏷️ TypeScript interfaces & types
├── constants/      # ⚙️ App-wide constants (routes, configs, enums)
├── App.tsx
├── main.tsx
└── index.css
```

---

## 🚀 Installation

1.  Clone the repository:
    ```bash
    git clone <gitlab-repo-url>
    cd travel-agency-frontend-app
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Start the development server:
    ```bash
    npm run dev
    ```
4.  Open `http://localhost:5173` in your browser.
