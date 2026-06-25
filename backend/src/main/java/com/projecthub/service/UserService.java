package com.projecthub.service;

import com.projecthub.dto.UserDto;
import com.projecthub.entity.User;
import com.projecthub.entity.enums.UserRole;
import com.projecthub.entity.enums.UserStatus;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.mapper.UserMapper;
import com.projecthub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public List<UserDto> findAll() {
        return userMapper.toDtoList(userRepository.findAllActiveUsers());
    }

    public UserDto findById(UUID id) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDto(user);
    }

    public UserDto findByKeycloakId(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", keycloakId));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto upsertFromToken(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String username = jwt.getClaimAsString("preferred_username");
        String displayName = jwt.getClaimAsString("name");

        return userRepository.findByKeycloakId(keycloakId)
                .map(existing -> {
                    existing.setEmail(email != null ? email : existing.getEmail());
                    existing.setDisplayName(displayName != null ? displayName : existing.getDisplayName());
                    existing.setStatus(UserStatus.ACTIVE);
                    User saved = userRepository.save(existing);
                    log.debug("Updated existing user from JWT: {}", saved.getUsername());
                    return userMapper.toDto(saved);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .keycloakId(keycloakId)
                            .username(username != null ? username : keycloakId)
                            .email(email != null ? email : keycloakId + "@unknown.com")
                            .displayName(displayName != null ? displayName : username)
                            .role(UserRole.DEVELOPER)
                            .status(UserStatus.ACTIVE)
                            .build();
                    User saved = userRepository.save(newUser);
                    log.info("Created new user from JWT token: {}", saved.getUsername());
                    return userMapper.toDto(saved);
                });
    }

    @Transactional
    public void softDelete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setDeletedAt(LocalDateTime.now());
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("Soft deleted user: {}", id);
    }
}
