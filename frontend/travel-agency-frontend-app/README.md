# Travel Agency Frontend Application

## Overview

A modern, responsive web application for managing travel tours, bookings, and user profiles. Built with React, TypeScript, Redux, and Tailwind CSS.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Available Scripts](#available-scripts)
- [Routing](#routing)
- [State Management](#state-management)
- [Components](#components)
- [Pages](#pages)
- [Services](#services)
- [Authentication](#authentication)
- [Role-Based Access](#role-based-access)
- [Styling](#styling)
- [Environment Configuration](#environment-configuration)

## Features

### Customer Features
- Browse and search tours with filters (destination, date, duration, meal plan, price)
- View detailed tour information
- Book tours with guest information
- View and manage bookings
- Upload travel documents
- Post reviews and ratings
- Update profile (name, password, profile picture)
- AI-powered travel assistant chat

### Travel Agent Features
- All customer features
- Create, edit, and delete tours
- View bookings for their tours
- Confirm bookings
- Manage tour inventory

### Admin Features
- All travel agent features
- Create and manage travel agent accounts
- View all bookings across the platform
- Full access to all tours

## Tech Stack

- **Framework**: React 19.1.1
- **Language**: TypeScript 5.8.3
- **State Management**: Redux Toolkit
- **Routing**: React Router DOM 7.8.2
- **Form Handling**: React Hook Form 7.62.0
- **Validation**: Zod 4.1.5
- **HTTP Client**: Axios 1.11.0
- **Styling**: Tailwind CSS 4.1.13
- **Icons**: Lucide React 0.544.0
- **Date Handling**: date-fns 4.1.0, react-date-range 2.0.1
- **Build Tool**: Vite 7.1.2
- **Testing**: Vitest, React Testing Library

## Project Structure

```
src/
├── assets/              # Static assets (images, icons)
├── components/          # Reusable UI components
│   ├── modals/         # Modal components
│   ├── Button/         # Button component
│   ├── Header/         # Navigation header
│   └── ...
├── constants.ts         # Application constants
├── hooks/               # Custom React hooks
├── layouts/             # Layout components
│   ├── AuthLayout.tsx  # Layout for auth pages
│   ├── MainLayout.tsx  # Main application layout
│   └── ProfileLayout.tsx # Profile pages layout
├── models/             # TypeScript type definitions
├── pages/              # Page components
│   ├── MainPage.tsx    # Home/tours listing page
│   ├── LoginPage.tsx   # Login page
│   ├── RegisterPage.tsx # Registration page
│   ├── TourDetailsPage.tsx # Tour details page
│   ├── BookingsPage.tsx # Bookings management
│   ├── ProfilePage.tsx # User profile
│   └── ...
├── schemas/            # Zod validation schemas
├── services/           # API service functions
│   ├── api.ts         # Axios instance configuration
│   ├── fetchTours.ts  # Tour-related API calls
│   ├── bookTour.ts    # Booking API calls
│   └── ...
├── store/             # Redux store configuration
│   ├── store.ts       # Store setup
│   ├── user/          # User state slice
│   ├── modal/         # Modal state slice
│   └── ...
└── ui/                # Base UI components
    ├── Input/         # Input component
    ├── Modal/         # Modal component
    └── ...
```

## Getting Started

### Prerequisites

- Node.js 18+ and npm
- Backend API running and accessible

### Installation

1. Clone the repository
2. Navigate to the frontend directory:
   ```bash
   cd frontend/travel-agency-frontend-app
   ```

3. Install dependencies:
   ```bash
   npm install
   ```

4. Configure environment variables (see [Environment Configuration](#environment-configuration))

5. Start the development server:
   ```bash
   npm run dev
   ```

6. Open your browser to `http://localhost:5173` (or the port shown in terminal)

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

## Routing

The application uses React Router with role-based route protection.

### Public Routes
- `/` - Home page (tours listing)
- `/login` - Login page
- `/register` - Registration page
- `/forgot-password` - Password recovery
- `/tours/:id` - Tour details (public)

### Protected Routes (Require Authentication)
- `/my-tours` - User's bookings (CUSTOMER)
- `/bookings` - Bookings management (CUSTOMER, TRAVEL_AGENT, ADMIN)
- `/profile` - User profile
- `/profile/change-password` - Change password
- `/agent/tours` - Travel agent's tours (TRAVEL_AGENT, ADMIN)
- `/admin/travel-agents` - Travel agents management (ADMIN only)
- `/reports` - Reports page (ADMIN)

### Route Protection

Routes are protected using `PrivateRoute`, `RoleRoute`, and `RoleRedirect` components:

```tsx
<Route
  path="/agent/tours"
  element={
    <RoleRoute allowedRoles={["TRAVEL_AGENT", "ADMIN"]}>
      <TravelAgentToursPage />
    </RoleRoute>
  }
/>
```

## State Management

The application uses Redux Toolkit for state management.

### Store Slices

#### User Slice (`store/user/`)
- User authentication state
- User profile information
- Role management
- Token storage

#### Modal Slice (`store/modal/`)
- Active modal state
- Modal props
- Modal open/close actions

#### Filter Slices
- `store/location/` - Selected destination filter
- `store/dates/` - Date range filter
- `store/guests/` - Guest count filter
- `store/meal/` - Meal plan filter
- `store/tourTypes/` - Tour type filter

### Usage Example

```tsx
import { useSelector, useDispatch } from 'react-redux';
import { RootState } from './store/store';
import { setLocation } from './store/location/locationSlice';

const MyComponent = () => {
  const location = useSelector((state: RootState) => state.location.value);
  const dispatch = useDispatch();
  
  const handleLocationChange = (newLocation: string) => {
    dispatch(setLocation(newLocation));
  };
  
  return <div>Current location: {location}</div>;
};
```

## Components

### Reusable Components

#### Button
```tsx
import Button from './components/Button';

<Button variant="primary" size="medium" onClick={handleClick}>
  Click Me
</Button>
```

#### Input
```tsx
import Input from './ui/Input';

<Input
  id="email"
  name="email"
  label="Email"
  type="email"
  placeholder="Enter your email"
  error={errors.email?.message}
/>
```

#### Modal
```tsx
import Modal from './ui/Modal';

<Modal isOpen={isOpen} onClose={handleClose} className="md:w-[544px]">
  <div className="p-6">
    <h2>Modal Title</h2>
    {/* Modal content */}
  </div>
</Modal>
```

### Feature Components

- **BookingFormModal**: Form for creating bookings
- **CreateTourModal**: Form for creating tours (Travel Agents)
- **EditTourModalForTours**: Form for editing tours
- **UploadDocsModal**: Document upload interface
- **FeedbackModal**: Review submission form

## Pages

### MainPage
Home page displaying available tours with search and filter functionality.

**Features:**
- Tour listing with pagination
- Search by destination, date, duration, meal plan, price
- Tour cards with images, ratings, and prices
- Navigation to tour details

### TourDetailsPage
Detailed view of a single tour.

**Features:**
- Full tour information
- Image gallery
- Date and duration selection
- Meal plan options
- Pricing calculator
- Reviews section
- Booking button

### BookingsPage
Manage user bookings.

**Features:**
- List of all bookings
- Booking status (PENDING, CONFIRMED, CANCELLED)
- Document upload
- Booking cancellation
- Booking confirmation (for agents)

### ProfilePage
User profile management.

**Features:**
- Display user information
- Edit name
- Upload profile picture
- View role

### TravelAgentToursPage
Travel agent tour management.

**Features:**
- List of created tours
- Create new tour
- Edit existing tour
- Delete tour
- View tour details

### AdminTravelAgentsPage
Admin interface for managing travel agents.

**Features:**
- List all travel agents
- Create new travel agent
- Delete travel agent

## Services

API service functions are located in `src/services/`. Each service handles a specific domain:

### Authentication Services
- `api.ts` - Axios instance with base configuration

### Tour Services
- `fetchTours.ts` - Get available tours with filters
- `createTour.ts` - Create new tour
- `updateTour.ts` - Update existing tour
- `deleteTour.ts` - Delete tour
- `getMyTours.ts` - Get agent's tours

### Booking Services
- `bookTour.ts` - Create booking
- `getBookings.ts` - Get user bookings
- `updateBooking.ts` - Update booking
- `CancelBooking.ts` - Cancel booking
- `uploadDocuments.ts` - Upload documents
- `getDocuments.ts` - Get booking documents

### User Services
- `getUserInfo.ts` - Get user profile
- `updateUserName.ts` - Update name
- `updatePassword.ts` - Update password
- `updateUserImage.ts` - Update profile picture

### Travel Agent Services
- `createTravelAgent.ts` - Create agent (Admin)
- `listTravelAgents.ts` - List agents (Admin)
- `deleteTravelAgent.ts` - Delete agent (Admin)

### Example Usage

```tsx
import { fetchTours } from './services/fetchTours';

const MyComponent = () => {
  const [tours, setTours] = useState([]);
  
  useEffect(() => {
    const loadTours = async () => {
      const token = getTokenFromStorage();
      const data = await fetchTours(token, {
        page: 0,
        size: 10,
        destination: 'Paris'
      });
      setTours(data.tours);
    };
    loadTours();
  }, []);
  
  return <div>{/* Render tours */}</div>;
};
```

## Authentication

### Login Flow

1. User submits credentials on `/login`
2. Frontend calls `/auth/sign-in` API
3. Backend returns JWT token and user info
4. Token stored in localStorage and Redux store
5. User redirected based on role:
   - CUSTOMER → Home page
   - TRAVEL_AGENT → `/agent/tours`
   - ADMIN → `/admin/travel-agents`

### Token Management

Tokens are stored in localStorage under the key `"user"`:

```json
{
  "token": "eyJraWQiOiJ...",
  "email": "user@example.com",
  "role": "CUSTOMER",
  "userName": "John Doe"
}
```

### Protected API Calls

All protected API calls include the token in the Authorization header:

```tsx
const response = await api.get('/tours/my', {
  headers: {
    Authorization: `Bearer ${token}`
  }
});
```

## Role-Based Access

### Role Definitions

- **CUSTOMER**: Regular users who can book tours
- **TRAVEL_AGENT**: Can create and manage tours
- **ADMIN**: Full system access

### Role-Based UI

Components check user role to show/hide features:

```tsx
const { role } = useSelector((state: RootState) => state.user);

{role === 'TRAVEL_AGENT' && (
  <Button onClick={handleCreateTour}>Create Tour</Button>
)}
```

### Navigation

Header navigation adapts based on role:

```tsx
const getNavigationItems = (role: UserRole) => {
  switch (role) {
    case "CUSTOMER":
      return [
        { label: "All tours", href: "/" },
        { label: "My tours", href: "/my-tours" },
      ];
    case "TRAVEL_AGENT":
      return [
        { label: "All tours", href: "/" },
        { label: "My Tours", href: "/agent/tours" },
        { label: "Bookings", href: "/bookings" },
      ];
    // ...
  }
};
```

## Styling

The application uses Tailwind CSS for styling.

### Design System

- **Colors**: Defined in `tailwind.config.js` (blue-01 through blue-09, grey-01 through grey-07)
- **Typography**: Custom font classes (h1, h2, h3, body, caption, navigation)
- **Spacing**: Tailwind spacing scale
- **Components**: Consistent button, input, and card styles

### Custom Classes

```css
.h1, .h2, .h3 - Heading styles
.body, .body-bold - Body text styles
.caption - Small text
.navigation - Navigation link style
```

### Responsive Design

Uses Tailwind responsive prefixes:
- `md:` - Medium screens and up
- `lg:` - Large screens and up

Example:
```tsx
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
  {/* Responsive grid */}
</div>
```

## Environment Configuration

Create a `.env` file in the root directory:

```env
VITE_API_BASE_URL=https://your-api-gateway-url.execute-api.region.amazonaws.com/dev
```

The API base URL is used in `src/services/api.ts`:

```ts
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000',
});
```

## Form Validation

Forms use React Hook Form with Zod validation schemas.

### Example Schema

```ts
// schemas/registerSchema.ts
import { z } from 'zod';

export const registerSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
});
```

### Usage in Component

```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { registerSchema } from './schemas/registerSchema';

const MyForm = () => {
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm({
    resolver: zodResolver(registerSchema)
  });
  
  const onSubmit = (data) => {
    // Handle form submission
  };
  
  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Input {...register('email')} error={errors.email?.message} />
      {/* More fields */}
    </form>
  );
};
```

## Error Handling

API errors are handled consistently:

```tsx
try {
  const data = await fetchTours(token, params);
  // Handle success
} catch (error) {
  if (error.response) {
    // API error
    console.error('API Error:', error.response.data);
  } else {
    // Network error
    console.error('Network Error:', error.message);
  }
}
```

## Testing

Run tests with:
```bash
npm test
```

Test files are located alongside components with `.test.tsx` extension.

## Building for Production

1. Update environment variables
2. Build the application:
   ```bash
   npm run build
   ```
3. The `dist/` folder contains the production build
4. Deploy to your hosting service (e.g., AWS S3 + CloudFront, Vercel, Netlify)

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Troubleshooting

### Common Issues

1. **API calls failing**: Check that `VITE_API_BASE_URL` is set correctly
2. **Authentication not working**: Verify token is stored in localStorage
3. **CORS errors**: Ensure backend CORS is configured correctly
4. **Build errors**: Clear `node_modules` and reinstall dependencies

## Contributing

1. Follow the existing code style
2. Use TypeScript for all new code
3. Add tests for new features
4. Update documentation as needed

## License

[Your License Here]

## Support

For issues or questions, please contact the development team.
