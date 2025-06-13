package com.example.indproj.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * Сущность водителя
 * Представляет информацию о водителе транспортного средства
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Driver {
    
    /**
     * Уникальный идентификатор водителя
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_id")
    private Long id;

    /**
     * ФИО водителя
     */
    @NotNull
    @Size(max = 100, message = "Имя не может превышать 100 символов")
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Номер водительского удостоверения
     */
    @NotNull
    @Size(max = 15, message = "Номер удостоверения не может превышать 15 символов")
    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;

    /**
     * Дата рождения водителя
     */
    @NotNull
    @Column(name = "birth_date", nullable = false)
    private java.time.LocalDate birthDate;

    /**
     * Номер телефона водителя
     */
    @Size(max = 15, message = "Номер телефона не может превышать 15 символов")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]+$", message = "Неверный формат номера телефона")
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * Адрес водителя
     */
    @Size(max = 255, message = "Адрес не может превышать 255 символов")
    @Column(name = "address")
    private String address;
}

