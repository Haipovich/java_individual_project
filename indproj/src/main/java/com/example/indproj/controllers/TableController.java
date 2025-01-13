package com.example.indproj.controllers;
import com.example.indproj.model.*;
import com.example.indproj.services.TableService;
import jakarta.persistence.metamodel.EntityType;
import com.example.indproj.utils.TableUtils;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/table")
public class TableController {

    private final EntityManager entityManager;
    private final TableService tableService;

    public TableController(EntityManager entityManager, TableService tableService) {
        this.entityManager = entityManager;
        this.tableService = tableService;

    }

    @GetMapping("/{tableName}")
    public String tableDetails(@PathVariable String tableName, Model model) {
        Class<?> entityClass = getEntityClass(tableName);
        if (entityClass == null) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }

        List<String> headers = TableUtils.getHeaders(entityClass);

        List<Object> entities = (List<Object>) entityManager.createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                .getResultList();
        List<List<Object>> rows = entities.stream()
                .map(TableUtils::toRow)
                .toList();

        model.addAttribute("tableName", tableName);
        model.addAttribute("headers", headers);
        model.addAttribute("rows", rows);

        return "table_data";
    }

    @PostMapping("/{tableName}/delete/{id}")
    public String deleteRow(@PathVariable String tableName, @PathVariable Long id) {
        Class<?> entityClass = getEntityClass(tableName);
        tableService.deleteEntityById(entityClass, id); // Вызов сервиса для удаления
        return "redirect:/table/" + tableName;
    }

    // Метод для сопоставления имени таблицы и класса сущности
    private Class<?> getEntityClass(String tableName) {
        return entityManager.getMetamodel()
                .getEntities()
                .stream()
                .filter(entity -> entity.getName().equalsIgnoreCase(tableName))
                .map(EntityType::getJavaType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid table name: " + tableName));
    }

    @PostMapping("/{tableName}/add")
    public String addRow(@PathVariable String tableName, @RequestParam Map<String, String> formData, Model model) {
        Class<?> entityClass = getEntityClass(tableName);
        List<String> headers = TableUtils.getHeaders(entityClass);

        try {
            // Валидация данных
            TableUtils.validateFormData(entityClass, formData);

            // Создание и сохранение сущности
            Object entity = TableUtils.createEntityFromForm(entityClass, formData, entityManager);
            tableService.saveEntity(entity);

            return "redirect:/table/" + tableName;
        } catch (IllegalArgumentException e) {
            // Обработка ошибок
            model.addAttribute("tableName", tableName);
            model.addAttribute("headers", headers);
            model.addAttribute("errorMessage", e.getMessage());

            // Загрузка текущих данных таблицы
            List<Object> entities = (List<Object>) entityManager.createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                    .getResultList();
            List<List<Object>> rows = entities.stream()
                    .map(TableUtils::toRow)
                    .toList();

            model.addAttribute("rows", rows);

            return "table_data"; // Вернуться на ту же страницу с сообщением об ошибке
        }
    }

    @GetMapping("/{tableName}/edit/{id}")
    public String editRow(@PathVariable String tableName, @PathVariable Long id, Model model) {
        Class<?> entityClass = getEntityClass(tableName);
        Object entity = tableService.findById(entityClass, id);
        if (entity == null) {
            throw new IllegalArgumentException("Entity with id " + id + " not found.");
        }

        // Преобразование полей для отображения
        Map<String, Object> entityData = new HashMap<>();
        for (Field field : entityClass.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);

                // Проверяем, если поле — это объект с полем "id"
                if (value != null && !isPrimitiveOrWrapper(value.getClass()) && !value.getClass().equals(String.class)) {
                    try {
                        Field idField = value.getClass().getDeclaredField("id");
                        idField.setAccessible(true);
                        value = idField.get(value); // Получаем id связанного объекта
                    } catch (NoSuchFieldException e) {
                        // Поле не содержит "id", оставляем как есть
                    }
                }
                entityData.put(field.getName(), value);
            } catch (Exception e) {
                throw new RuntimeException("Error processing field: " + field.getName(), e);
            }
        }

        List<String> headers = TableUtils.getHeaders(entityClass);

        model.addAttribute("tableName", tableName);
        model.addAttribute("entity", entityData); // Передаем обработанные данные
        model.addAttribute("headers", headers);

        return "edit_row";
    }

    // Проверка, является ли класс примитивом или его оберткой
    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Boolean.class ||
                clazz == Byte.class ||
                clazz == Character.class ||
                clazz == Double.class ||
                clazz == Float.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Short.class;
    }


    @PostMapping("/{tableName}/edit/{id}")
    public String updateRow(
            @PathVariable String tableName,
            @PathVariable Long id,
            @RequestParam Map<String, String> formData,
            Model model) {
        Class<?> entityClass = getEntityClass(tableName);
        try {
            tableService.updateEntityFromForm(entityClass, id, formData);
            return "redirect:/table/" + tableName;
        } catch (IllegalArgumentException e) {
            Object entity = tableService.findById(entityClass, id);
            if (entity == null) {
                throw new IllegalArgumentException("Entity with id " + id + " not found.");
            }
            Map<String, Object> entityData = new HashMap<>();
            for (Field field : entityClass.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);

                    // Проверяем, если поле — это объект с полем "id"
                    if (value != null && !isPrimitiveOrWrapper(value.getClass()) && !value.getClass().equals(String.class)) {
                        try {
                            Field idField = value.getClass().getDeclaredField("id");
                            idField.setAccessible(true);
                            value = idField.get(value); // Получаем id связанного объекта
                        } catch (NoSuchFieldException ex) {
                            // Поле не содержит "id", оставляем как есть
                        }
                    }
                    entityData.put(field.getName(), value);
                } catch (Exception ex) {
                    throw new RuntimeException("Error processing field: " + field.getName(), ex);
                }
            }

            List<String> headers = TableUtils.getHeaders(entityClass);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tableName", tableName);
            model.addAttribute("entity", entityData); // Передаем обработанные данные
            model.addAttribute("headers", headers);

            return "edit_row";
        } catch (Exception e) {
            Object entity = tableService.findById(entityClass, id);
            if (entity == null) {
                throw new IllegalArgumentException("Entity with id " + id + " not found.");
            }
            List<String> headers = TableUtils.getHeaders(entityClass);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("formData", formData);
            model.addAttribute("tableName", tableName);
            model.addAttribute("id", id);
            model.addAttribute("entity", entity);
            model.addAttribute("headers", headers);
            return "edit_row";
        }
    }
}

