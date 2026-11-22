package com.nhom.datve.dao;

import com.nhom.datve.model.KhachHang;
import java.util.Optional;

public interface KhachHangDAO extends GenericDAO<KhachHang, Integer> {
    Optional<KhachHang> findByMaTK(int maTK);

    boolean existsBySoDienThoai(String soDienThoai);

    boolean existsByEmail(String email);
}
