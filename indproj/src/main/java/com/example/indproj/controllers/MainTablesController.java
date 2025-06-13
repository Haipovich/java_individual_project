package com.example.indproj.controllers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Главный контроллер для отображения списка таблиц
 * Предоставляет доступ к основным страницам приложения
 */
@Controller
@RequestMapping("/")
public class MainTablesController {

    private final EntityManager entityManager;

    /**
     * Конструктор контроллера
     * 
     * @param entityManager менеджер сущностей для получения метаданных
     */
    public MainTablesController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Отображает список всех доступных таблиц
     * 
     * @param model модель для передачи данных в представление
     * @return имя представления со списком таблиц
     */
    @GetMapping
    public String listTables(Model model) {
        // Получаем список всех сущностей
        List<String> tables = entityManager.getMetamodel()
                .getEntities()
                .stream()
                .map(EntityType::getName)  // Извлекаем имя каждой сущности
                .collect(Collectors.toList());

        model.addAttribute("tables", tables);  // Добавляем в модель

        return "tables";
    }
}

