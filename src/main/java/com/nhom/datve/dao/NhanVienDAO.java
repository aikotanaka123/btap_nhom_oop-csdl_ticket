package com.nhom.datve.dao;

import com.nhom.datve.model.NhanVien;
import java.util.Optional;

public interface NhanVienDAO extends GenericDAO<NhanVien, Integer> {
    Optional<NhanVien> findByMaTK(int maTK);
}
