package com.strideboard.data.workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    List<Membership> findByUserId(UUID userId);

    boolean existsByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);

    Optional<Membership> findByUserIdAndWorkspaceId(UUID userId, UUID workspaceId);

    List<Membership> findByWorkspaceId(UUID workspaceId);
}
