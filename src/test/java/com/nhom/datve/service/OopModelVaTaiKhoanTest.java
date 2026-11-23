package com.nhom.datve.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nhom.datve.dao.mock.MockData;
import com.nhom.datve.dao.mock.MockKhachHangDAO;
import com.nhom.datve.dao.mock.MockNhanVienDAO;
import com.nhom.datve.dao.mock.MockPhimDAO;
import com.nhom.datve.dao.mock.MockSuatChieuDAO;
import com.nhom.datve.dao.mock.MockTaiKhoanDAO;
import com.nhom.datve.dao.mock.MockTheLoaiDAO;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.Ghe;
import com.nhom.datve.model.GheDoi;
import com.nhom.datve.model.GheThuong;
import com.nhom.datve.model.GheVIP;
import com.nhom.datve.model.KhachHang;
import com.nhom.datve.model.LoaiGhe;
import com.nhom.datve.model.NguoiDung;
import com.nhom.datve.model.NhanVien;
import com.nhom.datve.model.VaiTro;
import com.nhom.datve.util.PhienDangNhap;
import com.nhom.datve.util.Validator;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Kiem thu phan OOP model, Validator, dang ky/dang nhap va PhimService.
 */
class OopModelVaTaiKhoanTest {

    private TaiKhoanService taiKhoanService;
    private PhimService phimService;

    @BeforeEach
    void setUp() {
        MockData.reset();
        taiKhoanService = new TaiKhoanService(new MockTaiKhoanDAO(), new MockKhachHangDAO(), new MockNhanVienDAO());
        phimService = new PhimService(new MockPhimDAO(), new MockTheLoaiDAO(), new MockSuatChieuDAO());
    }

    @Test
    void daHinhNguoiDungKhachHangVaNhanVien() {
        NguoiDung kh = new KhachHang(1, 2, "Nguyễn Văn An", "0901234567", "an@example.com");
        NguoiDung nv = new NhanVien(1, 1, "Quản Trị Viên", "Quản lý");

        assertEquals(VaiTro.KHACHHANG, kh.getVaiTro());
        assertEquals("Khách hàng", kh.getMoTaVaiTro());
        assertEquals(VaiTro.NHANVIEN, nv.getVaiTro());
        assertTrue(nv.getMoTaVaiTro().contains("Quản lý"));
    }

    @Test
    void daHinhTinhGiaVeTheoLoaiGhe() {
        BigDecimal giaCoBan = new BigDecimal("75000");
        Ghe thuong = Ghe.taoGhe(1, 1, new LoaiGhe(1, "Thường", new BigDecimal("1.00")), 'A', 1);
        Ghe vip = Ghe.taoGhe(2, 1, new LoaiGhe(2, "VIP", new BigDecimal("1.30")), 'D', 1);
        Ghe doi = Ghe.taoGhe(3, 1, new LoaiGhe(3, "Đôi", new BigDecimal("2.00")), 'F', 1);

        assertInstanceOf(GheThuong.class, thuong);
        assertInstanceOf(GheVIP.class, vip);
        assertInstanceOf(GheDoi.class, doi);

        assertEquals(new BigDecimal("75000"), thuong.tinhGiaVe(giaCoBan));
        assertEquals(new BigDecimal("97500"), vip.tinhGiaVe(giaCoBan));
        assertEquals(new BigDecimal("150000"), doi.tinhGiaVe(giaCoBan));
    }

    @Test
    void dangKyThanhCongVaDangNhapLaiDuoc() throws Exception {
        PhienDangNhap phienDK = taiKhoanService.dangKy(
                "trung_n24", "matkhau123", "matkhau123",
                "Đỗ Quang Trung", "0987654321", "trung@ptit.edu.vn");
        assertNotNull(phienDK);
        assertEquals("Đỗ Quang Trung", phienDK.getTenHienThi());

        PhienDangNhap phienDN = taiKhoanService.dangNhap("trung_n24", "matkhau123");
        assertEquals(phienDK.getMaKH(), phienDN.getMaKH());
    }

    @Test
    void dangKyBaoLoiKhiTrungTenHoacSaiDuLieu() {
        assertThrows(NghiepVuException.class, () -> taiKhoanService.dangKy(
                "khach", "123456", "123456", "Trùng Tên", "0911222333", null));
        assertThrows(NghiepVuException.class, () -> taiKhoanService.dangKy(
                "user_moi", "123", "123", "MK Ngắn", "0911222333", null));
        assertThrows(NghiepVuException.class, () -> taiKhoanService.dangKy(
                "user_moi", "123456", "654321", "Sai Xác Nhận", "0911222333", null));
        assertThrows(NghiepVuException.class, () -> Validator.kiemTraSoDienThoai("12345"));
        assertThrows(NghiepVuException.class, () -> Validator.kiemTraEmail("khong-phai-email"));
    }

    @Test
    void phimServiceLayPhimVaSuatChieu() throws Exception {
        assertEquals(3, phimService.layPhimDangChieu(null, null).size());
        assertEquals("Biệt Đội Bầu Trời", phimService.layPhimTheoMa(1).getTenPhim());
        assertEquals(3, phimService.laySuatSapChieu(1).size());
    }
}
