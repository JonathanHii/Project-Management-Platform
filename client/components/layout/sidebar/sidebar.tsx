"use client"
import { useState } from "react";
import Logo from "./logo";
import MyWork from "./my-work";
import WorkspaceList from "./workspace-list";

export default function Sidebar() {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      {/* --- Mobile Header (Slim Version) --- */}
      <div className="md:hidden flex items-center p-2 bg-[#F9FAFB] border-b border-gray-200">
        <button
          onClick={() => setIsOpen(true)}
          className="p-1.5 text-gray-600 rounded-md hover:bg-gray-200 focus:outline-none"
        >
          {/* UPDATED: Sidebar/Panel Icon (Looks like a side menu) */}
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2" strokeWidth={2} />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 3v18" />
          </svg>
        </button>
      </div>

      {/* --- Overlay (Backdrop for mobile) --- */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-gray-600 bg-opacity-75 md:hidden"
          onClick={() => setIsOpen(false)}
        ></div>
      )}

      {/* --- Sidebar (Responsive) --- */}
      <aside
        className={`
          fixed inset-y-0 left-0 z-50 w-64 transform bg-[#F9FAFB] border-r border-gray-200 transition-transform duration-300 ease-in-out flex flex-col
          ${isOpen ? "translate-x-0" : "-translate-x-full"}
          md:static md:translate-x-0 md:h-auto
        `}
      >
        {/* Close Button (Mobile Only) */}
        <div className="absolute top-2 right-2 md:hidden">
          <button
            onClick={() => setIsOpen(false)}
            className="p-1 text-gray-500 hover:text-gray-700"
          >
            {/* X Icon */}
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Original Content */}
        <div className="p-4">
          <Logo />
        </div>
        <nav className="flex-1 px-2 py-4 space-y-8 overflow-y-auto">
          <MyWork />
          <WorkspaceList />
        </nav>
      </aside>
    </>
  );
}