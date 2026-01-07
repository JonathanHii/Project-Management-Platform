"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, usePathname } from "next/navigation"; // 1. Import usePathname
import { ChevronRight } from "lucide-react";
import { workspaceService } from "@/services/workspace-service";
import { Workspace } from "@/types/types";

export default function Breadcrumbs() {
  const pathname = usePathname(); 
  const params = useParams();
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);

  const workspaceId = params?.workspace as string;
  const projectId = params?.project as string;

  useEffect(() => {
    if (pathname === "/dashboard") return;

    const loadData = async () => {
      try {
        const data = await workspaceService.getMyWorkspaces();
        setWorkspaces(data);
      } catch (error) {
        console.error("Failed to load breadcrumb data:", error);
      }
    };
    loadData();
  }, [pathname]); 

  // Hide component completely on /dashboard
  if (pathname === "/dashboard") {
    return null;
  }

  const currentWorkspace = workspaces.find((w) => w.id === workspaceId);
  const currentProject = currentWorkspace?.projects?.find((p) => p.id === projectId);

  return (
    <nav className="flex items-center text-sm font-medium">
      <Link
        href="/dashboard"
        className="text-gray-500 hover:text-gray-900 transition-colors"
      >
        Dashboard
      </Link>

      {workspaceId && (
        <>
          <ChevronRight className="w-4 h-4 mx-2 text-gray-400" />
          <Link
            href={`/${workspaceId}`}
            className={`transition-colors hover:text-blue-600 ${!projectId ? "text-gray-900 font-semibold" : "text-gray-500"
              }`}
          >
            {currentWorkspace ? currentWorkspace.name : "Loading..."}
          </Link>
        </>
      )}

      {projectId && (
        <>
          <ChevronRight className="w-4 h-4 mx-2 text-gray-400" />
          <span className="text-gray-900 font-semibold">
            {currentProject ? currentProject.name : "Loading..."}
          </span>
        </>
      )}
    </nav>
  );
}