package com.platform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceNumber;
    private String serviceType; // OIL_CHANGE, TIRE_ROTATION, BRAKE_SERVICE, etc.
    private LocalDate serviceDate;
    private Integer mileage;
    private String description;
    private Double cost;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    private String technician;
    private String status; // COMPLETED, SCHEDULED, CANCELLED
}
