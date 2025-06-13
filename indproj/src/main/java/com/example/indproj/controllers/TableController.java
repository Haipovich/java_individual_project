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

/**
 * Контроллер для работы с таблицами сущностей
 * Предоставляет REST API для CRUD операций с сущностями
 */
@Controller
@RequestMapping("/table")
public class TableController {

    private final EntityManager entityManager;
    private final TableService tableService;

    /**
     * Конструктор контроллера
     * 
     * @param entityManager менеджер сущностей
     * @param tableService сервис для работы с таблицами
     */
    public TableController(EntityManager entityManager, TableService tableService) {
        this.entityManager = entityManager;
        this.tableService = tableService;
    }

    /**
     * Отображает детали таблицы с данными
     * 
     * @param tableName имя таблицы для отображения
     * @param model модель для передачи данных в представление
     * @return имя представления
     */
    @GetMapping("/{tableName}")
    public String tableDetails(@PathVariable String tableName, Model model) {
        Class<?> entityClass = getEntityClass(tableName);
        if (entityClass == null) {
            System.out.println("Ошибка: Неверное имя таблицы: " + tableName);
            return "error";
        }

        List<String> headers = TableUtils.getHeaders(entityClass);

        List<Object> entities = (List<Object>) entityManager
                .createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                .getResultList();
        List<List<Object>> rows = entities.stream()
                .map(TableUtils::toRow)
                .toList();

        model.addAttribute("tableName", tableName);
        model.addAttribute("headers", headers);
        model.addAttribute("rows", rows);

        return "table_data";
    }

    /**
     * Удаляет строку из таблицы
     * 
     * @param tableName имя таблицы
     * @param id идентификатор удаляемой строки
     * @return перенаправление на таблицу
     */
    @PostMapping("/{tableName}/delete/{id}")
    public String deleteRow(@PathVariable String tableName, @PathVariable Long id) {
        Class<?> entityClass = getEntityClass(tableName);
        tableService.deleteEntityById(entityClass, id);
        return "redirect:/table/" + tableName;
    }

    /**
     * Получает класс сущности по имени таблицы
     * 
     * @param tableName имя таблицы
     * @return класс сущности или null если не найден
     */
    private Class<?> getEntityClass(String tableName) {
        try {
            return entityManager.getMetamodel()
                    .getEntities()
                    .stream()
                    .filter(entity -> entity.getName().equalsIgnoreCase(tableName))
                    .map(EntityType::getJavaType)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.out.println("Ошибка получения класса сущности для таблицы " 
                    + tableName + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Добавляет новую строку в таблицу
     * 
     * @param tableName имя таблицы
     * @param formData данные формы
     * @param model модель для передачи данных в представление
     * @return имя представления или перенаправление
     */
    @PostMapping("/{tableName}/add")
    public String addRow(@PathVariable String tableName, 
            @RequestParam Map<String, String> formData, Model model) {
        Class<?> entityClass = getEntityClass(tableName);
        List<String> headers = TableUtils.getHeaders(entityClass);

        try {
            if (!TableUtils.validateFormData(entityClass, formData)) {
                model.addAttribute("tableName", tableName);
                model.addAttribute("headers", headers);
                model.addAttribute("errorMessage", "Ошибка валидации данных");

                List<Object> entities = (List<Object>) entityManager
                        .createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                        .getResultList();
                List<List<Object>> rows = entities.stream()
                        .map(TableUtils::toRow)
                        .toList();

                model.addAttribute("rows", rows);
                return "table_data";
            }

            Object entity = TableUtils.createEntityFromForm(entityClass, formData, entityManager);
            if (entity != null) {
                tableService.saveEntity(entity);
                return "redirect:/table/" + tableName;
            } else {
                model.addAttribute("tableName", tableName);
                model.addAttribute("headers", headers);
                model.addAttribute("errorMessage", "Ошибка создания сущности");

                List<Object> entities = (List<Object>) entityManager
                        .createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                        .getResultList();
                List<List<Object>> rows = entities.stream()
                        .map(TableUtils::toRow)
                        .toList();

                model.addAttribute("rows", rows);
                return "table_data";
            }
        } catch (Exception e) {
            System.out.println("Ошибка добавления строки: " + e.getMessage());
            model.addAttribute("tableName", tableName);
            model.addAttribute("headers", headers);
            model.addAttribute("errorMessage", "Ошибка добавления строки: " + e.getMessage());

            List<Object> entities = (List<Object>) entityManager
                    .createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                    .getResultList();
            List<List<Object>> rows = entities.stream()
                    .map(TableUtils::toRow)
                    .toList();

            model.addAttribute("rows", rows);
            return "table_data";
        }
    }

    /**
     * Отображает форму редактирования строки
     * 
     * @param tableName имя таблицы
     * @param id идентификатор редактируемой строки
     * @param model модель для передачи данных в представление
     * @return имя представления
     */
    @GetMapping("/{tableName}/edit/{id}")
    public String editRow(@PathVariable String tableName, @PathVariable Long id, Model model) {
        Class<?> entityClass = getEntityClass(tableName);
        Object entity = tableService.findById(entityClass, id);
        if (entity == null) {
            System.out.println("Ошибка: Сущность с id " + id + " не найдена.");
            return "error";
        }

        Map<String, Object> entityData = new HashMap<>();
        for (Field field : entityClass.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);

                if (value != null && !isPrimitiveOrWrapper(value.getClass()) 
                        && !value.getClass().equals(String.class)) {
                    try {
                        Field idField = value.getClass().getDeclaredField("id");
                        idField.setAccessible(true);
                        value = idField.get(value);
                    } catch (NoSuchFieldException e) {
                    }
                }
                entityData.put(field.getName(), value);
            } catch (Exception e) {
                System.out.println("Ошибка обработки поля: " + field.getName() 
                        + ": " + e.getMessage());
                return "error";
            }
        }

        List<String> headers = TableUtils.getHeaders(entityClass);

        model.addAttribute("tableName", tableName);
        model.addAttribute("entity", entityData);
        model.addAttribute("headers", headers);

        return "edit_row";
    }

    /**
     * Проверяет, является ли класс примитивом или его оберткой
     * 
     * @param clazz класс для проверки
     * @return true если класс является примитивом или оберткой
     */
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

    /**
     * Обновляет строку в таблице
     * 
     * @param tableName имя таблицы
     * @param id идентификатор обновляемой строки
     * @param formData данные формы
     * @param model модель для передачи данных в представление
     * @return имя представления или перенаправление
     */
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
        } catch (Exception e) {
            System.out.println("Ошибка обновления строки: " + e.getMessage());
            Object entity = tableService.findById(entityClass, id);
            if (entity == null) {
                System.out.println("Ошибка: Сущность с id " + id + " не найдена.");
                return "error";
            }
            Map<String, Object> entityData = new HashMap<>();
            for (Field field : entityClass.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);

                    if (value != null && !isPrimitiveOrWrapper(value.getClass()) 
                            && !value.getClass().equals(String.class)) {
                        try {
                            Field idField = value.getClass().getDeclaredField("id");
                            idField.setAccessible(true);
                            value = idField.get(value);
                        } catch (NoSuchFieldException ex) {
                        }
                    }
                    entityData.put(field.getName(), value);
                } catch (Exception ex) {
                    System.out.println("Ошибка обработки поля: " + field.getName() 
                            + ": " + ex.getMessage());
                    return "error";
                }
            }

            List<String> headers = TableUtils.getHeaders(entityClass);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tableName", tableName);
            model.addAttribute("entity", entityData);
            model.addAttribute("headers", headers);

            return "edit_row";
        }
    }
}

