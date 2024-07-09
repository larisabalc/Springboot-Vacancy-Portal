package com.lab.model.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Optional;

/**
 * JPA Entities
 */
@Entity
@Table(name="days_off")
public class DaysOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="start_date")
    private LocalDate startDate;

    @Column(name="end_date")
    private LocalDate endDate;

    @Column(name="is_approved")
    private Boolean isApproved;

    @Column(name="message")
    private String message;

    @ManyToOne
    private UserEntity user;

    public DaysOff() {}

    public DaysOff(LocalDate startDate, LocalDate endDate, Boolean isApproved, String message, UserEntity user) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.isApproved = isApproved;
        this.message = message;
        this.user = user;
    }

    // Getter methods
    public Long getId() {
        return id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public String getMessage() {
        return message;
    }

    public UserEntity getUser() {
        return user;
    }
}
