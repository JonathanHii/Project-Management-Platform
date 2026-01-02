import { authService } from "./authService";

export const workService = {
  async getHomeMessage() {
    const token = authService.getToken();
    
    const response = await fetch("http://localhost:8080/", {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${token}`,
      },
    });

    if (response.status === 401) {
      authService.logout();
      window.location.href = "/login";
      return;
    }

    const data = await response.json();
    return data.message; // Extract the string from the JSON object
  }
};