import Cookies from 'js-cookie';

const API_URL = `${process.env.NEXT_PUBLIC_API_URL}/auth`;

export const authService = {
  async login(email: string, password: string) {
    const credentials = btoa(`${email}:${password}`);

    const response = await fetch(`${API_URL}/login`, {
      method: 'POST',
      headers: { 'Authorization': `Basic ${credentials}` },
    });

    if (!response.ok) throw new Error("Login failed");

    const token = await response.text();

    Cookies.set('stride_token', token, { expires: 1, secure: true, sameSite: 'strict' });

    return token;
  },

  async register(email: string, password: string, fullName: string) {
    const response = await fetch(`${API_URL}/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, fullName }),
    });

    if (!response.ok) throw new Error("Registration failed");

    const token = await response.text();

    Cookies.set('stride_token', token, { expires: 1, secure: true, sameSite: 'strict' });

    return token;
  },

  getToken() {
    return Cookies.get('stride_token');
  },

  logout() {
    Cookies.remove('stride_token');
  }
};