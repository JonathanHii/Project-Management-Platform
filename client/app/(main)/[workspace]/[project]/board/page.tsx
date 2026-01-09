"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { projectService } from "@/services/project-service";
import { WorkItem } from "@/types/types";

export default function BoardPage() {
    const params = useParams();
    const [data, setData] = useState<WorkItem[] | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const testApi = async () => {
            try {
                // IMPORTANT: Matches [workspace] and [project] folder names
                const workspaceId = params.workspace as string;
                const projectId = params.project as string;

                if (workspaceId && projectId) {
                    const items = await projectService.getProjectWorkItems(workspaceId, projectId);
                    setData(items);
                } else {
                    setError(`Missing params. Found workspace: ${workspaceId}, project: ${projectId}`);
                }
            } catch (err: any) {
                setError(err.message);
            }
        };

        testApi();
    }, [params]);

    return (
        <div>
            {/* API TEST SECTION */}
            <div>
                {error && <div>Error: {error}</div>}
                {data ? (
                    <pre>
                        {JSON.stringify(data, null, 2)}
                    </pre>
                ) : (
                    <p>{!error && "Fetching data from API..."}</p>
                )}
            </div>
        </div>
    );
}