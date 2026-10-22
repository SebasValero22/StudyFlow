package com.iescamp.studyflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VaultItem {
    private Integer vaultItemId;

    @JsonProperty("userId")
    private Integer userId;

    private String title;

    private String domain; // "work" or "life"

    private String reason;

    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String syncStatus;
    private String clientUuid;
}