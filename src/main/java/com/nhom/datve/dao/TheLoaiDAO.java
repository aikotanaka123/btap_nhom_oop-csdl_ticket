package com.nhom.datve.dao;

import com.nhom.datve.model.TheLoai;
import java.util.List;

public interface TheLoaiDAO extends GenericDAO<TheLoai, Integer> {
    List<TheLoai> findByPhim(int maPhim);
}
