package com.example.indproj.services;

import com.example.indproj.utils.TableUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToOne;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Сервис для работы с таблицами сущностей
 * Предоставляет методы для CRUD операций с использованием рефлексии
 */
@Service
public class TableService {
    private final EntityManager entityManager;

    /**
     * Конструктор сервиса
     * 
     * @param entityManager менеджер сущностей для работы с базой данных
     */
    public TableService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Удаляет сущность по указанному идентификатору
     * 
     * @param entityClass класс сущности для удаления
     * @param id идентификатор удаляемой сущности
     */
    @Transactional
    public void deleteEntityById(Class<?> entityClass, Long id) {
        Object entity = entityManager.find(entityClass, id);
        if (entity != null) {
            entityManager.remove(entity);
        } else {
            System.out.println("Ошибка: Сущность с ID " + id + " не найдена");
        }
    }

    /**
     * Сохраняет новую сущность в базе данных
     * 
     * @param entity сущность для сохранения
     */
    @Transactional
    public void saveEntity(Object entity) {
        entityManager.persist(entity);
    }

    /**
     * Находит сущность по идентификатору
     * 
     * @param entityClass класс сущности для поиска
     * @param id идентификатор искомой сущности
     * @return найденная сущность или null если не найдена
     */
    public Object findById(Class<?> entityClass, Long id) {
        return entityManager.find(entityClass, id);
    }

    /**
     * Обновляет сущность данными из формы
     * 
     * @param entityClass класс сущности для обновления
     * @param id идентификатор обновляемой сущности
     * @param formData данные формы для обновления
     */
    @Transactional
    public void updateEntityFromForm(Class<?> entityClass, Long id, Map<String, String> formData) {
        Object entity = entityManager.find(entityClass, id);
        if (entity == null) {
            System.out.println("Ошибка: Сущность с ID " + id + " не найдена");
            return;
        }

        for (Map.Entry<String, String> entry : formData.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            try {
                Field field = entityClass.getDeclaredField(fieldName);
                field.setAccessible(true);

                if (field.isAnnotationPresent(ManyToOne.class)) {
                    Class<?> relatedEntityClass = field.getType();
                    Object relatedEntity = entityManager.find(relatedEntityClass, 
                            Long.valueOf(fieldValue));

                    if (relatedEntity == null) {
                        System.out.println("Ошибка: Связанная сущность с ID " + fieldValue 
                                + " не найдена для поля " + fieldName);
                        return;
                    }

                    field.set(entity, relatedEntity);
                } else {
                    Object convertedValue = TableUtils.convertData(field.getType(), fieldValue);
                    field.set(entity, convertedValue);
                }
            } catch (NoSuchFieldException e) {
                System.out.println("Ошибка: Поле " + fieldName + " не найдено в " 
                        + entityClass.getSimpleName());
                return;
            } catch (Exception e) {
                System.out.println("Ошибка преобразования значения '" + fieldValue 
                        + "' для поля '" + fieldName + "': " + e.getMessage());
                return;
            }
        }

        entityManager.merge(entity);
    }
}


