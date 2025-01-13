package com.example.indproj.services;

import com.example.indproj.utils.TableUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToOne;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.Map;

@Service
public class TableService {
    private final EntityManager entityManager;

    public TableService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void deleteEntityById(Class<?> entityClass, Long id) {
        Object entity = entityManager.find(entityClass, id);
        if (entity != null) {
            entityManager.remove(entity);
        } else {
            throw new IllegalArgumentException("Entity not found for ID: " + id);
        }
    }

    @Transactional
    public void saveEntity(Object entity) {
        entityManager.persist(entity);
    }

    public Object findById(Class<?> entityClass, Long id) {
        return entityManager.find(entityClass, id);
    }

    @Transactional
    public void updateEntityFromForm(Class<?> entityClass, Long id, Map<String, String> formData) {
        Object entity = entityManager.find(entityClass, id);
        if (entity == null) {
            throw new IllegalArgumentException("Entity with ID " + id + " not found");
        }

        for (Map.Entry<String, String> entry : formData.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            try {
                Field field = entityClass.getDeclaredField(fieldName);
                field.setAccessible(true);

                // Проверяем, является ли поле ссылкой на другую сущность
                if (field.isAnnotationPresent(ManyToOne.class)) {
                    // Извлекаем класс связанной сущности
                    Class<?> relatedEntityClass = field.getType();
                    Object relatedEntity = entityManager.find(relatedEntityClass, Long.valueOf(fieldValue));

                    if (relatedEntity == null) {
                        throw new IllegalArgumentException("Related entity with ID " + fieldValue + " not found for field " + fieldName);
                    }

                    field.set(entity, relatedEntity);
                } else {
                    // Преобразование и установка значения для обычных полей
                    Object convertedValue = TableUtils.convertData(field.getType(), fieldValue);
                    field.set(entity, convertedValue);
                }
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException("Field " + fieldName + " not found in " + entityClass.getSimpleName());
            } catch (Exception e) {
                throw new IllegalArgumentException("Error converting value '" + fieldValue + "' for field '" + fieldName + "': " + e.getMessage(), e);
            }
        }

        entityManager.merge(entity);
    }

}


