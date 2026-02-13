package com.iescamp.studyflow_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "operationhistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer historyId;

    private String description;
    private String actionType; // e.g., "CREATE", "UPDATE", "DELETE"

    @Temporal(TemporalType.TIMESTAMP)
    private Date operationDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}