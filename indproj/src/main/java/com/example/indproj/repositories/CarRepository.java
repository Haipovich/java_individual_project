package com.example.indproj.repositories;

import com.example.indproj.model.Car;
import com.example.indproj.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с автомобилями
 * Предоставляет методы для доступа к данным автомобилей
 */
@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    
    /**
     * Находит автомобиль по государственному номеру
     * 
     * @param licensePlate государственный номер автомобиля
     * @return автомобиль с указанным номером
     */
    Optional<Car> findByLicensePlate(String licensePlate);
    
    /**
     * Проверяет существование автомобиля по номеру
     * 
     * @param licensePlate государственный номер автомобиля
     * @return true если автомобиль существует
     */
    boolean existsByLicensePlate(String licensePlate);
    
    /**
     * Находит все автомобили водителя
     * 
     * @param driver водитель
     * @return список автомобилей водителя
     */
    List<Car> findByDriver(Driver driver);
} 
