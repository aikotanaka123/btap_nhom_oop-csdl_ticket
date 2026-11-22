package com.nhom.datve.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nhom.datve.dao.mock.MockData;
import com.nhom.datve.dao.mock.MockDatVeDAO;
import com.nhom.datve.dao.mock.MockGheDAO;
import com.nhom.datve.dao.mock.MockPhongChieuDAO;
import com.nhom.datve.dao.mock.MockSuatChieuDAO;
import com.nhom.datve.dao.mock.MockVeDAO;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.DatVe;
import com.nhom.datve.model.Ghe;
import com.nhom.datve.model.PhuongThucThanhToan;
import com.nhom.datve.model.SuatChieu;
import com.nhom.datve.model.TrangThaiDatVe;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
