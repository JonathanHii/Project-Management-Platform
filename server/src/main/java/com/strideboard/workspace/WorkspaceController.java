package com.strideboard.workspace;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.strideboard.data.user.User;
import com.strideboard.data.user.UserRepository;
import com.strideboard.data.workspace.Membership;
import com.strideboard.data.workspace.MembershipRepository;
import com.strideboard.data.workspace.Workspace;
import com.strideboard.data.workspace.WorkspaceRepository;
import com.strideboard.project.Project;
import com.strideboard.project.ProjectRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {
        private final WorkspaceRepository workspaceRepository;
        private final MembershipRepository membershipRepository;
        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;

        @GetMapping
        @Transactional(readOnly = true)
        public ResponseEntity<List<Workspace>> getMyWorkspaces(Authentication auth) {
                // Find the user by email (principal name)
                User user = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Get workspaces through memberships
                List<Workspace> workspaces = user.getMemberships().stream()
                                .map(Membership::getWorkspace) // converet workspaces
                                .collect(Collectors.toList());

                return ResponseEntity.ok(workspaces);
        }

        @PostMapping
        @Transactional
        public ResponseEntity<Workspace> createWorkspace(@RequestBody Workspace workspace, Authentication auth) {
                // Generate a slug (simple version: "My Team" -> "my-team")
                workspace.setSlug(workspace.getName().toLowerCase().replaceAll(" ", "-"));

                // Save Workspace
                Workspace savedWorkspace = workspaceRepository.save(workspace);

                // Find Current User
                User currentUser = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Create and Save Membership (Owner/Admin)
                Membership membership = Membership.builder()
                                .user(currentUser)
                                .workspace(savedWorkspace)
                                .role("ADMIN")
                                .build();

                membershipRepository.save(membership);

                return ResponseEntity.ok(savedWorkspace);
        }

        @GetMapping("/{workspaceId}/projects")
        @Transactional(readOnly = true)
        public ResponseEntity<List<Project>> getWorkspaceProjects(
                        @PathVariable UUID workspaceId,
                        Authentication auth) {

                // 1. Find the user
                User user = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // 2. Security Check: Verify user is a member of this workspace
                boolean isMember = membershipRepository.existsByUserIdAndWorkspaceId(user.getId(), workspaceId);

                if (!isMember) {
                        return ResponseEntity.status(403).build(); // Forbidden if not a member
                }

                // 3. Fetch projects
                List<Project> projects = projectRepository.findByWorkspace_Id(workspaceId);

                return ResponseEntity.ok(projects);
        }

        @GetMapping("/{workspaceId}")
        public ResponseEntity<Workspace> getWorkspaceById(@PathVariable UUID workspaceId, Authentication auth) {
                User user = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Ensure the user is actually a member of this specific workspace
                if (!membershipRepository.existsByUserIdAndWorkspaceId(user.getId(), workspaceId)) {
                        return ResponseEntity.status(403).build();
                }

                return workspaceRepository.findById(workspaceId)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @GetMapping("/{workspaceId}/projects/{projectId}")
        @Transactional(readOnly = true)
        public ResponseEntity<Project> getProjectById(
                        @PathVariable UUID workspaceId,
                        @PathVariable UUID projectId,
                        Authentication auth) {

                User user = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (!membershipRepository.existsByUserIdAndWorkspaceId(user.getId(), workspaceId)) {
                        return ResponseEntity.status(403).build();
                }

                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new RuntimeException("Project not found"));

                if (!project.getWorkspace().getId().equals(workspaceId)) {
                        return ResponseEntity.status(400).build(); // Project is not in this workspace
                }

                return ResponseEntity.ok(project);
        }

}
