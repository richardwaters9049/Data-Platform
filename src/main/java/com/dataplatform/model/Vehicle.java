package com.dataplatform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vin; // Vehicle Identification Number
    private String make;
    private String model;
    private Integer year;
    private String trim;
    private String color;
    private String fuelType;
    private String transmission;
    private Double engineSize;
    private String bodyStyle;

    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    private String status; // NEW, USED, SOLD, etc.
}
