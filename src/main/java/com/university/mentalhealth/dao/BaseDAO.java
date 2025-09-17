package com.university.mentalhealth.dao;

import java.util.List;
import java.util.Optional;

public interface BaseDAO<T> {
    Optional<T> findById(int id);
    List<T> findAll();
    boolean save(T entity);
    boolean update(T entity);
    boolean delete(int id);
}