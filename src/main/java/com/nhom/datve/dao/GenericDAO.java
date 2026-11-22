package com.nhom.datve.dao;

import java.util.List;
import java.util.Optional;

/**
 * Cac thao tac CRUD chung cho moi bang. T = lop model, K = kieu khoa chinh.
 * Cac interface DAO cu the ke thua interface nay va bo sung truy van rieng.
 */
public interface GenericDAO<T, K> {
    List<T> findAll();

    Optional<T> findById(K id);

    /** Them moi, tra ve khoa chinh vua sinh (cot IDENTITY). */
    K insert(T entity);

    boolean update(T entity);

    boolean delete(K id);
}
