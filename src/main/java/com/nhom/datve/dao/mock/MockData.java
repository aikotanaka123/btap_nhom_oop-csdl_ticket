package com.nhom.datve.dao.mock;

import com.nhom.datve.model.DatVe;
import com.nhom.datve.model.Ghe;
import com.nhom.datve.model.KhachHang;
import com.nhom.datve.model.LoaiGhe;
import com.nhom.datve.model.NhanVien;
import com.nhom.datve.model.Phim;
import com.nhom.datve.model.PhongChieu;
import com.nhom.datve.model.SuatChieu;
import com.nhom.datve.model.TaiKhoan;
import com.nhom.datve.model.TheLoai;
import com.nhom.datve.model.ThanhToan;
import com.nhom.datve.model.VaiTro;
import com.nhom.datve.model.Ve;
import com.nhom.datve.util.PasswordUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Du lieu gia dung chung cho cac Mock DAO (chi de code va test giao dien).
 * Tai khoan mau: khach / 123456 (khach hang) va admin / admin123 (nhan vien).
 */
public final class MockData {
    static final Map<Integer, TaiKhoan> TAI_KHOAN = new LinkedHashMap<>();
    static final Map<Integer, KhachHang> KHACH_HANG = new LinkedHashMap<>();
    static final Map<Integer, NhanVien> NHAN_VIEN = new LinkedHashMap<>();
    static final Map<Integer, TheLoai> THE_LOAI = new LinkedHashMap<>();
    static final Map<Integer, Phim> PHIM = new LinkedHashMap<>();
    static final Map<Integer, PhongChieu> PHONG = new LinkedHashMap<>();
    static final Map<Integer, LoaiGhe> LOAI_GHE = new LinkedHashMap<>();
    static final Map<Integer, Ghe> GHE = new LinkedHashMap<>();
    static final Map<Integer, SuatChieu> SUAT = new LinkedHashMap<>();
    static final Map<Integer, DatVe> DAT_VE = new LinkedHashMap<>();
    static final Map<Integer, Ve> VE = new LinkedHashMap<>();
    static final Map<Integer, ThanhToan> THANH_TOAN = new LinkedHashMap<>();

    static {
        reset();
    }

    private MockData() { }

    /** Nap lai du lieu mau (goi trong test de moi test bat dau sach). */
    public static synchronized void reset() {
        for (Map<?, ?> m : List.of(TAI_KHOAN, KHACH_HANG, NHAN_VIEN, THE_LOAI, PHIM, PHONG,
                LOAI_GHE, GHE, SUAT, DAT_VE, VE, THANH_TOAN)) {
            m.clear();
        }
        TAI_KHOAN.put(1, new TaiKhoan(1, "admin", PasswordUtil.hash("admin123"), VaiTro.NHANVIEN));
        TAI_KHOAN.put(2, new TaiKhoan(2, "khach", PasswordUtil.hash("123456"), VaiTro.KHACHHANG));
        NHAN_VIEN.put(1, new NhanVien(1, 1, "Quản Trị Viên", "Quản lý"));
        KHACH_HANG.put(1, new KhachHang(1, 2, "Nguyễn Văn An", "0901234567", "an@example.com"));

        String[] tl = {"Hành động", "Hoạt hình", "Kinh dị", "Tình cảm", "Hài"};
        for (int i = 0; i < tl.length; i++) {
            THE_LOAI.put(i + 1, new TheLoai(i + 1, tl[i]));
        }
        themPhim(1, "Biệt Đội Bầu Trời", 128, "Trần Minh", "T13", 1);
        themPhim(2, "Chú Mèo Phiêu Lưu", 95, "Lê Hạnh", "P", 2, 5);
        themPhim(3, "Ngôi Nhà Cuối Phố", 110, "Phạm Quân", "T18", 3);

        LOAI_GHE.put(1, new LoaiGhe(1, "Thường", new BigDecimal("1.00")));
        LOAI_GHE.put(2, new LoaiGhe(2, "VIP", new BigDecimal("1.30")));
        LOAI_GHE.put(3, new LoaiGhe(3, "Đôi", new BigDecimal("2.00")));
        themPhong(1, "Phòng 1", 6, 10);
        themPhong(2, "Phòng 2", 5, 8);

        LocalDate maiNay = LocalDate.now().plusDays(1);
        int ma = 1;
        for (int p = 1; p <= 3; p++) {
            for (LocalTime gio : List.of(LocalTime.of(10, 0), LocalTime.of(14, 30), LocalTime.of(19, 45))) {
                LocalDateTime bd = LocalDateTime.of(maiNay, gio);
                Phim phim = PHIM.get(p);
                int phong = p == 2 ? 2 : 1;
                if (p == 3) {
                    bd = bd.plusHours(4);  // tranh trung lich phong 1 voi phim 1
                }
                SuatChieu s = new SuatChieu(ma, p, phong, bd, bd.plusMinutes(phim.getThoiLuong() + 15),
                        new BigDecimal(gio.getHour() >= 17 ? "90000" : "75000"));
                s.setTenPhim(phim.getTenPhim());
                s.setTenPhong(PHONG.get(phong).getTenPhong());
                SUAT.put(ma++, s);
            }
        }
    }

    private static void themPhim(int ma, String ten, int phut, String daoDien, String doTuoi, int... maTL) {
        Phim p = new Phim(ma, ten, phut, daoDien, doTuoi);
        p.setNgayKhoiChieu(LocalDate.now().minusDays(7));
        p.setMoTa("Mô tả ngắn của phim " + ten + ".");
        for (int t : maTL) {
            p.getTheLoais().add(THE_LOAI.get(t));
        }
        PHIM.put(ma, p);
    }

    /** Hang cuoi la ghe Doi, 2 hang ke cuoi la VIP, con lai ghe Thuong. */
    private static void themPhong(int ma, String ten, int soHang, int soCot) {
        PHONG.put(ma, new PhongChieu(ma, ten, soHang, soCot));
        int maGhe = GHE.size() + 1;
        for (int h = 0; h < soHang; h++) {
            int loai = h == soHang - 1 ? 3 : (h >= soHang - 3 ? 2 : 1);
            int soGheHang = loai == 3 ? soCot / 2 : soCot;
            for (int c = 1; c <= soGheHang; c++) {
                GHE.put(maGhe, Ghe.taoGhe(maGhe, ma, LOAI_GHE.get(loai), (char) ('A' + h), c));
                maGhe++;
            }
        }
    }
}
