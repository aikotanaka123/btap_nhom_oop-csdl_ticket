package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.PhongChieuDAO;
import com.nhom.datve.model.PhongChieu;
import java.util.List;

/** DAO gia cho PhongChieu (du lieu trong MockData). */
public class MockPhongChieuDAO extends InMemoryDAO<PhongChieu> implements PhongChieuDAO {

    public MockPhongChieuDAO() {
        super(MockData.PHONG, PhongChieu::getMaPhong, PhongChieu::setMaPhong);
    }

    @Override
    public List<PhongChieu> findDangHoatDong() {
        return store.values().stream().filter(PhongChieu::isHoatDong).toList();
    }
}
