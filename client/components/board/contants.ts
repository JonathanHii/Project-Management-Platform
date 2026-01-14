import {
  WorkItemStatus,
  WorkItemPriority,
  WorkItemType,
} from "@/types/types";

export const COLUMNS: WorkItemStatus[] = ["BACKLOG", "TODO", "IN_PROGRESS", "DONE"];
export const STATUSES: WorkItemStatus[] = ["BACKLOG", "TODO", "IN_PROGRESS", "DONE"];
export const PRIORITIES: WorkItemPriority[] = ["LOW", "MEDIUM", "HIGH", "URGENT"];
export const TYPES: WorkItemType[] = ["TASK", "BUG", "EPIC"];