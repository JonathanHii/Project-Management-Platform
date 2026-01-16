"use client";

import { WorkItem } from "@/types/types";
import {
    X,
    FileText,
    AlertCircle,
    Flag,
    Layers,
    Clock,
    UserCircle,
} from "lucide-react";

interface ViewOnlyWorkItemModalProps {
    item: WorkItem | null;
    isOpen: boolean;
    onClose: () => void;
}

export default function ViewOnlyWorkItemModal({
    item,
    isOpen,
    onClose,
}: ViewOnlyWorkItemModalProps) {
    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString("en-US", {
            month: "short",
            day: "numeric",
            year: "numeric",
            hour: "numeric",
            minute: "2-digit",
        });
    };

    // Color helpers for badges
    const getStatusColor = (status: string) => {
        switch (status) {
            case "DONE": return "bg-green-100 text-green-700 border-green-200";
            case "IN_PROGRESS": return "bg-blue-100 text-blue-700 border-blue-200";
            case "TODO": return "bg-amber-100 text-amber-700 border-amber-200";
            default: return "bg-gray-100 text-gray-700 border-gray-200";
        }
    };

    const getPriorityColor = (priority: string) => {
        switch (priority) {
            case "URGENT": return "bg-red-100 text-red-700 border-red-200";
            case "HIGH": return "bg-orange-100 text-orange-700 border-orange-200";
            case "MEDIUM": return "bg-blue-100 text-blue-700 border-blue-200";
            default: return "bg-slate-100 text-slate-700 border-slate-200";
        }
    };

    if (!isOpen || !item) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 bg-black/60 backdrop-blur-sm transition-all duration-300">
            <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg flex flex-col max-h-[90vh] transform transition-all animate-in fade-in zoom-in-95 duration-200">

                {/* Header */}
                <div className="px-6 py-5 border-b border-gray-100 flex justify-between items-center bg-white shrink-0 z-20 rounded-t-2xl">
                    <div>
                        <div className="flex items-center gap-2">
                            <h2 className="text-xl font-semibold text-gray-900">
                                Work Item Details
                            </h2>
                            <span className="text-xs font-mono text-gray-500 bg-gray-100 border border-gray-200 px-2 py-0.5 rounded">
                                {item.id.slice(0, 8).toUpperCase()}
                            </span>
                        </div>
                        <p className="text-sm text-gray-500 mt-0.5">
                            View details for this work item.
                        </p>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-gray-600 hover:bg-gray-100 p-2 rounded-full transition-all"
                    >
                        <X className="w-5 h-5" />
                    </button>
                </div>

                {/* Scrollable Content */}
                <div className="p-6 space-y-6 overflow-y-auto custom-scrollbar flex-1 [scrollbar-gutter:stable]">

                    {/* Title */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Title
                        </label>
                        <div className="relative">
                            <FileText className="absolute left-3.5 top-3.5 w-5 h-5 text-gray-400" />
                            <div className="w-full pl-11 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-gray-900 font-medium">
                                {item.title}
                            </div>
                        </div>
                    </div>

                    {/* Description */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Description
                        </label>
                        <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-gray-900 min-h-[100px] whitespace-pre-wrap">
                            {item.description ? (
                                item.description
                            ) : (
                                <span className="text-gray-400 italic">No description provided.</span>
                            )}
                        </div>
                    </div>

                    {/* Type, Status, Priority */}
                    <div className="grid grid-cols-3 gap-4">
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                <Layers className="w-4 h-4 inline mr-1.5 text-gray-400" />
                                Type
                            </label>
                            <div className="w-full h-11 px-3 bg-gray-50 border border-gray-200 rounded-xl flex items-center text-sm font-medium text-gray-700">
                                {item.type}
                            </div>
                        </div>
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                <AlertCircle className="w-4 h-4 inline mr-1.5 text-gray-400" />
                                Status
                            </label>
                            <div className={`w-full h-11 px-3 border rounded-xl flex items-center text-sm font-bold ${getStatusColor(item.status)}`}>
                                {item.status.replace("_", " ")}
                            </div>
                        </div>
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                <Flag className="w-4 h-4 inline mr-1.5 text-gray-400" />
                                Priority
                            </label>
                            <div className={`w-full h-11 px-3 border rounded-xl flex items-center text-sm font-bold ${getPriorityColor(item.priority)}`}>
                                {item.priority}
                            </div>
                        </div>
                    </div>

                    {/* Assignee Section */}
                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">
                            Assignee
                        </label>

                        {item.assignee ? (
                            <div className="flex items-center justify-between p-3 bg-white border border-gray-200 rounded-xl">
                                <div className="flex items-center gap-3">
                                    <div className="w-9 h-9 rounded-full bg-indigo-100 border border-indigo-200 flex items-center justify-center text-indigo-700 font-bold text-sm">
                                        {item.assignee.fullName.charAt(0)}
                                    </div>
                                    <div>
                                        <p className="text-sm font-semibold text-gray-900">
                                            {item.assignee.fullName}
                                        </p>
                                        <p className="text-xs text-gray-500">
                                            {item.assignee.email}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        ) : (
                            <div className="w-full h-12 border border-dashed border-gray-200 bg-gray-50 rounded-xl flex items-center px-3 text-gray-400 select-none">
                                <span className="text-sm italic">Unassigned</span>
                            </div>
                        )}
                    </div>

                    {/* Meta Details */}
                    <div className="pt-4 border-t border-gray-100">
                        <h3 className="text-sm font-semibold text-gray-700 mb-3">Meta Information</h3>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-xl border border-gray-100">
                                <UserCircle className="w-5 h-5 text-gray-400" />
                                <div>
                                    <p className="text-xs text-gray-500">Creator</p>
                                    <p className="text-sm font-medium text-gray-900">
                                        {item.creator.fullName}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-xl border border-gray-100">
                                <Clock className="w-5 h-5 text-gray-400" />
                                <div>
                                    <p className="text-xs text-gray-500">Created</p>
                                    <p className="text-sm font-medium text-gray-900">
                                        {formatDate(item.createdAt)}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-xl border border-gray-100">
                                <Clock className="w-5 h-5 text-gray-400" />
                                <div>
                                    <p className="text-xs text-gray-500">Updated</p>
                                    <p className="text-sm font-medium text-gray-900">
                                        {formatDate(item.updatedAt)}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>

                {/* Footer */}
                <div className="px-6 py-5 bg-gray-50 border-t border-gray-100 flex justify-end gap-3 shrink-0 rounded-b-2xl z-20">
                    <button
                        onClick={onClose}
                        className="px-5 py-2.5 text-gray-700 bg-white border border-gray-200 hover:bg-gray-50 hover:border-gray-300 rounded-xl font-medium transition-all shadow-sm"
                    >
                        Close
                    </button>
                </div>
            </div>
        </div>
    );
}