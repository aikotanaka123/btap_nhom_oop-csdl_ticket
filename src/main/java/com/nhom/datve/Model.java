package com.nhom.datve;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Model.java: Cac lop du lieu (model OOP): nguoi dung, phim, phong, ghe, suat chieu, ve, dat ve...
 * Gom cac lop: VaiTro, NguoiDung, TaiKhoan, KhachHang, NhanVien, TheLoai, Phim, PhongChieu, LoaiGhe, Ghe, GheThuong, GheVIP, GheDoi, SuatChieu, BoLocTimKiem, DatVe, KetQuaTimKiem, PhuongThucThanhToan, ThanhToan, TrangThaiDatVe, Ve
 */

// ==========================================================================================
// VaiTro   (truoc day: src/main/java/com/nhom/datve/model/VaiTro.java)
// ==========================================================================================

/** Vai tro cua tai khoan, khop voi cot TaiKhoan.VaiTro. */
enum VaiTro {
    KHACHHANG, NHANVIEN
}


// ==========================================================================================
// NguoiDung   (truoc day: src/main/java/com/nhom/datve/model/NguoiDung.java)
// ==========================================================================================

/**
 * Lop truu tuong NguoiDung the hien 4 tinh chat OOP:
 * - Truu tuong: lop abstract voi phuong thuc abstract getVaiTro(), getMoTaVaiTro().
 * - Dong goi: cac thuoc tinh private, truy cap qua getter/setter co kiem tra hop le.
 * - Ke thua: KhachHang va NhanVien cung ke thua NguoiDung.
 * - Da hinh: getVaiTro() va getMoTaVaiTro() tra ket qua khac nhau tuy lop con.
 */
abstract class NguoiDung {
    private int maTK;
    private String hoTen;
    private String soDienThoai;
    private String email;

    protected NguoiDung() { }

    protected NguoiDung(int maTK, String hoTen, String soDienThoai, String email) {
        this.maTK = maTK;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
    }

    public abstract VaiTro getVaiTro();

    public abstract String getMoTaVaiTro();

    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return hoTen + " (" + getMoTaVaiTro() + ")";
    }
}


// ==========================================================================================
// TaiKhoan   (truoc day: src/main/java/com/nhom/datve/model/TaiKhoan.java)
// ==========================================================================================

/** Bang TaiKhoan. matKhau luu chuoi bam SHA-256, khong luu mat khau goc. */
class TaiKhoan {
    private int maTK;
    private String tenDangNhap;
    private String matKhau;
    private VaiTro vaiTro;
    private boolean trangThai = true;
    private LocalDateTime ngayTao;

    public TaiKhoan() { }

    public TaiKhoan(int maTK, String tenDangNhap, String matKhau, VaiTro vaiTro) {
        this.maTK = maTK;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.ngayTao = LocalDateTime.now();
    }

    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public VaiTro getVaiTro() { return vaiTro; }
    public void setVaiTro(VaiTro vaiTro) { this.vaiTro = vaiTro; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}


// ==========================================================================================
// KhachHang   (truoc day: src/main/java/com/nhom/datve/model/KhachHang.java)
// ==========================================================================================

/**
 * Bang KhachHang ke thua lop truu tuong {@link NguoiDung}.
 */
class KhachHang extends NguoiDung {
    private int maKH;
    private LocalDate ngaySinh;

    public KhachHang() {
        super();
    }

    public KhachHang(int maKH, int maTK, String hoTen, String soDienThoai, String email) {
        super(maTK, hoTen, soDienThoai, email);
        this.maKH = maKH;
    }

    @Override
    public VaiTro getVaiTro() {
        return VaiTro.KHACHHANG;
    }

    @Override
    public String getMoTaVaiTro() {
        return "Khách hàng";
    }

    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }
}


// ==========================================================================================
// NhanVien   (truoc day: src/main/java/com/nhom/datve/model/NhanVien.java)
// ==========================================================================================

/**
 * Bang NhanVien (tai khoan admin) ke thua lop truu tuong {@link NguoiDung}.
 */
class NhanVien extends NguoiDung {
    private int maNV;
    private String chucVu;

    public NhanVien() {
        super();
    }

    public NhanVien(int maNV, int maTK, String hoTen, String chucVu) {
        super(maTK, hoTen, null, null);
        this.maNV = maNV;
        this.chucVu = chucVu;
    }

    @Override
    public VaiTro getVaiTro() {
        return VaiTro.NHANVIEN;
    }

    @Override
    public String getMoTaVaiTro() {
        return chucVu != null && !chucVu.isBlank() ? "Nhân viên - " + chucVu : "Nhân viên";
    }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }
}


// ==========================================================================================
// TheLoai   (truoc day: src/main/java/com/nhom/datve/model/TheLoai.java)
// ==========================================================================================

/** Bang TheLoai. */
class TheLoai {
    private int maTL;
    private String tenTL;

    public TheLoai() { }

    public TheLoai(int maTL, String tenTL) {
        this.maTL = maTL;
        this.tenTL = tenTL;
    }

    public int getMaTL() { return maTL; }
    public void setMaTL(int maTL) { this.maTL = maTL; }
    public String getTenTL() { return tenTL; }
    public void setTenTL(String tenTL) { this.tenTL = tenTL; }

    @Override
    public String toString() { return tenTL; }
}


// ==========================================================================================
// Phim   (truoc day: src/main/java/com/nhom/datve/model/Phim.java)
// ==========================================================================================

/** Bang Phim; danh sach the loai lay tu bang trung gian Phim_TheLoai. */
class Phim {
    public static final String DANG_CHIEU = "DANG_CHIEU";
    public static final String SAP_CHIEU = "SAP_CHIEU";
    public static final String NGUNG_CHIEU = "NGUNG_CHIEU";

    private int maPhim;
    private String tenPhim;
    private int thoiLuong;          // phut
    private String daoDien;
    private LocalDate ngayKhoiChieu;
    private String doTuoi;          // P, T13, T16, T18
    private String moTa;
    private String poster;          // duong dan anh
    private String trangThai = DANG_CHIEU;
    private List<TheLoai> theLoais = new ArrayList<>();

    public Phim() { }

    public Phim(int maPhim, String tenPhim, int thoiLuong, String daoDien, String doTuoi) {
        this.maPhim = maPhim;
        this.tenPhim = tenPhim;
        this.thoiLuong = thoiLuong;
        this.daoDien = daoDien;
        this.doTuoi = doTuoi;
    }

    public int getMaPhim() { return maPhim; }
    public void setMaPhim(int maPhim) { this.maPhim = maPhim; }
    public String getTenPhim() { return tenPhim; }
    public void setTenPhim(String tenPhim) { this.tenPhim = tenPhim; }
    public int getThoiLuong() { return thoiLuong; }
    public void setThoiLuong(int thoiLuong) { this.thoiLuong = thoiLuong; }
    public String getDaoDien() { return daoDien; }
    public void setDaoDien(String daoDien) { this.daoDien = daoDien; }
    public LocalDate getNgayKhoiChieu() { return ngayKhoiChieu; }
    public void setNgayKhoiChieu(LocalDate ngayKhoiChieu) { this.ngayKhoiChieu = ngayKhoiChieu; }
    public String getDoTuoi() { return doTuoi; }
    public void setDoTuoi(String doTuoi) { this.doTuoi = doTuoi; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public List<TheLoai> getTheLoais() { return theLoais; }
    public void setTheLoais(List<TheLoai> theLoais) { this.theLoais = theLoais; }

    @Override
    public String toString() { return tenPhim; }
}


// ==========================================================================================
// PhongChieu   (truoc day: src/main/java/com/nhom/datve/model/PhongChieu.java)
// ==========================================================================================

/** Bang PhongChieu. soHang x soCot la kich thuoc luoi de ve so do ghe. */
class PhongChieu {
    private int maPhong;
    private String tenPhong;
    private int soHang;
    private int soCot;
    private boolean hoatDong = true;

    public PhongChieu() { }

    public PhongChieu(int maPhong, String tenPhong, int soHang, int soCot) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.soHang = soHang;
        this.soCot = soCot;
    }

    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    public int getSoHang() { return soHang; }
    public void setSoHang(int soHang) { this.soHang = soHang; }
    public int getSoCot() { return soCot; }
    public void setSoCot(int soCot) { this.soCot = soCot; }
    public boolean isHoatDong() { return hoatDong; }
    public void setHoatDong(boolean hoatDong) { this.hoatDong = hoatDong; }

    @Override
    public String toString() { return tenPhong; }
}


// ==========================================================================================
// LoaiGhe   (truoc day: src/main/java/com/nhom/datve/model/LoaiGhe.java)
// ==========================================================================================

/** Bang LoaiGhe: Thuong (1.0), VIP (1.3), Doi (2.0)... */
class LoaiGhe {
    private int maLoaiGhe;
    private String tenLoai;
    private BigDecimal heSoGia;

    public LoaiGhe() { }

    public LoaiGhe(int maLoaiGhe, String tenLoai, BigDecimal heSoGia) {
        this.maLoaiGhe = maLoaiGhe;
        this.tenLoai = tenLoai;
        this.heSoGia = heSoGia;
    }

    public int getMaLoaiGhe() { return maLoaiGhe; }
    public void setMaLoaiGhe(int maLoaiGhe) { this.maLoaiGhe = maLoaiGhe; }
    public String getTenLoai() { return tenLoai; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }
    public BigDecimal getHeSoGia() { return heSoGia; }
    public void setHeSoGia(BigDecimal heSoGia) { this.heSoGia = heSoGia; }

    @Override
    public String toString() { return tenLoai; }
}


// ==========================================================================================
// Ghe   (truoc day: src/main/java/com/nhom/datve/model/Ghe.java)
// ==========================================================================================

/**
 * Bang Ghe. Moi ghe thuoc 1 phong, co 1 loai ghe.
 * Co cac lop con {@link GheThuong}, {@link GheVIP}, {@link GheDoi} ke thua va override
 * {@link #tinhGiaVe(BigDecimal)} de the hien tinh ke thua va da hinh.
 */
class Ghe {
    private int maGhe;
    private int maPhong;
    private LoaiGhe loaiGhe;
    private char hang;      // A, B, C...
    private int soGhe;      // 1, 2, 3...

    public Ghe() { }

    public Ghe(int maGhe, int maPhong, LoaiGhe loaiGhe, char hang, int soGhe) {
        this.maGhe = maGhe;
        this.maPhong = maPhong;
        this.loaiGhe = loaiGhe;
        this.hang = hang;
        this.soGhe = soGhe;
    }

    /**
     * Factory method tao doi tuong Ghe dung lop con (GheThuong / GheVIP / GheDoi)
     * dua theo LoaiGhe de the hien tinh da hinh.
     */
    public static Ghe taoGhe(int maGhe, int maPhong, LoaiGhe loaiGhe, char hang, int soGhe) {
        if (loaiGhe != null && loaiGhe.getTenLoai() != null) {
            String ten = loaiGhe.getTenLoai().trim().toLowerCase();
            if (ten.contains("vip")) {
                return new GheVIP(maGhe, maPhong, loaiGhe, hang, soGhe);
            }
            if (ten.contains("đôi") || ten.contains("doi")) {
                return new GheDoi(maGhe, maPhong, loaiGhe, hang, soGhe);
            }
        }
        return new GheThuong(maGhe, maPhong, loaiGhe, hang, soGhe);
    }

    /** Ten hien thi tren so do ghe, vd "C7". */
    public String getTenGhe() { return hang + String.valueOf(soGhe); }

    /**
     * Gia ve = gia co ban cua suat chieu x he so cua loai ghe, lam tron den dong.
     * Cac lop con GheThuong, GheVIP, GheDoi override phuong thuc nay.
     */
    public BigDecimal tinhGiaVe(BigDecimal giaCoBan) {
        if (giaCoBan == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal heSo = loaiGhe != null && loaiGhe.getHeSoGia() != null
                ? loaiGhe.getHeSoGia()
                : getHeSoMacDinh();
        return giaCoBan.multiply(heSo).setScale(0, RoundingMode.HALF_UP);
    }

    /** He so gia mac dinh cua loai ghe. */
    protected BigDecimal getHeSoMacDinh() {
        return BigDecimal.ONE;
    }

    public int getMaGhe() { return maGhe; }
    public void setMaGhe(int maGhe) { this.maGhe = maGhe; }
    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }
    public LoaiGhe getLoaiGhe() { return loaiGhe; }
    public void setLoaiGhe(LoaiGhe loaiGhe) { this.loaiGhe = loaiGhe; }
    public char getHang() { return hang; }
    public void setHang(char hang) { this.hang = hang; }
    public int getSoGhe() { return soGhe; }
    public void setSoGhe(int soGhe) { this.soGhe = soGhe; }

    @Override
    public String toString() { return getTenGhe(); }
}


// ==========================================================================================
// GheThuong   (truoc day: src/main/java/com/nhom/datve/model/GheThuong.java)
// ==========================================================================================

/**
 * Ghe thuong - he so gia 1.00 (100% gia co ban).
 * Ke thua {@link Ghe} va override {@link #tinhGiaVe(BigDecimal)}.
 */
class GheThuong extends Ghe {
    public static final BigDecimal HE_SO_THUONG = new BigDecimal("1.00");

    public GheThuong() {
        super();
    }

    public GheThuong(int maGhe, int maPhong, LoaiGhe loaiGhe, char hang, int soGhe) {
        super(maGhe, maPhong,
              loaiGhe != null ? loaiGhe : new LoaiGhe(1, "Thường", HE_SO_THUONG),
              hang, soGhe);
    }

    @Override
    protected BigDecimal getHeSoMacDinh() {
        return HE_SO_THUONG;
    }

    @Override
    public BigDecimal tinhGiaVe(BigDecimal giaCoBan) {
        if (giaCoBan == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal heSo = (getLoaiGhe() != null && getLoaiGhe().getHeSoGia() != null)
                ? getLoaiGhe().getHeSoGia()
                : HE_SO_THUONG;
        return giaCoBan.multiply(heSo).setScale(0, RoundingMode.HALF_UP);
    }
}


// ==========================================================================================
// GheVIP   (truoc day: src/main/java/com/nhom/datve/model/GheVIP.java)
// ==========================================================================================

/**
 * Ghe VIP - he so gia 1.30 (130% gia co ban).
 * Ke thua {@link Ghe} va override {@link #tinhGiaVe(BigDecimal)}.
 */
class GheVIP extends Ghe {
    public static final BigDecimal HE_SO_VIP = new BigDecimal("1.30");

    public GheVIP() {
        super();
    }

    public GheVIP(int maGhe, int maPhong, LoaiGhe loaiGhe, char hang, int soGhe) {
        super(maGhe, maPhong,
              loaiGhe != null ? loaiGhe : new LoaiGhe(2, "VIP", HE_SO_VIP),
              hang, soGhe);
    }

    @Override
    protected BigDecimal getHeSoMacDinh() {
        return HE_SO_VIP;
    }

    @Override
    public BigDecimal tinhGiaVe(BigDecimal giaCoBan) {
        if (giaCoBan == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal heSo = (getLoaiGhe() != null && getLoaiGhe().getHeSoGia() != null)
                ? getLoaiGhe().getHeSoGia()
                : HE_SO_VIP;
        return giaCoBan.multiply(heSo).setScale(0, RoundingMode.HALF_UP);
    }
}


// ==========================================================================================
// GheDoi   (truoc day: src/main/java/com/nhom/datve/model/GheDoi.java)
// ==========================================================================================

/**
 * Ghe Doi (Sweetbox 2 nguoi) - he so gia 2.00 (200% gia co ban).
 * Ke thua {@link Ghe} va override {@link #tinhGiaVe(BigDecimal)}.
 */
class GheDoi extends Ghe {
    public static final BigDecimal HE_SO_DOI = new BigDecimal("2.00");

    public GheDoi() {
        super();
    }

    public GheDoi(int maGhe, int maPhong, LoaiGhe loaiGhe, char hang, int soGhe) {
        super(maGhe, maPhong,
              loaiGhe != null ? loaiGhe : new LoaiGhe(3, "Đôi", HE_SO_DOI),
              hang, soGhe);
    }

    @Override
    protected BigDecimal getHeSoMacDinh() {
        return HE_SO_DOI;
    }

    @Override
    public BigDecimal tinhGiaVe(BigDecimal giaCoBan) {
        if (giaCoBan == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal heSo = (getLoaiGhe() != null && getLoaiGhe().getHeSoGia() != null)
                ? getLoaiGhe().getHeSoGia()
                : HE_SO_DOI;
        return giaCoBan.multiply(heSo).setScale(0, RoundingMode.HALF_UP);
    }
}


// ==========================================================================================
// SuatChieu   (truoc day: src/main/java/com/nhom/datve/model/SuatChieu.java)
// ==========================================================================================

/** Bang SuatChieu. tenPhim, tenPhong chi de hien thi (lay khi JOIN), khong luu trong bang. */
class SuatChieu {
    private int maSuat;
    private int maPhim;
    private int maPhong;
    private LocalDateTime gioBatDau;
    private LocalDateTime gioKetThuc;
    private BigDecimal giaCoBan;
    private String tenPhim;
    private String tenPhong;
    private String theLoai;   // vd "Hanh dong, Hai" - chi de hien thi

    public SuatChieu() { }

    public SuatChieu(int maSuat, int maPhim, int maPhong, LocalDateTime gioBatDau,
                     LocalDateTime gioKetThuc, BigDecimal giaCoBan) {
        this.maSuat = maSuat;
        this.maPhim = maPhim;
        this.maPhong = maPhong;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.giaCoBan = giaCoBan;
    }

    public int getMaSuat() { return maSuat; }
    public void setMaSuat(int maSuat) { this.maSuat = maSuat; }
    public int getMaPhim() { return maPhim; }
    public void setMaPhim(int maPhim) { this.maPhim = maPhim; }
    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }
    public LocalDateTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalDateTime gioBatDau) { this.gioBatDau = gioBatDau; }
    public LocalDateTime getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(LocalDateTime gioKetThuc) { this.gioKetThuc = gioKetThuc; }
    public BigDecimal getGiaCoBan() { return giaCoBan; }
    public void setGiaCoBan(BigDecimal giaCoBan) { this.giaCoBan = giaCoBan; }
    public String getTenPhim() { return tenPhim; }
    public void setTenPhim(String tenPhim) { this.tenPhim = tenPhim; }
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    public String getTheLoai() { return theLoai; }
    public void setTheLoai(String theLoai) { this.theLoai = theLoai; }
}


// ==========================================================================================
// BoLocTimKiem   (truoc day: src/main/java/com/nhom/datve/model/BoLocTimKiem.java)
// ==========================================================================================

/** Cac dieu kien loc tren man hinh tim kiem phim. Truong nao null = khong loc theo truong do. */
record BoLocTimKiem(String tuKhoa, Integer maTL, LocalDate ngayChieu, BigDecimal giaToiDa) {

    public static BoLocTimKiem tatCa() {
        return new BoLocTimKiem(null, null, null, null);
    }

    public boolean coTuKhoa() {
        return tuKhoa != null && !tuKhoa.isBlank();
    }
}


// ==========================================================================================
// DatVe   (truoc day: src/main/java/com/nhom/datve/model/DatVe.java)
// ==========================================================================================

/**
 * Bang DatVe: 1 lan dat gom nhieu ve.
 * Tong tien KHONG luu trong bang (la thuoc tinh dan xuat) ma tinh tu danh sach ve.
 */
class DatVe {
    private int maDatVe;
    private int maKH;
    private LocalDateTime ngayDat;
    private TrangThaiDatVe trangThai = TrangThaiDatVe.CHO_THANH_TOAN;
    private PhuongThucThanhToan phuongThuc;
    private List<Ve> ves = new ArrayList<>();

    public DatVe() { }

    public DatVe(int maDatVe, int maKH, LocalDateTime ngayDat, TrangThaiDatVe trangThai) {
        this.maDatVe = maDatVe;
        this.maKH = maKH;
        this.ngayDat = ngayDat;
        this.trangThai = trangThai;
    }

    public BigDecimal getTongTien() {
        return ves.stream().map(Ve::getGiaVe).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getMaDatVe() { return maDatVe; }
    public void setMaDatVe(int maDatVe) { this.maDatVe = maDatVe; }
    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }
    public LocalDateTime getNgayDat() { return ngayDat; }
    public void setNgayDat(LocalDateTime ngayDat) { this.ngayDat = ngayDat; }
    public TrangThaiDatVe getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiDatVe trangThai) { this.trangThai = trangThai; }
    public PhuongThucThanhToan getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(PhuongThucThanhToan phuongThuc) { this.phuongThuc = phuongThuc; }
    public List<Ve> getVes() { return ves; }
    public void setVes(List<Ve> ves) { this.ves = ves; }
}


// ==========================================================================================
// KetQuaTimKiem   (truoc day: src/main/java/com/nhom/datve/model/KetQuaTimKiem.java)
// ==========================================================================================

/**
 * Ket qua tim kiem kem cau SQL thuc su da gui len CSDL, de man hinh tim kiem hien ra phuc vu
 * demo Dynamic SQL (chuong 10).
 *
 * @param dsSuat  cac suat chieu tim thay (tenPhim, theLoai, tenPhong da dien san)
 * @param sql     cau lenh SQL da chay
 * @param thamSo  gia tri cac dau ? theo thu tu (rong neu dung noi chuoi)
 * @param anToan  true = PreparedStatement, false = noi chuoi
 * @param ghiChu  thong bao them (loi cu phap SQL, che do du lieu gia...), null neu khong co
 */
record KetQuaTimKiem(List<SuatChieu> dsSuat, String sql, List<Object> thamSo, boolean anToan, String ghiChu) {
}


// ==========================================================================================
// PhuongThucThanhToan   (truoc day: src/main/java/com/nhom/datve/model/PhuongThucThanhToan.java)
// ==========================================================================================

/** Khop voi cot ThanhToan.PhuongThuc (thanh toan gia lap). */
enum PhuongThucThanhToan {
    TIEN_MAT("Tiền mặt tại quầy"),
    THE("Thẻ ngân hàng"),
    VI_DIEN_TU("Ví điện tử");

    private final String moTa;

    PhuongThucThanhToan(String moTa) { this.moTa = moTa; }

    public String getMoTa() { return moTa; }

    @Override
    public String toString() { return moTa; }
}


// ==========================================================================================
// ThanhToan   (truoc day: src/main/java/com/nhom/datve/model/ThanhToan.java)
// ==========================================================================================

/** Bang ThanhToan (gia lap): moi lan dat ve co toi da 1 thanh toan. */
class ThanhToan {
    public static final String THANH_CONG = "THANH_CONG";
    public static final String HOAN_TIEN = "HOAN_TIEN";

    private int maTT;
    private int maDatVe;
    private BigDecimal soTien;
    private PhuongThucThanhToan phuongThuc;
    private LocalDateTime thoiGian;
    private String trangThai = THANH_CONG;

    public ThanhToan() { }

    public int getMaTT() { return maTT; }
    public void setMaTT(int maTT) { this.maTT = maTT; }
    public int getMaDatVe() { return maDatVe; }
    public void setMaDatVe(int maDatVe) { this.maDatVe = maDatVe; }
    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }
    public PhuongThucThanhToan getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(PhuongThucThanhToan phuongThuc) { this.phuongThuc = phuongThuc; }
    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}


// ==========================================================================================
// TrangThaiDatVe   (truoc day: src/main/java/com/nhom/datve/model/TrangThaiDatVe.java)
// ==========================================================================================

/** Khop voi cot DatVe.TrangThai. */
enum TrangThaiDatVe {
    CHO_THANH_TOAN("Chờ thanh toán"),
    DA_THANH_TOAN("Đã thanh toán"),
    DA_HUY("Đã hủy");

    private final String moTa;

    TrangThaiDatVe(String moTa) { this.moTa = moTa; }

    public String getMoTa() { return moTa; }
}


// ==========================================================================================
// Ve   (truoc day: src/main/java/com/nhom/datve/model/Ve.java)
// ==========================================================================================

/** Bang Ve: 1 ve = 1 ghe cua 1 suat chieu. Cac truong ten* va gioBatDau chi de hien thi. */
class Ve {
    public static final String HIEU_LUC = "HIEU_LUC";
    public static final String DA_HUY = "DA_HUY";

    private int maVe;
    private int maDatVe;
    private int maSuat;
    private int maGhe;
    private BigDecimal giaVe;
    private String trangThai = HIEU_LUC;
    private String tenGhe;
    private String tenPhim;
    private String tenPhong;
    private LocalDateTime gioBatDau;

    public Ve() { }

    public Ve(int maVe, int maDatVe, int maSuat, int maGhe, BigDecimal giaVe) {
        this.maVe = maVe;
        this.maDatVe = maDatVe;
        this.maSuat = maSuat;
        this.maGhe = maGhe;
        this.giaVe = giaVe;
    }

    public int getMaVe() { return maVe; }
    public void setMaVe(int maVe) { this.maVe = maVe; }
    public int getMaDatVe() { return maDatVe; }
    public void setMaDatVe(int maDatVe) { this.maDatVe = maDatVe; }
    public int getMaSuat() { return maSuat; }
    public void setMaSuat(int maSuat) { this.maSuat = maSuat; }
    public int getMaGhe() { return maGhe; }
    public void setMaGhe(int maGhe) { this.maGhe = maGhe; }
    public BigDecimal getGiaVe() { return giaVe; }
    public void setGiaVe(BigDecimal giaVe) { this.giaVe = giaVe; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getTenGhe() { return tenGhe; }
    public void setTenGhe(String tenGhe) { this.tenGhe = tenGhe; }
    public String getTenPhim() { return tenPhim; }
    public void setTenPhim(String tenPhim) { this.tenPhim = tenPhim; }
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    public LocalDateTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalDateTime gioBatDau) { this.gioBatDau = gioBatDau; }
}
