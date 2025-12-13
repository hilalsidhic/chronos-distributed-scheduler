import { API_BASE_URL } from "@/config"; // Import the constant

// Define interfaces
export interface User {
  id: string; 
  email: string; 
  name: string;
}

export interface AuthResponse {
  user: User;
  token: string;
}

const STORAGE_KEY = "auth_user";
const TOKEN_KEY = "auth_token"; 

export const authService = {
  // 1. LOGIN
  async login(email: string, password: string): Promise<AuthResponse> {
    const credentials = btoa(`${email}:${password}`);
    const token = `Basic ${credentials}`;

    // Used API_BASE_URL here (Correct)
    const response = await fetch(`${API_BASE_URL}/scheduler/jobs?limit=1`, {
      method: "GET",
      headers: {
        Authorization: token,
      },
    });

    if (!response.ok) {
      if (response.status === 401) throw new Error("Invalid credentials");
      throw new Error("Login failed");
    }

    const user: User = {
      id: "0", 
      email: email,
      name: email.split("@")[0], 
    };

    this.setSession(user, token);
    return { user, token };
  },

  // 2. SIGNUP
  async signup(email: string, password: string, name: string): Promise<AuthResponse> {
    // FIXED: Changed API_URL to API_BASE_URL here
    const response = await fetch(`${API_BASE_URL}/auth/signup`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        username: email, 
        password: password,
        name: name,
      }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || "Signup failed");
    }

    const credentials = btoa(`${email}:${password}`);
    const token = `Basic ${credentials}`;
    
    const createdUser = await response.json();
    
    const user: User = {
        id: createdUser.id,
        email: createdUser.username,
        name: name
    };

    this.setSession(user, token);
    return { user, token };
  },

  async logout(): Promise<void> {
    localStorage.removeItem(STORAGE_KEY);
    localStorage.removeItem(TOKEN_KEY);
  },

  setSession(user: User, token: string) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
    localStorage.setItem(TOKEN_KEY, token);
  },

  getCurrentUser(): User | null {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored ? JSON.parse(stored) : null;
  },

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  },

  isAuthenticated(): boolean {
    return !!this.getToken();
  },
};