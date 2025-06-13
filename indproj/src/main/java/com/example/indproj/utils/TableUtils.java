package com.example.indproj.utils;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Map;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Утилитарный класс для работы с таблицами сущностей
 * Предоставляет методы для преобразования данных и работы с рефлексией
 */
public class TableUtils {

    /**
     * Получает заголовки таблицы на основе полей класса
     * 
     * @param clazz класс сущности
     * @return список имен полей класса
     */
    public static List<String> getHeaders(Class<?> clazz) {
        return List.of(clazz.getDeclaredFields()).stream()
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует сущность в строку таблицы
     * 
     * @param entity сущность для преобразования
     * @return список значений полей сущности
     */
    public static List<Object> toRow(Object entity) {
        List<Object> row = new ArrayList<>();
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);

                if (value != null && value.getClass().isAnnotationPresent(Entity.class)) {
                    Field idField = value.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    value = idField.get(value);
                }

                row.add(value);
            } catch (Exception e) {
                row.add("Error");
            }
        }
        return row;
    }

    /**
     * Создает сущность из данных формы
     * 
     * @param entityClass класс создаваемой сущности
     * @param formData данные формы
     * @param entityManager менеджер сущностей
     * @return созданная сущность или null при ошибке
     */
    public static Object createEntityFromForm(Class<?> entityClass, 
            Map<String, String> formData, EntityManager entityManager) {
        try {
            Object entity = entityClass.getDeclaredConstructor().newInstance();

            for (Map.Entry<String, String> entry : formData.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();

                Field field = entityClass.getDeclaredField(fieldName);
                field.setAccessible(true);

                if (fieldValue == null || fieldValue.isBlank()) {
                    continue;
                }

                if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                    field.set(entity, Integer.parseInt(fieldValue));
                } else if (field.getType().equals(long.class) 
                        || field.getType().equals(Long.class)) {
                    field.set(entity, Long.parseLong(fieldValue));
                } else if (field.getType().equals(double.class) 
                        || field.getType().equals(Double.class)) {
                    field.set(entity, Double.parseDouble(fieldValue));
                } else if (field.getType().equals(boolean.class) 
                        || field.getType().equals(Boolean.class)) {
                    field.set(entity, Boolean.parseBoolean(fieldValue));
                } else if (field.getType().equals(java.time.LocalDate.class)) {
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    field.set(entity, LocalDate.parse(fieldValue, dateFormatter));
                } else if (field.getType().equals(java.sql.Timestamp.class)) {
                    SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    timestampFormat.setLenient(false);
                    field.set(entity, new java.sql.Timestamp(
                            timestampFormat.parse(fieldValue).getTime()));
                } else if (field.getType().equals(String.class)) {
                    field.set(entity, fieldValue);
                } else if (field.getType().equals(java.math.BigDecimal.class)) {
                    field.set(entity, new java.math.BigDecimal(fieldValue));
                } else {
                    Object relatedEntity = entityManager.find(field.getType(), 
                            Long.parseLong(fieldValue));
                    if (relatedEntity == null) {
                        System.out.println("Ошибка: Сущность с ID " + fieldValue 
                                + " не найдена для поля " + fieldName);
                        return null;
                    }
                    field.set(entity, relatedEntity);
                }
            }

            return entity;
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка создания сущности из данных формы: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Ошибка создания сущности из данных формы: " + e.getMessage());
            return null;
        }
    }

    /**
     * Валидирует данные формы
     * 
     * @param entityClass класс сущности для валидации
     * @param formData данные формы для проверки
     * @return true если данные валидны, false в противном случае
     */
    public static boolean validateFormData(Class<?> entityClass, Map<String, String> formData) {
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            if (fieldValue == null || fieldValue.isBlank()) {
                System.out.println("Ошибка: Поле " + fieldName + " не может быть пустым");
                return false;
            }

            try {
                Field field = entityClass.getDeclaredField(fieldName);

                if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                    Integer.parseInt(fieldValue);
                } else if (field.getType().equals(double.class) 
                        || field.getType().equals(Double.class)) {
                    Double.parseDouble(fieldValue);
                } else if (field.getType().equals(long.class) 
                        || field.getType().equals(Long.class)) {
                    Long.parseLong(fieldValue);
                } else if (field.getType().equals(String.class) && fieldValue.length() > 255) {
                    System.out.println("Ошибка: Поле " + fieldName 
                            + " превышает максимальную длину 255 символов");
                    return false;
                } else if (field.getType().equals(boolean.class) 
                        || field.getType().equals(Boolean.class)) {
                    if (!fieldValue.equalsIgnoreCase("true") 
                            && !fieldValue.equalsIgnoreCase("false")) {
                        System.out.println("Ошибка: Поле " + fieldName 
                                + " должно быть булевым значением ('true' или 'false')");
                        return false;
                    }
                } else if (field.getType().equals(java.util.Date.class)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateFormat.setLenient(false);
                    Date parsedDate = dateFormat.parse(fieldValue);
                } else if (field.getType().equals(java.sql.Timestamp.class)) {
                    SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    timestampFormat.setLenient(false);
                    Date parsedTimestamp = timestampFormat.parse(fieldValue);
                } else if (field.getType().equals(java.math.BigDecimal.class)) {
                    new java.math.BigDecimal(fieldValue);
                }
            } catch (NoSuchFieldException e) {
                System.out.println("Ошибка: Поле " + fieldName + " не существует");
                return false;
            } catch (NumberFormatException | ParseException e) {
                System.out.println("Ошибка: Неверное значение для поля " + fieldName);
                return false;
            }
        }
        return true;
    }

    /**
     * Преобразует строковое значение в соответствующий тип данных
     * 
     * @param fieldType тип поля для преобразования
     * @param fieldValue строковое значение для преобразования
     * @return преобразованное значение или null при ошибке
     */
    public static Object convertData(Class<?> fieldType, String fieldValue) {
        if (fieldValue == null || fieldValue.isBlank()) {
            return null;
        }

        try {
            if (fieldType.equals(String.class)) {
                return fieldValue;
            } else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                return Long.parseLong(fieldValue);
            } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                return Integer.parseInt(fieldValue);
            } else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
                return Double.parseDouble(fieldValue);
            } else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
                return Float.parseFloat(fieldValue);
            } else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                return Boolean.parseBoolean(fieldValue);
            } else if (fieldType.equals(java.time.LocalDate.class)) {
                return java.time.LocalDate.parse(fieldValue);
            } else if (fieldType.equals(java.time.LocalDateTime.class)) {
                return java.time.LocalDateTime.parse(fieldValue);
            } else if (fieldType.equals(java.util.Date.class)) {
                return java.sql.Date.valueOf(fieldValue);
            } else if (fieldType.isEnum()) {
                return Enum.valueOf((Class<Enum>) fieldType, fieldValue.toUpperCase());
            } else {
                System.out.println("Ошибка: Неподдерживаемый тип поля: " + fieldType.getName());
                return null;
            }
        } catch (Exception e) {
            System.out.println("Ошибка преобразования значения '" + fieldValue 
                    + "' для типа " + fieldType.getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }
}

