package com.strideboard.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    
    // Finds the user by their unique email
    Optional<User> findByEmail(String email);

    // Quickly check if an email is already taken
    // Returns true or false
    boolean existsByEmail(String email);
}