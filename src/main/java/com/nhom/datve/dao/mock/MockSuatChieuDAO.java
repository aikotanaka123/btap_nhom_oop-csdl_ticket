package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.SuatChieuDAO;
import com.nhom.datve.model.SuatChieu;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/** DAO gia cho SuatChieu (du lieu trong MockData). */
public class MockSuatChieuDAO extends InMemoryDAO<SuatChieu> implements SuatChieuDAO {

    public MockSuatChieuDAO() {
        super(MockData.SUAT, SuatChieu::getMaSuat, SuatChieu::setMaSuat);
    }

    @Override
    public List<SuatChieu> findByPhimVaNgay(int maPhim, LocalDate ngay) {
        return store.values().stream()
                .filter(s -> s.getMaPhim() == maPhim && s.getGioBatDau().toLocalDate().equals(ngay))
                .sorted(Comparator.comparing(SuatChieu::getGioBatDau)).toList();
    }

    @Override
    public List<SuatChieu> findSapChieu(int maPhim, LocalDateTime tu) {
        return store.values().stream()
                .filter(s -> s.getMaPhim() == maPhim && s.getGioBatDau().isAfter(tu))
                .sorted(Comparator.comparing(SuatChieu::getGioBatDau)).toList();
    }

    @Override
    public boolean biTrungLich(int maPhong, LocalDateTime batDau, LocalDateTime ketThuc, Integer boQuaMaSuat) {
        return store.values().stream()
                .filter(s -> s.getMaPhong() == maPhong)
                .filter(s -> boQuaMaSuat == null || s.getMaSuat() != boQuaMaSuat)
                .anyMatch(s -> s.getGioBatDau().isBefore(ketThuc) && batDau.isBefore(s.getGioKetThuc()));
    }
}
