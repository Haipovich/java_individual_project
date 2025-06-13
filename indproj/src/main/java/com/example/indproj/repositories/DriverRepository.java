package com.example.indproj.repositories;

import com.example.indproj.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с водителями
 * Предоставляет методы для доступа к данным водителей
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    
    /**
     * Находит водителя по номеру водительского удостоверения
     * 
     * @param licenseNumber номер водительского удостоверения
     * @return водитель с указанным номером удостоверения
     */
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    
    /**
     * Проверяет существование водителя по номеру удостоверения
     * 
     * @param licenseNumber номер водительского удостоверения
     * @return true если водитель существует
     */
    boolean existsByLicenseNumber(String licenseNumber);
} 
