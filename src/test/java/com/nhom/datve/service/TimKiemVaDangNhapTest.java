package com.nhom.datve.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nhom.datve.dao.TimKiemSql;
import com.nhom.datve.dao.mock.MockData;
import com.nhom.datve.dao.mock.MockKhachHangDAO;
import com.nhom.datve.dao.mock.MockNhanVienDAO;
import com.nhom.datve.dao.mock.MockTaiKhoanDAO;
import com.nhom.datve.dao.mock.MockTheLoaiDAO;
import com.nhom.datve.dao.mock.MockTimKiemDAO;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.BoLocTimKiem;
import com.nhom.datve.model.KetQuaTimKiem;
import com.nhom.datve.util.PhienDangNhap;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** TimKiemService va TaiKhoanService tren du lieu gia. */
class TimKiemVaDangNhapTest {
    private TimKiemService timKiem;
    private TaiKhoanService taiKhoan;

    @BeforeEach
    void setUp() {
        MockData.reset();
        timKiem = new TimKiemService(new MockTimKiemDAO(), new MockTheLoaiDAO());
        taiKhoan = new TaiKhoanService(new MockTaiKhoanDAO(), new MockKhachHangDAO(), new MockNhanVienDAO());
    }

    @Test
    void timTatCaTraVeMoiSuatSapChieu() throws Exception {
        KetQuaTimKiem kq = timKiem.tim(BoLocTimKiem.tatCa(), true);
        assertEquals(9, kq.dsSuat().size());
        assertTrue(kq.anToan());
    }

    @Test
    void locTheoTenVaGia() throws Exception {
        KetQuaTimKiem kq = timKiem.tim(new BoLocTimKiem("mèo", null, null, new BigDecimal("80000")), true);
        assertEquals(2, kq.dsSuat().size());      // 10:00 va 14:30 gia 75.000; 19:45 gia 90.000 bi loai
        assertEquals("Hoạt hình, Hài", kq.dsSuat().get(0).getTheLoai());
    }

    @Test
    void cheDoNoiChuoiTraVeCauSqlBiChen() throws Exception {
        KetQuaTimKiem kq = timKiem.tim(new BoLocTimKiem(TimKiemSql.CHUOI_TAN_CONG, null, null, null), false);
        assertFalse(kq.anToan());
        assertTrue(kq.sql().contains("UNION SELECT"));
    }

    @Test
    void giaAmBiTuChoi() {
        assertThrows(NghiepVuException.class,
                () -> timKiem.tim(new BoLocTimKiem(null, null, null, new BigDecimal("-1")), true));
    }

    @Test
    void dangNhapDungVaiTro() throws Exception {
        PhienDangNhap kh = taiKhoan.dangNhap("khach", "123456");
        assertFalse(kh.isAdmin());
        assertEquals(1, kh.getMaKH());
        assertTrue(taiKhoan.dangNhap("admin", "admin123").isAdmin());
    }

    @Test
    void dangNhapSaiMatKhau() {
        assertThrows(NghiepVuException.class, () -> taiKhoan.dangNhap("khach", "sai"));
        assertThrows(NghiepVuException.class, () -> taiKhoan.dangNhap("", ""));
    }
}
