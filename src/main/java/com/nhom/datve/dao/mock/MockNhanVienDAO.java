package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.NhanVienDAO;
import com.nhom.datve.model.NhanVien;
import java.util.Optional;

/** DAO gia cho NhanVien (du lieu trong MockData). */
public class MockNhanVienDAO extends InMemoryDAO<NhanVien> implements NhanVienDAO {

    public MockNhanVienDAO() {
        super(MockData.NHAN_VIEN, NhanVien::getMaNV, NhanVien::setMaNV);
    }

    @Override
    public Optional<NhanVien> findByMaTK(int maTK) {
        return store.values().stream().filter(n -> n.getMaTK() == maTK).findFirst();
    }
}
