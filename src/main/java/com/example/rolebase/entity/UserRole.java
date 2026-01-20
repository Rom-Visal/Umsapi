package com.example.rolebase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "user_roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_role",
                columnNames = {"user_id", "role_id"}))
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "role_id")
    private Role role;

    private LocalDateTime assignAt;
    private String assignBy;

    @PrePersist
    public void onCreate() {
        this.assignAt = LocalDateTime.now();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String currentPrincipalName = authentication.getName();

            this.assignBy = Objects.equals("anonymousUser".toLowerCase(),
                    currentPrincipalName.toLowerCase())
                    ? "SELF_REGISTERED"
                    : currentPrincipalName;

        } else {
            this.assignBy = Objects.requireNonNullElse(this.assignBy, "SYSTEM");
        }
    }
}
