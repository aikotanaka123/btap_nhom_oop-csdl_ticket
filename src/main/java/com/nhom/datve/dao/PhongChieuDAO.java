package com.nhom.datve.dao;

import com.nhom.datve.model.PhongChieu;
import java.util.List;

public interface PhongChieuDAO extends GenericDAO<PhongChieu, Integer> {
    List<PhongChieu> findDangHoatDong();
}
