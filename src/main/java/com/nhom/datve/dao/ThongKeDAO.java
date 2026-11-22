package com.nhom.datve.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Thong ke doanh thu cho admin (cai dat goi ham fn_DoanhThu). */
public interface ThongKeDAO {
    /** Tong doanh thu trong khoang ngay [tu, den]. */
    BigDecimal tongDoanhThu(LocalDate tu, LocalDate den);

    /** Doanh thu theo tung phim, giu thu tu giam dan (LinkedHashMap). */
    Map<String, BigDecimal> doanhThuTheoPhim(LocalDate tu, LocalDate den);

    /** Doanh thu theo tung ngay. */
    Map<LocalDate, BigDecimal> doanhThuTheoNgay(LocalDate tu, LocalDate den);
}
