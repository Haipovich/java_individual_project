package com.example.indproj.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Сущность автомобиля
 * Представляет информацию о транспортном средстве
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Car {
    
    /**
     * Уникальный идентификатор автомобиля
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Long id;

    /**
     * Страна производства автомобиля
     */
    @NotNull
    @Size(max = 50, message = "Название страны не может превышать 50 символов")
    @Column(name = "country", nullable = false)
    private String country;

    /**
     * Модель автомобиля
     */
    @NotNull
    @Size(max = 50, message = "Название модели не может превышать 50 символов")
    @Column(name = "model", nullable = false)
    private String model;

    /**
     * Государственный номер автомобиля
     */
    @NotNull
    @Size(max = 15, message = "Номер не может превышать 15 символов")
    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    /**
     * Год выпуска автомобиля
     */
    @NotNull
    @Min(value = 1900, message = "Год выпуска не может быть меньше 1900")
    @Max(value = 2030, message = "Год выпуска не может быть больше 2030")
    @Column(name = "year", nullable = false)
    private Integer year;

    /**
     * Водитель автомобиля
     */
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;
}

