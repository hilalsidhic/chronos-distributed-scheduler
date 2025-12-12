import { API_BASE_URL } from "@/config"; // Import the constant
// Define interfaces
export interface User {
  id: string; // or number, depending on your DB
  email: string; // We will map 'username' to email in the frontend context
  name: string;
}

export interface AuthResponse {
  user: User;
  token: string;
}

const STORAGE_KEY = "auth_user";
const TOKEN_KEY = "auth_token"; // We store the "Basic ..." string here

export const authService = {
  // 1. LOGIN
  // Since we use Basic Auth, we "login" by trying to fetch a protected resource.
  async login(email: string, password: string): Promise<AuthResponse> {
    const credentials = btoa(`${email}:${password}`);
    const token = `Basic ${credentials}`;

    // Test the credentials by hitting a protected endpoint (e.g., /scheduler/jobs)
    // We use limit=1 to keep the payload light
    const response = await fetch(`${API_URL}/scheduler/jobs?limit=1`, {
      method: "GET",
      headers: {
        Authorization: token,
      },
    });

    if (!response.ok) {
      if (response.status === 401) throw new Error("Invalid credentials");
      throw new Error("Login failed");
    }

    // If successful, construct the user object
    // (In Basic Auth, the server doesn't send the user back on GET, so we use the input)
    const user: User = {
      id: "0", // Placeholder since we don't have the ID yet
      email: email,
      name: email.split("@")[0], // Fallback name
    };

    this.setSession(user, token);
    return { user, token };
  },

  // 2. SIGNUP
  async signup(email: string, password: string, name: string): Promise<AuthResponse> {
    const response = await fetch(`${API_URL}/auth/signup`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        username: email, // Map email to backend 'username'
        password: password,
        name: name,
      }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || "Signup failed");
    }

    // If signup is successful, we automatically log them in
    const credentials = btoa(`${email}:${password}`);
    const token = `Basic ${credentials}`;
    
    // The backend returns the created user
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

  // Helper to save session
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