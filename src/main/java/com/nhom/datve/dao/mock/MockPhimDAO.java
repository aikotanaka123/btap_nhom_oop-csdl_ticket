package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.PhimDAO;
import com.nhom.datve.model.Phim;
import java.util.ArrayList;
import java.util.List;

/** DAO gia cho Phim (du lieu trong MockData). */
public class MockPhimDAO extends InMemoryDAO<Phim> implements PhimDAO {

    public MockPhimDAO() {
        super(MockData.PHIM, Phim::getMaPhim, Phim::setMaPhim);
    }

    @Override
    public List<Phim> findDangChieu() {
        return store.values().stream().filter(p -> Phim.DANG_CHIEU.equals(p.getTrangThai())).toList();
    }

    @Override
    public List<Phim> search(String tuKhoa, Integer maTL) {
        return store.values().stream()
                .filter(p -> tuKhoa == null || p.getTenPhim().toLowerCase().contains(tuKhoa.toLowerCase()))
                .filter(p -> maTL == null || p.getTheLoais().stream().anyMatch(t -> t.getMaTL() == maTL))
                .toList();
    }

    @Override
    public void capNhatTheLoai(int maPhim, List<Integer> dsMaTL) {
        Phim p = store.get(maPhim);
        if (p != null) {
            p.setTheLoais(new ArrayList<>(dsMaTL.stream().map(MockData.THE_LOAI::get).toList()));
        }
    }
}
