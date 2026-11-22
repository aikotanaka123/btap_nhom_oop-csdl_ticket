package com.nhom.datve.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cac dieu kien loc tren man hinh tim kiem phim. Truong nao null = khong loc theo truong do. */
public record BoLocTimKiem(String tuKhoa, Integer maTL, LocalDate ngayChieu, BigDecimal giaToiDa) {

    public static BoLocTimKiem tatCa() {
        return new BoLocTimKiem(null, null, null, null);
    }

    public boolean coTuKhoa() {
        return tuKhoa != null && !tuKhoa.isBlank();
    }
}
