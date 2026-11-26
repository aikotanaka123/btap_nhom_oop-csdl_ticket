package com.nhom.datve;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * KiemThuTest.java: Toan bo kiem thu JUnit (chay: mvn test).
 * Gom cac lop: TimKiemSqlTest, DatVeServiceTest, OopModelVaTaiKhoanTest, TimKiemVaDangNhapTest
 */

// ==========================================================================================
// TimKiemSqlTest   (truoc day: src/test/java/com/nhom/datve/dao/TimKiemSqlTest.java)
// ==========================================================================================

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


// ==========================================================================================
// DatVeServiceTest   (truoc day: src/test/java/com/nhom/datve/service/DatVeServiceTest.java)
// ==========================================================================================

/** Kiem thu nghiep vu dat/huy ve tren du lieu gia (khong can SQL Server). Chay: mvn test */
class DatVeServiceTest {
    private static final int MA_KH = 1;
    private static final int SUAT_PHONG_1 = 1;   // phim 1, phong 1, ngay mai 10:00, gia 75.000
    private static final PhuongThucThanhToan TT = PhuongThucThanhToan.VI_DIEN_TU;

    private DatVeService service;

    @BeforeEach
    void setUp() {
        MockData.reset();
        service = taoService(Clock.systemDefaultZone());
    }

    private DatVeService taoService(Clock clock) {
        return new DatVeService(new MockSuatChieuDAO(), new MockPhongChieuDAO(), new MockGheDAO(),
                new MockVeDAO(), new MockDatVeDAO(), clock);
    }

    private Ghe ghe(int maSuat, char hang, int so) throws NghiepVuException {
        return service.laySoDoGhe(maSuat).timGhe(hang, so).orElseThrow();
    }

    private static Clock dongHoLuc(LocalDateTime t) {
        return Clock.fixed(t.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    }

    @Test
    void soDoGheDungKichThuoc() throws Exception {
        SoDoGhe sd = service.laySoDoGhe(SUAT_PHONG_1);
        assertEquals(6, sd.getPhong().getSoHang());
        assertEquals(5 * 10 + 5, sd.getDsGhe().size());   // hang F la ghe doi: 5 ghe
        assertEquals(sd.getDsGhe().size(), sd.soGheTrong());
    }

    @Test
    void datVeThanhCongVaTinhGiaTheoLoaiGhe() throws Exception {
        List<Ghe> chon = List.of(ghe(SUAT_PHONG_1, 'A', 1), ghe(SUAT_PHONG_1, 'D', 5));   // thuong + VIP
        DatVe dv = service.datVe(MA_KH, SUAT_PHONG_1, chon, TT);
        assertEquals(TrangThaiDatVe.DA_THANH_TOAN, dv.getTrangThai());
        assertEquals(2, dv.getVes().size());
        assertEquals(new BigDecimal("172500"), dv.getTongTien());   // 75.000 + 75.000 x 1,3
        assertTrue(service.laySoDoGhe(SUAT_PHONG_1).isDaDat(chon.get(0)));
    }

    @Test
    void tongTienTamTinhKhopVoiKhiDat() throws Exception {
        SuatChieu suat = service.laySoDoGhe(SUAT_PHONG_1).getSuatChieu();
        List<Ghe> chon = List.of(ghe(SUAT_PHONG_1, 'F', 1));          // ghe doi x2
        assertEquals(new BigDecimal("150000"), service.tinhTongTien(suat, chon));
    }

    @Test
    void khongChoDatGheDaCoNguoiDat() throws Exception {
        Ghe a1 = ghe(SUAT_PHONG_1, 'A', 1);
        service.datVe(MA_KH, SUAT_PHONG_1, List.of(a1), TT);
        NghiepVuException ex = assertThrows(NghiepVuException.class,
                () -> service.datVe(MA_KH, SUAT_PHONG_1, List.of(a1), TT));
        assertTrue(ex.getMessage().contains("A1"));
    }

    @Test
    void khongChoChonQuaSoGheToiDa() throws Exception {
        List<Ghe> chon = service.laySoDoGhe(SUAT_PHONG_1).getDsGhe().subList(0, DatVeService.SO_GHE_TOI_DA + 1);
        assertThrows(NghiepVuException.class, () -> service.datVe(MA_KH, SUAT_PHONG_1, chon, TT));
    }

    @Test
    void khongChoDanhSachGheRong() {
        assertThrows(NghiepVuException.class, () -> service.datVe(MA_KH, SUAT_PHONG_1, List.of(), TT));
    }

    @Test
    void khongChoGheCuaPhongKhac() throws Exception {
        Ghe ghePhong2 = ghe(4, 'A', 1);   // suat 4 chieu o phong 2
        assertThrows(NghiepVuException.class, () -> service.datVe(MA_KH, SUAT_PHONG_1, List.of(ghePhong2), TT));
    }

    @Test
    void khongChoDatKhiSuatDaBatDau() throws Exception {
        Ghe a1 = ghe(SUAT_PHONG_1, 'A', 1);
        LocalDateTime batDau = service.laySoDoGhe(SUAT_PHONG_1).getSuatChieu().getGioBatDau();
        DatVeService treGio = taoService(dongHoLuc(batDau.plusMinutes(1)));
        assertThrows(NghiepVuException.class, () -> treGio.datVe(MA_KH, SUAT_PHONG_1, List.of(a1), TT));
    }

    @Test
    void huyVeGiaiPhongGhe() throws Exception {
        Ghe a1 = ghe(SUAT_PHONG_1, 'A', 1);
        DatVe dv = service.datVe(MA_KH, SUAT_PHONG_1, List.of(a1), TT);
        service.huyVe(MA_KH, dv.getMaDatVe());
        assertEquals(TrangThaiDatVe.DA_HUY, service.lichSuDatVe(MA_KH).get(0).getTrangThai());
        assertFalse(service.laySoDoGhe(SUAT_PHONG_1).isDaDat(a1));
        service.datVe(MA_KH, SUAT_PHONG_1, List.of(a1), TT);         // dat lai duoc
    }

    @Test
    void khongHuyVeCuaNguoiKhac() throws Exception {
        DatVe dv = service.datVe(MA_KH, SUAT_PHONG_1, List.of(ghe(SUAT_PHONG_1, 'A', 1)), TT);
        assertThrows(NghiepVuException.class, () -> service.huyVe(99, dv.getMaDatVe()));
    }

    @Test
    void khongHuySatGioChieu() throws Exception {
        DatVe dv = service.datVe(MA_KH, SUAT_PHONG_1, List.of(ghe(SUAT_PHONG_1, 'A', 1)), TT);
        LocalDateTime batDau = dv.getVes().get(0).getGioBatDau();
        DatVeService satGio = taoService(dongHoLuc(batDau.minusMinutes(90)));
        assertThrows(NghiepVuException.class, () -> satGio.huyVe(MA_KH, dv.getMaDatVe()));
    }

    @Test
    void khongHuyHaiLan() throws Exception {
        DatVe dv = service.datVe(MA_KH, SUAT_PHONG_1, List.of(ghe(SUAT_PHONG_1, 'A', 1)), TT);
        service.huyVe(MA_KH, dv.getMaDatVe());
        assertThrows(NghiepVuException.class, () -> service.huyVe(MA_KH, dv.getMaDatVe()));
    }
}


// ==========================================================================================
// OopModelVaTaiKhoanTest   (truoc day: src/test/java/com/nhom/datve/service/OopModelVaTaiKhoanTest.java)
// ==========================================================================================

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


// ==========================================================================================
// TimKiemVaDangNhapTest   (truoc day: src/test/java/com/nhom/datve/service/TimKiemVaDangNhapTest.java)
// ==========================================================================================

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
