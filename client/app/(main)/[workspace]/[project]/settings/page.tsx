"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { workspaceService } from "@/services/workspace-service";
import { Project } from "@/types/types";
import {
    Save,
    Trash2,
    AlertTriangle,
} from "lucide-react";

export default function SettingsPage() {
    const params = useParams();
    const [project, setProject] = useState<Project | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Form state
    const [projectName, setProjectName] = useState("");
    const [projectDescription, setProjectDescription] = useState("");

    const workspaceId = params.workspace as string;
    const projectId = params.project as string;

    useEffect(() => {
        const fetchProjectData = async () => {
            try {
                if (workspaceId && projectId) {
                    const data = await workspaceService.getProjectById(workspaceId, projectId);
                    setProject(data);

                    setProjectName(data.name || "");
                    setProjectDescription(data.description || "");
                }
            } catch (err: any) {
                setError(err.message || "Failed to load project settings");
            } finally {
                setIsLoading(false);
            }
        };
        fetchProjectData();
    }, [workspaceId, projectId]);

    const handleSaveGeneral = () => {
        console.log("Save general settings", { projectName, projectDescription });
    };

    const handleDeleteProject = () => {
        console.log("Delete project");
    };

    if (isLoading) return <div className="p-10 animate-pulse text-slate-400">Loading settings...</div>;
    if (error) return <div className="p-10 text-red-500 font-medium">Error: {error}</div>;

    return (
        <div className="h-full w-full overflow-y-auto pr-4">
            <div className="pb-12 space-y-8">

                {/* --- General Settings --- */}
                <section className="bg-white border border-slate-200 rounded-xl overflow-hidden">
                    <div className="px-6 py-4 border-b border-slate-100 bg-slate-50">
                        <h2 className="text-sm font-bold text-slate-700 uppercase tracking-wide">
                            General Settings
                        </h2>
                    </div>
                    <div className="p-6 space-y-5">
                        <div>
                            <label htmlFor="projectName" className="block text-sm font-medium text-slate-700 mb-1.5">
                                Project Name
                            </label>
                            <input
                                id="projectName"
                                type="text"
                                value={projectName}
                                onChange={(e) => setProjectName(e.target.value)}
                                placeholder="Enter project name"
                                className="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
                            />
                        </div>

                        <div>
                            <label htmlFor="projectDescription" className="block text-sm font-medium text-slate-700 mb-1.5">
                                Description
                            </label>
                            <textarea
                                id="projectDescription"
                                value={projectDescription}
                                onChange={(e) => setProjectDescription(e.target.value)}
                                placeholder="Describe your project..."
                                rows={3}
                                className="w-full px-4 py-2.5 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all resize-none"
                            />
                        </div>

                        <div className="pt-2">
                            <button
                                onClick={handleSaveGeneral}
                                className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition-all shadow-sm"
                            >
                                <Save className="h-4 w-4" />
                                Save Changes
                            </button>
                        </div>
                    </div>
                </section>


                {/* --- Danger Zone --- */}
                <section className="bg-white border border-red-200 rounded-xl overflow-hidden">
                    <div className="px-6 py-4 border-b border-red-100 bg-red-50">
                        <div className="flex items-center gap-2">
                            <AlertTriangle className="h-4 w-4 text-red-500" />
                            <h2 className="text-sm font-bold text-red-700 uppercase tracking-wide">
                                Danger Zone
                            </h2>
                        </div>
                    </div>
                    <div className="p-6 space-y-4">

                        {/* Delete Project */}
                        <div className="flex items-center justify-between p-4 border border-red-200 rounded-lg bg-red-50/30">
                            <div>
                                <h3 className="text-sm font-semibold text-slate-700">Delete Project</h3>
                                <p className="text-xs text-slate-500 mt-0.5">
                                    Permanently delete this project and all its data. This cannot be undone.
                                </p>
                            </div>
                            <button
                                onClick={handleDeleteProject}
                                className="flex items-center gap-2 bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-all"
                            >
                                <Trash2 className="h-4 w-4" />
                                Delete
                            </button>
                        </div>
                    </div>
                </section>

            </div>
        </div>
    );
}