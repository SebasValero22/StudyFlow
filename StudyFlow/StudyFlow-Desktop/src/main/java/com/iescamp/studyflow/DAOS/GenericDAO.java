package com.iescamp.studyflow.DAOS;

import java.util.List;

/**
 * Generic Interface for CRUD operations.
 * @param <T> The Entity class (e.g., User, Task)
 * @param <ID> The type of the Primary Key (e.g., Integer)
 */


public interface GenericDAO<T, ID> {
    void insert(T entity) throws Exception;
    T findById(ID id) throws Exception;
    List<T> findAll() throws Exception;
    void update(T entity) throws Exception;
    void delete(ID id) throws Exception;
}