export interface Workspace {
  id: string;
  name: string;
  slug: string;
  memberCount: number;  
  projectCount: number;
}

export interface Membership {
  id: string;
  role: 'ADMIN' | 'MEMBER' | 'VIEWER';
  user?: any; // expand this if  need User details
  workspace?: Workspace;
}

export interface CreateWorkspaceRequest {
  name: string;
}