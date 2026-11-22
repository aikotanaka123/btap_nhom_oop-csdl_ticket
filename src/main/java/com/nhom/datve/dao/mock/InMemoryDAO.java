package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.GenericDAO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/** Lop cha cho cac DAO gia: luu doi tuong trong Map, tu tang khoa nhu cot IDENTITY. */
abstract class InMemoryDAO<T> implements GenericDAO<T, Integer> {
    protected final Map<Integer, T> store;
    private final Function<T, Integer> getId;
    private final IdSetter<T> setId;

    interface IdSetter<T> {
        void set(T t, int id);
    }

    InMemoryDAO(Map<Integer, T> store, Function<T, Integer> getId, IdSetter<T> setId) {
        this.store = store;
        this.getId = getId;
        this.setId = setId;
    }

    @Override
    public List<T> findAll() {
        synchronized (store) {
            return new ArrayList<>(store.values());
        }
    }

    @Override
    public Optional<T> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Integer insert(T entity) {
        synchronized (store) {
            int id = store.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            setId.set(entity, id);
            store.put(id, entity);
            return id;
        }
    }

    @Override
    public boolean update(T entity) {
        Integer id = getId.apply(entity);
        if (!store.containsKey(id)) {
            return false;
        }
        store.put(id, entity);
        return true;
    }

    @Override
    public boolean delete(Integer id) {
        return store.remove(id) != null;
    }
}
