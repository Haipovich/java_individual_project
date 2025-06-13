package com.example.indproj.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Сущность штрафа
 * Представляет информацию о нарушении правил дорожного движения
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Fine {
    
    /**
     * Уникальный идентификатор штрафа
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fine_id")
    private Long id;

    /**
     * Дата выписки штрафа
     */
    @NotNull
    @Column(name = "issue_date", nullable = false)
    private java.time.LocalDate issueDate;

    /**
     * Описание нарушения
     */
    @NotNull
    @Size(max = 255, message = "Описание нарушения не может превышать 255 символов")
    @Column(name = "violation", nullable = false)
    private String violation;

    /**
     * Сумма штрафа в рублях
     */
    @NotNull
    @Min(value = 1, message = "Сумма штрафа должна быть больше 0")
    @Column(name = "amount", nullable = false)
    private Integer amount;

    /**
     * Автомобиль, на который выписан штраф
     */
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    /**
     * Водитель, которому выписан штраф
     */
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;
}

