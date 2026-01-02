const API_URL = "http://localhost:8080/api/auth";

export const authService = {
  // Login (The "POST /token" with Basic Auth)
  async login(email: string, password: string) {
    const credentials = btoa(`${email}:${password}`);
    
    const response = await fetch(`${API_URL}/token`, {
      method: 'POST',
      headers: {
        'Authorization': `Basic ${credentials}`,
      },
    });

    if (!response.ok) throw new Error("Login failed");

    const token = await response.text();
    
    // Save to localStorage so we can "Reuse" it later
    localStorage.setItem("stride_token", token);
    return token;
  },

  // Get the Token from storage
  getToken() {
    return localStorage.getItem("stride_token");
  },

  // 3. Logout
  logout() {
    localStorage.removeItem("stride_token");
  }
};