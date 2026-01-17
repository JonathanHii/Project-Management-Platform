package com.strideboard.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.strideboard.data.notification.Notification;
import com.strideboard.data.notification.NotificationRepository;
import com.strideboard.data.notification.NotificationType;
import com.strideboard.data.project.CreateProjectRequest;
import com.strideboard.data.project.Project;
import com.strideboard.data.project.ProjectRepository;
import com.strideboard.data.user.User;
import com.strideboard.data.user.UserRepository;
import com.strideboard.data.workspace.AddMembersRequest;
import com.strideboard.data.workspace.CreateWorkspaceRequest;
import com.strideboard.data.workspace.Membership;
import com.strideboard.data.workspace.MembershipRepository;
import com.strideboard.data.workspace.Workspace;
import com.strideboard.data.workspace.WorkspaceRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {
        private final WorkspaceRepository workspaceRepository;
        private final MembershipRepository membershipRepository;
        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;
        private final NotificationRepository notificationRepository;

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

        @GetMapping("/users/search")
        @Transactional(readOnly = true)
        public ResponseEntity<List<Map<String, String>>> searchUsers(
                        @RequestParam String query,
                        Authentication auth) {
                // Validation
                if (query == null || query.trim().length() < 2) {
                        return ResponseEntity.ok(Collections.emptyList());
                }

                // Get current user's email to exclude them from results
                String currentUserEmail = auth.getName();

                // Search Database
                List<User> users = userRepository.findByEmailContainingIgnoreCase(query);

                List<Map<String, String>> result = new ArrayList<>();

                for (User u : users) {
                        // Skip the current user
                        if (u.getEmail().equalsIgnoreCase(currentUserEmail)) {
                                continue;
                        }

                        Map<String, String> map = new HashMap<>();
                        map.put("id", u.getId().toString());
                        map.put("email", u.getEmail());
                        // Use getFullName() because your Entity uses 'fullName', not 'name'
                        map.put("name", u.getFullName() != null ? u.getFullName() : "");

                        result.add(map);
                }

                // 5. Limit to top 10 results manually
                if (result.size() > 10) {
                        return ResponseEntity.ok(result.subList(0, 10));
                }

                return ResponseEntity.ok(result);
        }

        @GetMapping("/{workspaceId}/users/search")
        @Transactional(readOnly = true)
        public ResponseEntity<List<Map<String, String>>> searchUsersNotInWorkspace(
                        @PathVariable UUID workspaceId,
                        @RequestParam String query,
                        Authentication auth) {
                // Validation
                if (query == null || query.trim().length() < 2) {
                        return ResponseEntity.ok(Collections.emptyList());
                }

                // Verify current user is a member of this workspace (security check)
                User currentUser = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                if (!membershipRepository.existsByUserIdAndWorkspaceId(currentUser.getId(), workspaceId)) {
                        return ResponseEntity.status(403).build();
                }

                // Get all current workspace member emails
                List<Membership> memberships = membershipRepository.findByWorkspaceId(workspaceId);
                List<String> memberEmails = memberships.stream()
                                .map(m -> m.getUser().getEmail().toLowerCase())
                                .collect(Collectors.toList());

                // Search Database
                List<User> users = userRepository.findByEmailContainingIgnoreCase(query);

                // Manual Mapping - exclude all workspace members
                List<Map<String, String>> result = new ArrayList<>();

                for (User u : users) {
                        // Skip users who are already members of this workspace
                        if (memberEmails.contains(u.getEmail().toLowerCase())) {
                                continue;
                        }

                        Map<String, String> map = new HashMap<>();
                        map.put("id", u.getId().toString());
                        map.put("email", u.getEmail());
                        map.put("name", u.getFullName() != null ? u.getFullName() : "");

                        result.add(map);
                }

                // Limit to top 10 results manually
                if (result.size() > 10) {
                        return ResponseEntity.ok(result.subList(0, 10));
                }

                return ResponseEntity.ok(result);
        }

        @PostMapping
        @Transactional
        public ResponseEntity<Workspace> createWorkspace(@RequestBody CreateWorkspaceRequest request,
                        Authentication auth) {

                User currentUser = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Workspace workspace = new Workspace();
                workspace.setName(request.getName());

                if (request.getSlug() != null && !request.getSlug().isEmpty()) {
                        workspace.setSlug(request.getSlug());
                } else {
                        workspace.setSlug(request.getName().toLowerCase().replaceAll(" ", "-"));
                }

                workspace.setOwner(currentUser);

                Workspace savedWorkspace = workspaceRepository.save(workspace);

                Membership ownerMembership = Membership.builder()
                                .user(currentUser)
                                .workspace(savedWorkspace)
                                .role("ADMIN")
                                .build();

                membershipRepository.save(ownerMembership);

                if (request.getMemberEmails() != null && !request.getMemberEmails().isEmpty()) {
                        for (String email : request.getMemberEmails()) {
                                if (email.equalsIgnoreCase(currentUser.getEmail())) {
                                        continue;
                                }

                                String trimmedEmail = email.trim().toLowerCase();

                                userRepository.findByEmail(trimmedEmail).ifPresent(userToInvite -> {

                                        boolean alreadyMember = membershipRepository.existsByUserIdAndWorkspaceId(
                                                        userToInvite.getId(), savedWorkspace.getId());

                                        if (!alreadyMember) {
                                                boolean inviteExists = notificationRepository
                                                                .existsByRecipientIdAndWorkspaceIdAndType(
                                                                                userToInvite.getId(),
                                                                                savedWorkspace.getId(),
                                                                                NotificationType.INVITE);

                                                if (!inviteExists) {
                                                        Notification invite = Notification.builder()
                                                                        .recipient(userToInvite)
                                                                        .type(NotificationType.INVITE)
                                                                        .workspace(savedWorkspace)
                                                                                                 
                                                                        .title("Workspace Invitation")
                                                                        .subtitle("You have been invited to join "
                                                                                        + savedWorkspace.getName())
                                                                        .build();

                                                        notificationRepository.save(invite);
                                                }
                                        }
                                });
                        }
                }

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

        @PostMapping("/{workspaceId}/projects")
        @Transactional
        public ResponseEntity<Project> createProject(
                        @PathVariable UUID workspaceId,
                        @RequestBody CreateProjectRequest request,
                        Authentication auth) {

                // Find the user (for security context)
                User user = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Security Check: Verify user is a member of this workspace and NOT a viewer
                Membership membership = membershipRepository.findByUserIdAndWorkspaceId(user.getId(), workspaceId)
                                .orElse(null);

                if (membership == null || "VIEWER".equalsIgnoreCase(membership.getRole())) {
                        return ResponseEntity.status(403).build(); // Forbidden
                }

                // Fetch the Workspace entity
                Workspace workspace = workspaceRepository.findById(workspaceId)
                                .orElseThrow(() -> new RuntimeException("Workspace not found"));

                // Build the Project using your @Builder
                Project project = Project.builder()
                                .name(request.getName())
                                .description(request.getDescription())
                                .workspace(workspace) // @ManyToOne relationship
                                .creator(user)
                                .build();

                // Save and Return
                Project savedProject = projectRepository.save(project);

                return ResponseEntity.ok(savedProject);
        }

        @GetMapping("/{workspaceId}/me")
        @Transactional(readOnly = true)
        public ResponseEntity<Map<String, String>> getCurrentUserInWorkspace(
                        @PathVariable UUID workspaceId,
                        Authentication auth) {

                User user = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Membership membership = membershipRepository.findByUserIdAndWorkspaceId(user.getId(), workspaceId)
                                .orElse(null);

                if (membership == null) {
                        return ResponseEntity.status(403).build();
                }

                Map<String, String> result = new HashMap<>();
                result.put("id", user.getId().toString());
                result.put("email", user.getEmail());
                result.put("name", user.getFullName() != null ? user.getFullName() : "");
                result.put("role", membership.getRole().substring(0, 1).toUpperCase()
                                + membership.getRole().substring(1).toLowerCase());

                return ResponseEntity.ok(result);
        }

        @GetMapping("/{workspaceId}/members")
        @Transactional(readOnly = true)
        public ResponseEntity<List<Map<String, String>>> getWorkspaceMembers(
                        @PathVariable UUID workspaceId,
                        Authentication auth) {

                User currentUser = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Security Check: Verify user is a member of this workspace
                if (!membershipRepository.existsByUserIdAndWorkspaceId(currentUser.getId(), workspaceId)) {
                        return ResponseEntity.status(403).build();
                }

                List<Membership> memberships = membershipRepository.findByWorkspaceId(workspaceId);

                List<Map<String, String>> result = new ArrayList<>();
                for (Membership m : memberships) {
                        User u = m.getUser();
                        Map<String, String> map = new HashMap<>();
                        map.put("id", u.getId().toString());
                        map.put("email", u.getEmail());
                        map.put("name", u.getFullName() != null ? u.getFullName() : "");
                        map.put("role", m.getRole().substring(0, 1).toUpperCase()
                                        + m.getRole().substring(1).toLowerCase());
                        result.add(map);
                }

                return ResponseEntity.ok(result);
        }

        @DeleteMapping("/{workspaceId}")
        @Transactional
        public ResponseEntity<Void> deleteWorkspace(
                        @PathVariable UUID workspaceId,
                        Authentication auth) {

                // Find the current user
                User user = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Find the user's membership in this workspace
                Membership membership = membershipRepository.findByUserIdAndWorkspaceId(user.getId(), workspaceId)
                                .orElse(null);

                // Security Check: User must be a member
                if (membership == null) {
                        return ResponseEntity.status(403).build(); // Forbidden - not a member
                }

                // Security Check: Only ADMIN can delete a workspace
                if (!"ADMIN".equalsIgnoreCase(membership.getRole())) {
                        return ResponseEntity.status(403).build(); // Forbidden - not an admin
                }

                // Find the workspace
                Workspace workspace = workspaceRepository.findById(workspaceId)
                                .orElse(null);

                if (workspace == null) {
                        return ResponseEntity.notFound().build();
                }

                // Delete the workspace (cascades to memberships and projects due to
                // CascadeType.ALL)
                workspaceRepository.delete(workspace);

                return ResponseEntity.noContent().build(); // 204 No Content
        }

        @PostMapping("/{workspaceId}/rename")
        @Transactional
        public ResponseEntity<?> updateWorkspaceName(
                        @PathVariable UUID workspaceId,
                        @RequestBody Map<String, String> request,
                        Authentication auth) {

                // Find the current user
                User user = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Find the user's membership in this workspace
                Membership membership = membershipRepository.findByUserIdAndWorkspaceId(user.getId(), workspaceId)
                                .orElse(null);

                // Security Check: User must be a member
                if (membership == null) {
                        return ResponseEntity.status(403)
                                        .body(Map.of("message", "You are not a member of this workspace"));
                }

                // Security Check: Only ADMIN can update workspace name
                if (!"ADMIN".equalsIgnoreCase(membership.getRole())) {
                        return ResponseEntity.status(403)
                                        .body(Map.of("message", "Only admins can update the workspace name"));
                }

                // Validate the new name
                String newName = request.get("name");
                if (newName == null || newName.trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("message", "Workspace name cannot be empty"));
                }

                // Find and update the workspace
                Workspace workspace = workspaceRepository.findById(workspaceId)
                                .orElse(null);

                if (workspace == null) {
                        return ResponseEntity.notFound().build();
                }

                workspace.setName(newName.trim());
                Workspace updatedWorkspace = workspaceRepository.save(workspace);

                return ResponseEntity.ok(updatedWorkspace);
        }

        @PostMapping("/{workspaceId}/members")
        @Transactional
        public ResponseEntity<?> addMembersToWorkspace(
                        @PathVariable UUID workspaceId,
                        @RequestBody AddMembersRequest request,
                        Authentication auth) {

                User currentUser = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Membership currentMembership = membershipRepository
                                .findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId)
                                .orElse(null);

                if (currentMembership == null) {
                        return ResponseEntity.status(403)
                                        .body(Map.of("message", "You are not a member of this workspace"));
                }

                if (!"ADMIN".equalsIgnoreCase(currentMembership.getRole())) {
                        return ResponseEntity.status(403).body(Map.of("message", "Only admins can invite members"));
                }

                Workspace workspace = workspaceRepository.findById(workspaceId)
                                .orElseThrow(() -> new RuntimeException("Workspace not found"));

                int invitesSent = 0;

                // Process each email
                for (String email : request.getEmails()) {
                        if (email == null || email.trim().isEmpty()) {
                                continue;
                        }

                        String trimmedEmail = email.trim().toLowerCase();

                        User userToInvite = userRepository.findByEmail(trimmedEmail).orElse(null);
                        if (userToInvite == null) {
                                continue;
                        }

                        if (membershipRepository.existsByUserIdAndWorkspaceId(userToInvite.getId(), workspaceId)) {
                                continue;
                        }

                        boolean inviteExists = notificationRepository.existsByRecipientIdAndWorkspaceIdAndType(
                                        userToInvite.getId(),
                                        workspaceId,
                                        NotificationType.INVITE);

                        if (inviteExists) {
                                continue;
                        }

                        Notification invite = Notification.builder()
                                        .recipient(userToInvite)
                                        .type(NotificationType.INVITE)
                                        .workspace(workspace)
                                        .title("Workspace Invitation")
                                        .subtitle("You have been invited to join " + workspace.getName())
                                        .build();

                        notificationRepository.save(invite);
                        invitesSent++;
                }

                if (invitesSent == 0) {
                        return ResponseEntity.ok(Map.of("message",
                                        "No new invitations sent (users may already be members or invited)"));
                }

                return ResponseEntity.ok(Map.of("message", invitesSent + " invitation(s) sent successfully"));
        }

        @DeleteMapping("/{workspaceId}/members/{memberId}")
        @Transactional
        public ResponseEntity<?> removeMemberFromWorkspace(
                        @PathVariable UUID workspaceId,
                        @PathVariable UUID memberId,
                        Authentication auth) {

                // Find the current user
                User currentUser = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Security Check: Verify current user is an ADMIN of this workspace
                Membership currentMembership = membershipRepository
                                .findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId)
                                .orElse(null);

                if (currentMembership == null || !"ADMIN".equalsIgnoreCase(currentMembership.getRole())) {
                        return ResponseEntity.status(403)
                                        .body(Map.of("message", "Only admins can remove members"));
                }

                // Prevent admin from removing themselves
                if (currentUser.getId().equals(memberId)) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("message", "You cannot remove yourself from the workspace"));
                }

                // Find the membership to remove
                Membership membershipToRemove = membershipRepository
                                .findByUserIdAndWorkspaceId(memberId, workspaceId)
                                .orElse(null);

                if (membershipToRemove == null) {
                        return ResponseEntity.notFound().build();
                }

                // Remove the membership
                membershipRepository.delete(membershipToRemove);

                return ResponseEntity.noContent().build();
        }

        @PutMapping("/{workspaceId}/members/{memberId}/role")
        @Transactional
        public ResponseEntity<?> changeMemberRole(
                        @PathVariable UUID workspaceId,
                        @PathVariable UUID memberId,
                        @RequestBody Map<String, String> request,
                        Authentication auth) {

                // Find the current user (requester)
                User currentUser = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Check Self-Modification
                if (currentUser.getId().equals(memberId)) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("message", "You cannot change your own role"));
                }

                // Verify requester is an ADMIN of this workspace
                Membership currentMembership = membershipRepository
                                .findByUserIdAndWorkspaceId(currentUser.getId(), workspaceId)
                                .orElse(null);

                if (currentMembership == null || !"ADMIN".equalsIgnoreCase(currentMembership.getRole())) {
                        return ResponseEntity.status(403)
                                        .body(Map.of("message", "Only admins can change member roles"));
                }

                // Validate the new role
                String newRole = request.get("role");
                if (newRole == null || newRole.trim().isEmpty()) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("message", "Role is required"));
                }

                String formattedRole = newRole.trim().toUpperCase();
                if (!List.of("ADMIN", "MEMBER", "VIEWER").contains(formattedRole)) {
                        return ResponseEntity.badRequest()
                                        .body(Map.of("message", "Invalid role. Must be ADMIN, MEMBER, or VIEWER"));
                }

                // Find the target member's membership
                Membership targetMembership = membershipRepository
                                .findByUserIdAndWorkspaceId(memberId, workspaceId)
                                .orElse(null);

                if (targetMembership == null) {
                        return ResponseEntity.notFound().build();
                }

                // Even if the requester is an Admin, they cannot change the Owner's role
                Workspace workspace = targetMembership.getWorkspace();
                if (workspace.getOwner().getId().equals(memberId)) {
                        return ResponseEntity.status(403)
                                        .body(Map.of("message", "Cannot change the role of the Workspace Owner"));
                }

                // A non-owner Admin cannot change the role of another Admin
                boolean isTargetAdmin = "ADMIN".equalsIgnoreCase(targetMembership.getRole());
                boolean amIOwner = workspace.getOwner().getId().equals(currentUser.getId());

                if (isTargetAdmin && !amIOwner) {
                        return ResponseEntity.status(403)
                                        .body(Map.of("message",
                                                        "Only the Workspace Owner can change the role of other Admins"));
                }

                // Update and Save
                targetMembership.setRole(formattedRole);
                membershipRepository.save(targetMembership);

                return ResponseEntity.ok(Map.of("message", "Member role updated successfully", "role", formattedRole));
        }

        @GetMapping("/{workspaceId}/owner")
        @Transactional(readOnly = true)
        public ResponseEntity<Map<String, String>> getWorkspaceOwner(
                        @PathVariable UUID workspaceId,
                        Authentication auth) {

                // Find Current User
                User user = userRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Security Check: Verify user is a member of this workspace
                if (!membershipRepository.existsByUserIdAndWorkspaceId(user.getId(), workspaceId)) {
                        return ResponseEntity.status(403).build();
                }

                // Find Workspace
                Workspace workspace = workspaceRepository.findById(workspaceId)
                                .orElseThrow(() -> new RuntimeException("Workspace not found"));

                // Return just the Owner ID
                return ResponseEntity.ok(Map.of("ownerId", workspace.getOwner().getId().toString()));
        }
}
