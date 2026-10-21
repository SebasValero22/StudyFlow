package com.iescamp.studyflow_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * "Say No" Vault item - quarantined ideas to protect Main Quest focus for 84 days.
 */
@Entity
@Table(name = "VaultItems")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deletedAt IS NULL")
public class VaultItem {

    @PrePersist
    void onCreate() {
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vaultItemId")
    private Integer vaultItemId;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private String title;

    @Column(name = "domain")
    private String domain; // "work" or "life"

    @Column(name = "reason")
    private String reason; // why it's quarantined

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt;

    @Column(name = "clientUuid")
    private String clientUuid;
}