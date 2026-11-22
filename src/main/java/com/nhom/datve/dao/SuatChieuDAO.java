package com.nhom.datve.dao;

import com.nhom.datve.model.SuatChieu;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SuatChieuDAO extends GenericDAO<SuatChieu, Integer> {
    /** Cac suat cua 1 phim trong 1 ngay (kem tenPhim, tenPhong). */
    List<SuatChieu> findByPhimVaNgay(int maPhim, LocalDate ngay);

    /** Cac suat tu thoi diem 'tu' tro di cua 1 phim. */
    List<SuatChieu> findSapChieu(int maPhim, LocalDateTime tu);

    /**
     * Kiem tra phong da co suat khac chong lan khoang [batDau, ketThuc) chua.
     * boQuaMaSuat: ma suat dang sua (null khi them moi).
     */
    boolean biTrungLich(int maPhong, LocalDateTime batDau, LocalDateTime ketThuc, Integer boQuaMaSuat);
}
