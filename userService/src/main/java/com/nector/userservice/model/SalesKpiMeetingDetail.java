package com.nector.userservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sales_kpi_meeting_details")
@Data
public class SalesKpiMeetingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_kpi_update_id", nullable = false)
    private SalesKpiUpdate salesKpiUpdate;

    @Column(length = 150)
    private String clientName;

    @Column(length = 150)
    private String contactPerson;

    @Column(length = 15)
    private String clientContactNo;

    @Column(length = 100)
    private String clientEmail;

    @Column(length = 50)
    private String type;

    @Column(length = 500)
    private String meetingAddress;

    @Column(length = 20)
    private String createDate;

    @Column(length = 20)
    private String createTime;
}
