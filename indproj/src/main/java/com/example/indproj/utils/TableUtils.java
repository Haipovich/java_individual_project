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

public class TableUtils {

    public static List<String> getHeaders(Class<?> clazz) {
        return List.of(clazz.getDeclaredFields()).stream()
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    public static List<Object> toRow(Object entity) {
        List<Object> row = new ArrayList<>();
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);

                // Если поле ссылается на сущность, извлекаем только id
                if (value != null && value.getClass().isAnnotationPresent(Entity.class)) {
                    Field idField = value.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    value = idField.get(value); // Получаем id вместо объекта
                }

                row.add(value);
            } catch (Exception e) {
                row.add("Error");
            }
        }
        return row;
    }

    public static Object createEntityFromForm(Class<?> entityClass, Map<String, String> formData, EntityManager entityManager) {
        try {
            Object entity = entityClass.getDeclaredConstructor().newInstance();

            for (Map.Entry<String, String> entry : formData.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = entry.getValue();

                Field field = entityClass.getDeclaredField(fieldName);
                field.setAccessible(true);

                if (fieldValue == null || fieldValue.isBlank()) {
                    continue; // Пропускаем пустые значения, они не должны перезаписывать поля
                }

                if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                    field.set(entity, Integer.parseInt(fieldValue));
                } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                    field.set(entity, Long.parseLong(fieldValue));
                } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                    field.set(entity, Double.parseDouble(fieldValue));
                } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                    field.set(entity, Boolean.parseBoolean(fieldValue));
                } else if (field.getType().equals(java.time.LocalDate.class)) {
                    // Формат даты, например, "yyyy-MM-dd"
                    System.out.println("дата формат");
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    field.set(entity, LocalDate.parse(fieldValue, dateFormatter));
                } else if (field.getType().equals(java.sql.Timestamp.class)) {
                    // Формат временной метки, например, "yyyy-MM-dd HH:mm:ss"
                    SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    timestampFormat.setLenient(false);
                    field.set(entity, new java.sql.Timestamp(timestampFormat.parse(fieldValue).getTime()));
                } else if (field.getType().equals(String.class)) {
                    field.set(entity, fieldValue);
                } else if (field.getType().equals(java.math.BigDecimal.class)) {
                    field.set(entity, new java.math.BigDecimal(fieldValue));
                } else {
                    // Для связанных сущностей
                    Object relatedEntity = entityManager.find(field.getType(), Long.parseLong(fieldValue));
                    if (relatedEntity == null) {
                        throw new IllegalArgumentException("No entity with ID " + fieldValue + " for field " + fieldName);
                    }
                    field.set(entity, relatedEntity);
                }
            }

            return entity;
        } catch (IllegalArgumentException e) {
            throw e; // Переподнимаем осмысленное исключение
        } catch (Exception e) {
            throw new IllegalArgumentException("Error creating entity from form data", e);
        }
    }




    public static void validateFormData(Class<?> entityClass, Map<String, String> formData) {
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            if (fieldValue == null || fieldValue.isBlank()) {
                throw new IllegalArgumentException("Field " + fieldName + " cannot be empty");
            }

            try {
                Field field = entityClass.getDeclaredField(fieldName);

                if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                    Integer.parseInt(fieldValue);
                } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                    Double.parseDouble(fieldValue);
                } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                    Long.parseLong(fieldValue);
                } else if (field.getType().equals(String.class) && fieldValue.length() > 255) {
                    throw new IllegalArgumentException("Field " + fieldName + " exceeds maximum length of 255 characters");
                } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                    if (!fieldValue.equalsIgnoreCase("true") && !fieldValue.equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException("Field " + fieldName + " must be a boolean value ('true' or 'false')");
                    }
                } else if (field.getType().equals(java.util.Date.class)) {
                    // Assuming a standard date format for validation, e.g., "yyyy-MM-dd"
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateFormat.setLenient(false);
                    Date parsedDate = dateFormat.parse(fieldValue); // Throws ParseException if invalid
                } else if (field.getType().equals(java.sql.Timestamp.class)) {
                    // Assuming a standard timestamp format for validation, e.g., "yyyy-MM-dd HH:mm:ss"
                    SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    timestampFormat.setLenient(false);
                    Date parsedTimestamp = timestampFormat.parse(fieldValue); // Throws ParseException if invalid
                } else if (field.getType().equals(java.math.BigDecimal.class)) {
                    // Validate BigDecimal values
                    new java.math.BigDecimal(fieldValue);
                }
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("Field " + fieldName + " does not exist", e);
            } catch (NumberFormatException | ParseException e) {
                throw new IllegalArgumentException("Invalid value for field " + fieldName, e);
            }
        }
    }

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
                // Для перечислений (enum)
                return Enum.valueOf((Class<Enum>) fieldType, fieldValue.toUpperCase());
            } else {
                // Неизвестный тип данных
                throw new IllegalArgumentException("Unsupported field type: " + fieldType.getName());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid value '" + fieldValue + "' for type " + fieldType.getSimpleName() + ": " + e.getMessage(), e
            );
        }
    }
}

