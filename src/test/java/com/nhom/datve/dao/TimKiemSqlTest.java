package com.nhom.datve.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nhom.datve.model.BoLocTimKiem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Kiem tra cach tao SQL dong cho man hinh tim kiem (khong can CSDL). */
class TimKiemSqlTest {

    @Test
    void khongCoBoLocThiKhongCoThamSo() {
        TimKiemSql.CauLenh cl = TimKiemSql.taoAnToan(BoLocTimKiem.tatCa());
        assertTrue(cl.thamSo().isEmpty());
        assertFalse(cl.sql().contains("?"));
    }

    @Test
    void moiBoLocThemMotDauHoiVaMotThamSoTheoThuTu() {
        LocalDate ngay = LocalDate.of(2026, 11, 28);
        BoLocTimKiem loc = new BoLocTimKiem(" mèo ", 2, ngay, new BigDecimal("80000"));
        TimKiemSql.CauLenh cl = TimKiemSql.taoAnToan(loc);
        assertEquals(4, cl.sql().chars().filter(ch -> ch == '?').count());
        assertEquals(List.of("%mèo%", 2, ngay, new BigDecimal("80000")), cl.thamSo());
    }

    @Test
    void anToanKhongDuaChuoiTanCongVaoSql() {
        BoLocTimKiem loc = new BoLocTimKiem(TimKiemSql.CHUOI_TAN_CONG, null, null, null);
        TimKiemSql.CauLenh cl = TimKiemSql.taoAnToan(loc);
        assertFalse(cl.sql().contains("UNION"));
        assertEquals("%" + TimKiemSql.CHUOI_TAN_CONG.trim() + "%", cl.thamSo().get(0));
    }

    @Test
    void noiChuoiBiChenUnionVaChuThich() {
        BoLocTimKiem loc = new BoLocTimKiem(TimKiemSql.CHUOI_TAN_CONG, null, null, null);
        String sql = TimKiemSql.taoNoiChuoi(loc);
        assertTrue(sql.contains("LIKE N'%' UNION SELECT MaTK, TenDangNhap, MatKhau"));
        // ORDER BY bi bien thanh chu thich phia sau dau --
        assertTrue(sql.indexOf("--") < sql.lastIndexOf("ORDER BY"));
        assertFalse(sql.contains("\n"));
    }

    @Test
    void noiChuoiGhepNgayVaGiaThangVaoSql() {
        BoLocTimKiem loc = new BoLocTimKiem(null, 3, LocalDate.of(2026, 11, 28), new BigDecimal("90000"));
        String sql = TimKiemSql.taoNoiChuoi(loc);
        assertTrue(sql.contains("x.MaTL = 3)"));
        assertTrue(sql.contains("= '2026-11-28'"));
        assertTrue(sql.contains("<= 90000"));
    }
}
