"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { FolderOpen, Plus, Search, Loader2 } from "lucide-react";
import { Workspace, Project } from "@/types/types";
import { workspaceService } from "@/services/workspace-service";
import ProjectCard from "@/components/project/project-card";

export default function WorkspaceProjectsPage() {
    const params = useParams();
    
    const workspaceId = params.workspace as string; 

    const [workspace, setWorkspace] = useState<Workspace | null>(null);
    const [projects, setProjects] = useState<Project[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState("");

    useEffect(() => {
        const loadData = async () => {
            if (!workspaceId) return;

            try {
                setLoading(true);
                
                // Fetch projects directly using the ID from URL
                const projectData = await workspaceService.getWorkspaceProjects(workspaceId);
                setProjects(projectData);

                // Fetch workspaces to find the name for the UI header
                const workspaceList = await workspaceService.getMyWorkspaces();
                const currentWs = workspaceList.find(ws => ws.id === workspaceId);
                
                if (currentWs) {
                    setWorkspace(currentWs);
                }
            } catch (err) {
                console.error("Error loading workspace data:", err);
            } finally {
                setLoading(false); 
            }
        };

        loadData();
    }, [workspaceId]);

    const filteredProjects = projects.filter(p => 
        p.name.toLowerCase().includes(searchQuery.toLowerCase())
    );

    if (loading) {
        return (
            <div className="flex flex-col items-center justify-center py-24 text-gray-500">
                <Loader2 className="w-10 h-10 animate-spin text-indigo-600 mb-4" />
                <p className="animate-pulse">Retrieving your projects...</p>
            </div>
        );
    }

    return (
        <div className="px-8 pb-8 max-w-7xl mx-auto">
            <div className="flex items-center justify-between mb-8">
                <h1 className="text-3xl font-bold text-gray-900">
                    {workspace?.name || "Workspace"}
                </h1>
                <button className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 transition-colors font-medium shadow-sm">
                    <Plus className="w-5 h-5" />
                    New Project
                </button>
            </div>

            <div className="relative mb-8 max-w-sm">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                    type="text"
                    placeholder="Filter projects..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-10 pr-4 py-2 bg-white border border-gray-200 rounded-lg focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 outline-none transition-all"
                />
            </div>

            {workspace ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                    {filteredProjects.map((project) => (
                        <ProjectCard
                            key={project.id}
                            project={project}
                            workspaceId={workspaceId} // Pass the ID from params
                        />
                    ))}

                    {filteredProjects.length === 0 && (
                        <div className="col-span-full py-20 border-2 border-dashed border-gray-100 rounded-2xl flex flex-col items-center">
                            <FolderOpen className="w-12 h-12 text-gray-200 mb-4" />
                            <p className="text-gray-500">No projects found.</p>
                        </div>
                    )}
                </div>
            ) : (
                <div className="text-center py-20 bg-gray-50 rounded-xl text-gray-500">
                    Workspace not found.
                </div>
            )}
        </div>
    );
}