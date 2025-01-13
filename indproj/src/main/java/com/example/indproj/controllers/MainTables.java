package com.example.indproj.controllers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class MainTables {

    private final EntityManager entityManager;

    public MainTables(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

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

