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
@Table(name = "BusyBlocks")
public class BusyBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blockId")
    private Integer blockId;

    @Column(name = "feedId", nullable = false)
    private Integer feedId;

    private String uid;

    @Column(name = "startAt", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "endAt", nullable = false)
    private LocalDateTime endAt;

    private String summary;
}
