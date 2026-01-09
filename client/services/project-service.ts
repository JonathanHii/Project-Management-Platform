import { authService } from "./authService";
import { WorkItem } from "@/types/types";

const API_BASE_URL = "http://localhost:8080/api/projects";

export const projectService = {
    /**
     * Fetches all work items for a specific project.
     * Note: workspaceId is required by the backend for security/membership validation.
     */
    async getProjectWorkItems(workspaceId: string, projectId: string): Promise<WorkItem[]> {
        const token = authService.getToken();

        // The URL structure assumes the fix provided in the section below
        const response = await fetch(`${API_BASE_URL}/${workspaceId}/${projectId}/work-items`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || "Failed to fetch work items");
        }

        return data;
    },
};