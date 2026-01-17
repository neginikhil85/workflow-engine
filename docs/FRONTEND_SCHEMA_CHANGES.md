# Frontend Schema Changes - Instructions for Gemini

This document describes the frontend changes needed to match the backend schema updates.

## Context

Backend has been updated with:
1. New `User` entity with REST endpoints at `/api/users`
2. `WorkflowDefinition` now has `ownerId` field (instead of `createdBy`)
3. **OAuth2 Login with Google and GitHub**
4. JWT-based authentication

---

## Part 1: Type Changes

### 1. Add User Interface

**File:** `src/types/workflow.interfaces.ts`

Add this interface:

```typescript
export interface User {
    id: string;
    email: string;
    name: string;
    createdAt?: string;
    updatedAt?: string;
    active: boolean;
}
```

### 2. Update Workflow Interface

**File:** `src/types/workflow.interfaces.ts`

Update `Workflow` interface to include `ownerId`:

```typescript
export interface Workflow {
    id: string | null;
    name: string;
    description: string;
    ownerId?: string;  // NEW - Owner's user ID
    nodes: WorkflowNodeDefinition[];
    edges: WorkflowEdge[];
}
```

---

## Part 2: Authentication Service

### Create Auth Service

**File:** `src/services/auth.service.ts` (NEW FILE)

```typescript
import axios from 'axios';
import { User } from '../types/workflow.interfaces';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Store token in localStorage
const TOKEN_KEY = 'workflow_studio_token';

export const authService = {
    // Get stored token
    getToken: () => localStorage.getItem(TOKEN_KEY),
    
    // Store token after OAuth callback
    setToken: (token: string) => localStorage.setItem(TOKEN_KEY, token),
    
    // Remove token on logout
    removeToken: () => localStorage.removeItem(TOKEN_KEY),
    
    // Check if user is logged in
    isLoggedIn: () => !!localStorage.getItem(TOKEN_KEY),
    
    // Get current user from token
    getCurrentUser: async (): Promise<User> => {
        const token = localStorage.getItem(TOKEN_KEY);
        const response = await axios.get(`${API_BASE}/auth/me`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data.data;
    },
    
    // Validate token
    validateToken: async (token: string) => {
        const response = await axios.post(`${API_BASE}/auth/validate`, { token });
        return response.data.data;
    },
    
    // Refresh token
    refreshToken: async (): Promise<string> => {
        const token = localStorage.getItem(TOKEN_KEY);
        const response = await axios.post(`${API_BASE}/auth/refresh`, {}, {
            headers: { Authorization: `Bearer ${token}` }
        });
        const newToken = response.data.data.token;
        localStorage.setItem(TOKEN_KEY, newToken);
        return newToken;
    },
    
    // OAuth login URLs
    getGoogleLoginUrl: () => `${API_BASE}/oauth2/authorization/google`,
    getGithubLoginUrl: () => `${API_BASE}/oauth2/authorization/github`,
    
    // Logout
    logout: () => {
        localStorage.removeItem(TOKEN_KEY);
        window.location.href = '/login';
    }
};

// Add auth header to all axios requests
axios.interceptors.request.use((config) => {
    const token = authService.getToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
```

---

## Part 3: Beautiful OAuth Login/Signup UI

### Design Requirements

**CRITICAL: Create a BEAUTIFUL, PREMIUM login experience that WOWs users at first glance!**

**Theme to Match (from WorkflowLayout.tsx):**
- Background: `bg-slate-50` (light mode)
- Text colors: `text-slate-900`, `text-slate-600`, `text-slate-400`
- Accent: `bg-slate-100`, `border-slate-200`
- Font: System font-sans

**Design Inspiration:**
1. **Gradient Background** - Subtle gradient from slate-100 to white
2. **Glassmorphism Card** - Frosted glass login card with subtle blur and shadow
3. **Micro-animations** - Smooth hover effects, button transitions
4. **Professional Typography** - Clear hierarchy, good spacing
5. **Premium Feel** - Like Linear, Vercel, or Notion login pages

---

### Create Login Page

**File:** `src/pages/LoginPage.tsx` (NEW FILE)

```tsx
import React from 'react';
import { authService } from '../services/auth.service';

const LoginPage: React.FC = () => {
    const handleGoogleLogin = () => {
        window.location.href = authService.getGoogleLoginUrl();
    };
    
    const handleGithubLogin = () => {
        window.location.href = authService.getGithubLoginUrl();
    };
    
    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-100 via-white to-slate-50">
            {/* Background decoration */}
            <div className="absolute inset-0 overflow-hidden">
                <div className="absolute -top-40 -right-40 w-96 h-96 bg-blue-100 rounded-full opacity-30 blur-3xl"></div>
                <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-purple-100 rounded-full opacity-30 blur-3xl"></div>
            </div>

            {/* Login Card - Glassmorphism */}
            <div className="relative z-10 w-full max-w-md mx-4">
                <div className="bg-white/80 backdrop-blur-xl rounded-2xl shadow-xl shadow-slate-200/50 border border-slate-100 p-8">
                    
                    {/* Logo & Title */}
                    <div className="text-center mb-8">
                        <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-slate-800 to-slate-600 rounded-xl mb-4 shadow-lg">
                            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                            </svg>
                        </div>
                        <h1 className="text-2xl font-bold text-slate-900">Workflow Studio</h1>
                        <p className="text-slate-500 mt-2">Build powerful workflows visually</p>
                    </div>
                    
                    {/* OAuth Buttons */}
                    <div className="space-y-3">
                        <button
                            onClick={handleGoogleLogin}
                            className="w-full flex items-center justify-center gap-3 bg-white text-slate-700 py-3.5 px-4 rounded-xl border border-slate-200 hover:bg-slate-50 hover:border-slate-300 transition-all duration-200 font-medium shadow-sm hover:shadow group"
                        >
                            <svg className="w-5 h-5" viewBox="0 0 24 24">
                                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                            </svg>
                            Continue with Google
                        </button>
                        
                        <button
                            onClick={handleGithubLogin}
                            className="w-full flex items-center justify-center gap-3 bg-slate-900 text-white py-3.5 px-4 rounded-xl hover:bg-slate-800 transition-all duration-200 font-medium shadow-sm hover:shadow-lg"
                        >
                            <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                                <path fillRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clipRule="evenodd"/>
                            </svg>
                            Continue with GitHub
                        </button>
                    </div>
                    
                    {/* Divider */}
                    <div className="relative my-8">
                        <div className="absolute inset-0 flex items-center">
                            <div className="w-full border-t border-slate-200"></div>
                        </div>
                        <div className="relative flex justify-center text-sm">
                            <span className="px-4 bg-white text-slate-400">Secure authentication</span>
                        </div>
                    </div>
                    
                    {/* Features */}
                    <div className="grid grid-cols-3 gap-4 text-center">
                        <div className="p-3">
                            <div className="text-slate-400 text-xs font-medium mb-1">🔒</div>
                            <div className="text-slate-500 text-xs">Secure</div>
                        </div>
                        <div className="p-3">
                            <div className="text-slate-400 text-xs font-medium mb-1">⚡</div>
                            <div className="text-slate-500 text-xs">Fast</div>
                        </div>
                        <div className="p-3">
                            <div className="text-slate-400 text-xs font-medium mb-1">🎯</div>
                            <div className="text-slate-500 text-xs">Simple</div>
                        </div>
                    </div>
                </div>
                
                {/* Footer */}
                <p className="text-center text-slate-400 text-sm mt-6">
                    By signing in, you agree to our Terms of Service
                </p>
            </div>
        </div>
    );
};

export default LoginPage;
```

---

### Create Auth Callback Page

**File:** `src/pages/AuthCallbackPage.tsx` (NEW FILE)

```tsx
import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { authService } from '../services/auth.service';

const AuthCallbackPage: React.FC = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [error, setError] = useState(false);
    
    useEffect(() => {
        const token = searchParams.get('token');
        
        if (token) {
            authService.setToken(token);
            navigate('/');
        } else {
            setError(true);
            setTimeout(() => navigate('/login'), 2000);
        }
    }, [searchParams, navigate]);
    
    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-100 via-white to-slate-50">
            <div className="text-center">
                {error ? (
                    <>
                        <div className="text-red-500 text-5xl mb-4">✕</div>
                        <div className="text-slate-600 text-lg">Authentication failed</div>
                        <div className="text-slate-400 text-sm mt-2">Redirecting to login...</div>
                    </>
                ) : (
                    <>
                        {/* Animated loader */}
                        <div className="relative w-16 h-16 mx-auto mb-6">
                            <div className="absolute inset-0 border-4 border-slate-200 rounded-full"></div>
                            <div className="absolute inset-0 border-4 border-slate-800 border-t-transparent rounded-full animate-spin"></div>
                        </div>
                        <div className="text-slate-600 text-lg font-medium">Signing you in...</div>
                        <div className="text-slate-400 text-sm mt-2">Please wait</div>
                    </>
                )}
            </div>
        </div>
    );
};

export default AuthCallbackPage;
```

### Create Auth Context

**File:** `src/contexts/AuthContext.tsx` (NEW FILE)

```tsx
import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User } from '../types/workflow.interfaces';
import { authService } from '../services/auth.service';

interface AuthContextType {
    user: User | null;
    loading: boolean;
    isAuthenticated: boolean;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);
    
    useEffect(() => {
        const loadUser = async () => {
            if (authService.isLoggedIn()) {
                try {
                    const currentUser = await authService.getCurrentUser();
                    setUser(currentUser);
                } catch (error) {
                    authService.removeToken();
                }
            }
            setLoading(false);
        };
        
        loadUser();
    }, []);
    
    const logout = () => {
        authService.logout();
        setUser(null);
    };
    
    return (
        <AuthContext.Provider value={{ 
            user, 
            loading, 
            isAuthenticated: !!user,
            logout 
        }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
};
```

### Create Protected Route

**File:** `src/components/ProtectedRoute.tsx` (NEW FILE)

```tsx
import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

interface ProtectedRouteProps {
    children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
    const { isAuthenticated, loading } = useAuth();
    
    if (loading) {
        return <div className="min-h-screen flex items-center justify-center bg-gray-900">
            <div className="text-white">Loading...</div>
        </div>;
    }
    
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }
    
    return <>{children}</>;
};

export default ProtectedRoute;
```

### Update Routes

**File:** `src/App.tsx` or `src/routes.tsx`

Add these routes:

```tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import AuthCallbackPage from './pages/AuthCallbackPage';
// ... other imports

function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <Routes>
                    {/* Public routes */}
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/auth/callback" element={<AuthCallbackPage />} />
                    
                    {/* Protected routes */}
                    <Route path="/" element={
                        <ProtectedRoute>
                            <Dashboard /> {/* or your main component */}
                        </ProtectedRoute>
                    } />
                    {/* ... other protected routes */}
                </Routes>
            </BrowserRouter>
        </AuthProvider>
    );
}
```

---

## Part 4: User API Service

### Create User Service

**File:** `src/services/user.service.ts` (NEW FILE)

```typescript
import axios from 'axios';
import { User, Workflow } from '../types/workflow.interfaces';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const userApi = {
    getAll: () => 
        axios.get<{ data: User[] }>(`${API_BASE}/api/users`),
    
    getById: (id: string) => 
        axios.get<{ data: User }>(`${API_BASE}/api/users/${id}`),
    
    getWorkflows: (userId: string) => 
        axios.get<{ data: Workflow[] }>(`${API_BASE}/api/users/${userId}/workflows`),
};
```

---

## Backend API Endpoints Summary

### Auth Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/oauth2/authorization/google` | Start Google OAuth flow |
| GET | `/oauth2/authorization/github` | Start GitHub OAuth flow |
| GET | `/auth/me` | Get current user (requires JWT) |
| POST | `/auth/validate` | Validate a JWT token |
| POST | `/auth/refresh` | Refresh JWT token |

### User Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create new user |
| PUT | `/api/users/{id}` | Update user |
| GET | `/api/users/{id}/workflows` | Get workflows owned by user |

---

## OAuth Flow Summary

```
1. User clicks "Continue with Google/GitHub"
2. Browser redirects to: /oauth2/authorization/google (or github)
3. Backend redirects to Google/GitHub login page
4. User logs in with provider
5. Provider redirects back to backend
6. Backend creates/finds user, generates JWT
7. Backend redirects to: http://localhost:5173/auth/callback?token=xxxxx
8. Frontend stores token in localStorage
9. Frontend redirects to home page
10. All API calls include Authorization: Bearer <token> header
```

---

## Code Style to Follow

1. **Types** go in `src/types/` directory
2. **Services** go in `src/services/` directory
3. **Contexts** go in `src/contexts/` directory
4. **Pages** go in `src/pages/` directory
5. Use existing axios patterns
6. Follow existing TypeScript patterns
7. Use Tailwind CSS for styling
8. Match existing dark theme
