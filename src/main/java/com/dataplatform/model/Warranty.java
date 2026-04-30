package com.dataplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warranty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String warrantyNumber;
    private String warrantyType; // BASIC, POWERTRAIN, EXTENDED, etc.
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer mileageLimit;
    private String coverage; // BUMPER_TO_BUMPER, POWERTRAIN, etc.
    private Double deductible;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private String provider;
    private String status; // ACTIVE, EXPIRED, CLAIMED, etc.
}
