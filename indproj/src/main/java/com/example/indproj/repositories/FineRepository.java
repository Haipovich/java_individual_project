package com.example.indproj.repositories;

import com.example.indproj.model.Fine;
import com.example.indproj.model.Driver;
import com.example.indproj.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Репозиторий для работы со штрафами
 * Предоставляет методы для доступа к данным штрафов
 */
@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    
    /**
     * Находит все штрафы водителя
     * 
     * @param driver водитель
     * @return список штрафов водителя
     */
    List<Fine> findByDriver(Driver driver);
    
    /**
     * Находит все штрафы автомобиля
     * 
     * @param car автомобиль
     * @return список штрафов автомобиля
     */
    List<Fine> findByCar(Car car);
    
    /**
     * Находит штрафы по диапазону дат
     * 
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return список штрафов в указанном диапазоне дат
     */
    List<Fine> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);
} 
