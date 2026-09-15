package com.iescamp.studyflow_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "CalendarFeeds")
public class CalendarFeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedId")
    private Integer feedId;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "lastSyncedAt")
    private LocalDateTime lastSyncedAt;
}
