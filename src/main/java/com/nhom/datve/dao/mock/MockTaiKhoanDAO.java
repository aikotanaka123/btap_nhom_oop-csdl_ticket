package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.TaiKhoanDAO;
import com.nhom.datve.model.TaiKhoan;
import java.util.Optional;

/** DAO gia cho TaiKhoan (du lieu trong MockData). */
public class MockTaiKhoanDAO extends InMemoryDAO<TaiKhoan> implements TaiKhoanDAO {

    public MockTaiKhoanDAO() {
        super(MockData.TAI_KHOAN, TaiKhoan::getMaTK, TaiKhoan::setMaTK);
    }

    @Override
    public Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap) {
        return store.values().stream().filter(t -> t.getTenDangNhap().equalsIgnoreCase(tenDangNhap)).findFirst();
    }

    @Override
    public boolean existsByTenDangNhap(String tenDangNhap) {
        return findByTenDangNhap(tenDangNhap).isPresent();
    }
}
