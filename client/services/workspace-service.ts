import { Workspace, CreateWorkspaceRequest, Project } from "@/types/types";
import { authService } from "./authService";

const API_BASE_URL = "http://localhost:8080/api/workspaces";

export const workspaceService = {
  async getMyWorkspaces(): Promise<Workspace[]> {
    const token = authService.getToken(); // Retrieve the token

    const response = await fetch(API_BASE_URL, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || "Failed to fetch workspaces");
    }

    return data;
  },

  async createWorkspace(data: CreateWorkspaceRequest): Promise<Workspace> {
    const token = authService.getToken();

    const response = await fetch(API_BASE_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify(data),
    });

    const result = await response.json();

    if (!response.ok) {
      throw new Error(result.message || "Failed to create workspace");
    }

    return result;
  },

  async getWorkspaceProjects(workspaceId: string): Promise<Project[]> {
    const token = authService.getToken();

    const response = await fetch(`${API_BASE_URL}/${workspaceId}/projects`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || "Failed to fetch projects for this workspace");
    }

    return data;
  },

  async getWorkspaceById(workspaceId: string): Promise<Workspace> {
    const token = authService.getToken();
    const response = await fetch(`${API_BASE_URL}/${workspaceId}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
    });

    if (!response.ok) {
      const data = await response.json();
      throw new Error(data.message || "Failed to fetch workspace");
    }

    return response.json();
  },

  async getProjectById(workspaceId: string, projectId: string): Promise<Project> {
    const token = authService.getToken();
    const response = await fetch(`${API_BASE_URL}/${workspaceId}/projects/${projectId}`, {
      headers: { "Authorization": `Bearer ${token}` },
    });

    if (!response.ok) throw new Error("Failed to fetch project");
    return response.json();
  }



};