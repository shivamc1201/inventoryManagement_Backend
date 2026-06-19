package com.nector.userservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_KPI_update")
@Data
public class SalesKpiUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String userName;

    @Column(nullable = false, length = 20)
    private String empCode;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_person_id")
    private com.nector.userservice.model.SalesPerson salesPerson;

    @Column(nullable = false)
    private LocalDate date;

    private Double totalDistanceInKm;

    private Integer noOfMeetings;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "salesKpiUpdate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesKpiMeetingDetail> meetingDetails = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
