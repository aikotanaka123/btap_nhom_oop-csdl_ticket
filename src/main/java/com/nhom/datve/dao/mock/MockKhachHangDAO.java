package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.KhachHangDAO;
import com.nhom.datve.model.KhachHang;
import java.util.Optional;

/** DAO gia cho KhachHang (du lieu trong MockData). */
public class MockKhachHangDAO extends InMemoryDAO<KhachHang> implements KhachHangDAO {

    public MockKhachHangDAO() {
        super(MockData.KHACH_HANG, KhachHang::getMaKH, KhachHang::setMaKH);
    }

    @Override
    public Optional<KhachHang> findByMaTK(int maTK) {
        return store.values().stream().filter(k -> k.getMaTK() == maTK).findFirst();
    }

    @Override
    public boolean existsBySoDienThoai(String soDienThoai) {
        return store.values().stream().anyMatch(k -> soDienThoai.equals(k.getSoDienThoai()));
    }

    @Override
    public boolean existsByEmail(String email) {
        return store.values().stream().anyMatch(k -> email.equalsIgnoreCase(k.getEmail()));
    }
}
