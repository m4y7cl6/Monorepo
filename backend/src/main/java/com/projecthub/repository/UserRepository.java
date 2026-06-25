package com.projecthub.repository;

import com.projecthub.entity.User;
import com.projecthub.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByKeycloakId(String keycloakId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAllByDeletedAtIsNull();

    List<User> findAllByStatusAndDeletedAtIsNull(UserStatus status);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL ORDER BY u.displayName ASC")
    List<User> findAllActiveUsers();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
