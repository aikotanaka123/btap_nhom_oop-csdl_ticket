package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.TheLoaiDAO;
import com.nhom.datve.model.Phim;
import com.nhom.datve.model.TheLoai;
import java.util.List;

/** DAO gia cho TheLoai (du lieu trong MockData). */
public class MockTheLoaiDAO extends InMemoryDAO<TheLoai> implements TheLoaiDAO {

    public MockTheLoaiDAO() {
        super(MockData.THE_LOAI, TheLoai::getMaTL, TheLoai::setMaTL);
    }

    @Override
    public List<TheLoai> findByPhim(int maPhim) {
        Phim p = MockData.PHIM.get(maPhim);
        return p == null ? List.of() : List.copyOf(p.getTheLoais());
    }
}
