package com.example.indproj.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(name = "country", nullable = false)
    private String country;

    @NotNull
    @Size(max = 50)
    @Column(name = "model", nullable = false)
    private String model;

    @NotNull
    @Size(max = 15)
    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @NotNull
    @Column(name = "year", nullable = false)
    private Integer year;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;
}

