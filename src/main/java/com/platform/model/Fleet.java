package com.platform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fleet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fleetName;
    private String fleetCode;
    private String company;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;

    private Integer vehicleCount;
    private String status; // ACTIVE, INACTIVE, MAINTENANCE
}
