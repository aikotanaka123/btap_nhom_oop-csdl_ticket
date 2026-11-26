package com.nhom.datve;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/*
 * Service.java: Nghiep vu: tai khoan, phim, tim kiem, so do ghe, dat ve.
 * Gom cac lop: DatVeService, PhimService, SoDoGhe, TaiKhoanService, TimKiemService
 */

// ==========================================================================================
// DatVeService   (truoc day: src/main/java/com/nhom/datve/service/DatVeService.java)
// ==========================================================================================

/**
 * Nghiep vu dat ve / huy ve. UI chi goi lop nay, khong goi DAO truc tiep.
 *
 * Kiem tra 2 lop:
 *  1. Tai day (Java): bao loi som, ro rang cho nguoi dung truoc khi gui xuong CSDL.
 *  2. Trong CSDL (sp_DatVe co transaction + chi muc UNIQUE + trigger): chan truong hop 2 nguoi
 *     cung dat 1 ghe gan nhu cung luc - Java khong tu dam bao duoc dieu nay.
 */
class DatVeService {
    public static final int SO_GHE_TOI_DA = 8;
    /** Chi duoc huy truoc gio chieu it nhat 2 tieng. */
    public static final Duration HAN_HUY_TRUOC = Duration.ofHours(2);

    private final SuatChieuDAO suatChieuDAO;
    private final PhongChieuDAO phongChieuDAO;
    private final GheDAO gheDAO;
    private final VeDAO veDAO;
    private final DatVeDAO datVeDAO;
    private final Clock clock;

    /** Dung trong ung dung: lay DAO tu DAOFactory (JDBC hoac mock tuy cau hinh). */
    public DatVeService() {
        this(DAOFactory.suatChieu(), DAOFactory.phongChieu(), DAOFactory.ghe(),
                DAOFactory.ve(), DAOFactory.datVe(), Clock.systemDefaultZone());
    }

    /** Dung trong test: truyen DAO va dong ho tu ngoai vao (dependency injection). */
    public DatVeService(SuatChieuDAO suatChieuDAO, PhongChieuDAO phongChieuDAO, GheDAO gheDAO,
                        VeDAO veDAO, DatVeDAO datVeDAO, Clock clock) {
        this.suatChieuDAO = suatChieuDAO;
        this.phongChieuDAO = phongChieuDAO;
        this.gheDAO = gheDAO;
        this.veDAO = veDAO;
        this.datVeDAO = datVeDAO;
        this.clock = clock;
    }

    /** Lay du lieu cho man hinh so do ghe. */
    public SoDoGhe laySoDoGhe(int maSuat) throws NghiepVuException {
        SuatChieu suat = laySuat(maSuat);
        PhongChieu phong = phongChieuDAO.findById(suat.getMaPhong())
                .orElseThrow(() -> new NghiepVuException("Không tìm thấy phòng chiếu của suất này."));
        return new SoDoGhe(suat, phong, gheDAO.findByPhong(phong.getMaPhong()), veDAO.findMaGheDaDat(maSuat));
    }

    /** Tong tien tam tinh de hien truoc khi khach bam Thanh toan. */
    public BigDecimal tinhTongTien(SuatChieu suat, List<Ghe> gheChon) {
        return gheChon.stream().map(g -> g.tinhGiaVe(suat.getGiaCoBan()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Dat ve va thanh toan (gia lap).
     * @return lan dat vua tao, kem danh sach ve
     */
    public DatVe datVe(int maKH, int maSuat, List<Ghe> gheChon, PhuongThucThanhToan phuongThuc)
            throws NghiepVuException {
        if (gheChon == null || gheChon.isEmpty()) {
            throw new NghiepVuException("Vui lòng chọn ít nhất 1 ghế.");
        }
        if (gheChon.size() > SO_GHE_TOI_DA) {
            throw new NghiepVuException("Mỗi lần đặt tối đa " + SO_GHE_TOI_DA + " ghế.");
        }
        if (phuongThuc == null) {
            throw new NghiepVuException("Vui lòng chọn phương thức thanh toán.");
        }
        SuatChieu suat = laySuat(maSuat);
        if (!suat.getGioBatDau().isAfter(LocalDateTime.now(clock))) {
            throw new NghiepVuException("Suất chiếu đã bắt đầu, không thể đặt vé.");
        }
        Set<Integer> daChon = new HashSet<>();
        Set<Integer> daDat = veDAO.findMaGheDaDat(maSuat);
        for (Ghe g : gheChon) {
            if (g.getMaPhong() != suat.getMaPhong()) {
                throw new NghiepVuException("Ghế " + g.getTenGhe() + " không thuộc phòng của suất chiếu này.");
            }
            if (!daChon.add(g.getMaGhe())) {
                throw new NghiepVuException("Ghế " + g.getTenGhe() + " bị chọn trùng.");
            }
            if (daDat.contains(g.getMaGhe())) {
                throw new NghiepVuException("Ghế " + g.getTenGhe() + " đã có người đặt.");
            }
        }
        List<Integer> dsMaGhe = gheChon.stream().map(Ghe::getMaGhe).toList();
        int maDatVe = datVeDAO.datVe(maKH, maSuat, dsMaGhe, phuongThuc);
        return datVeDAO.findById(maDatVe)
                .orElseThrow(() -> new NghiepVuException("Đặt vé xong nhưng không đọc lại được thông tin vé."));
    }

    /** Huy 1 lan dat ve cua chinh khach hang do. */
    public void huyVe(int maKH, int maDatVe) throws NghiepVuException {
        DatVe dv = datVeDAO.findById(maDatVe)
                .orElseThrow(() -> new NghiepVuException("Không tìm thấy vé cần hủy."));
        if (dv.getMaKH() != maKH) {
            throw new NghiepVuException("Bạn không thể hủy vé của người khác.");
        }
        if (dv.getTrangThai() != TrangThaiDatVe.DA_THANH_TOAN) {
            throw new NghiepVuException("Vé đang ở trạng thái \"" + dv.getTrangThai().getMoTa() + "\", không thể hủy.");
        }
        LocalDateTime gioChieuSomNhat = dv.getVes().stream().map(Ve::getGioBatDau)
                .filter(Objects::nonNull).min(LocalDateTime::compareTo)
                .orElseThrow(() -> new NghiepVuException("Lần đặt này không có vé."));
        if (LocalDateTime.now(clock).plus(HAN_HUY_TRUOC).isAfter(gioChieuSomNhat)) {
            throw new NghiepVuException("Chỉ được hủy vé trước giờ chiếu ít nhất "
                    + HAN_HUY_TRUOC.toHours() + " tiếng.");
        }
        if (!datVeDAO.huy(maDatVe)) {
            throw new NghiepVuException("Hủy vé không thành công, vui lòng thử lại.");
        }
    }

    public List<DatVe> lichSuDatVe(int maKH) {
        return datVeDAO.findByKhachHang(maKH);
    }

    private SuatChieu laySuat(int maSuat) throws NghiepVuException {
        return suatChieuDAO.findById(maSuat)
                .orElseThrow(() -> new NghiepVuException("Suất chiếu không tồn tại."));
    }
}


// ==========================================================================================
// PhimService   (truoc day: src/main/java/com/nhom/datve/service/PhimService.java)
// ==========================================================================================

/**
 * Nghiep vu tra cuu phim, the loai va suat chieu cho man hinh khach hang.
 */
class PhimService {
    private final PhimDAO phimDAO;
    private final TheLoaiDAO theLoaiDAO;
    private final SuatChieuDAO suatChieuDAO;

    public PhimService() {
        this(DAOFactory.phim(), DAOFactory.theLoai(), DAOFactory.suatChieu());
    }

    public PhimService(PhimDAO phimDAO, TheLoaiDAO theLoaiDAO, SuatChieuDAO suatChieuDAO) {
        this.phimDAO = phimDAO;
        this.theLoaiDAO = theLoaiDAO;
        this.suatChieuDAO = suatChieuDAO;
    }

    public List<TheLoai> layTatCaTheLoai() {
        return theLoaiDAO.findAll().stream()
                .sorted(Comparator.comparing(TheLoai::getTenTL))
                .toList();
    }

    public List<Phim> layPhimDangChieu(String tuKhoa, Integer maTL) {
        String kw = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        if (kw == null && maTL == null) {
            return phimDAO.findDangChieu();
        }
        return phimDAO.search(kw, maTL).stream()
                .filter(p -> Phim.DANG_CHIEU.equals(p.getTrangThai()))
                .toList();
    }

    public Phim layPhimTheoMa(int maPhim) throws NghiepVuException {
        return phimDAO.findById(maPhim)
                .orElseThrow(() -> new NghiepVuException("Không tìm thấy phim mã #" + maPhim + "."));
    }

    public List<SuatChieu> laySuatSapChieu(int maPhim) {
        return suatChieuDAO.findSapChieu(maPhim, LocalDateTime.now());
    }

    public List<SuatChieu> laySuatTheoNgay(int maPhim, LocalDate ngay) {
        return suatChieuDAO.findByPhimVaNgay(maPhim, ngay);
    }
}


// ==========================================================================================
// SoDoGhe   (truoc day: src/main/java/com/nhom/datve/service/SoDoGhe.java)
// ==========================================================================================

/**
 * Du lieu de ve so do chon ghe cho 1 suat chieu: phong (so hang, so cot), danh sach ghe va
 * cac ghe da co nguoi dat. Man hinh so do ghe chi can goi DatVeService.laySoDoGhe(maSuat).
 */
class SoDoGhe {
    private final SuatChieu suatChieu;
    private final PhongChieu phong;
    private final List<Ghe> dsGhe;
    private final Set<Integer> maGheDaDat;

    public SoDoGhe(SuatChieu suatChieu, PhongChieu phong, List<Ghe> dsGhe, Set<Integer> maGheDaDat) {
        this.suatChieu = suatChieu;
        this.phong = phong;
        this.dsGhe = List.copyOf(dsGhe);
        this.maGheDaDat = Set.copyOf(maGheDaDat);
    }

    public boolean isDaDat(Ghe ghe) { return maGheDaDat.contains(ghe.getMaGhe()); }

    /** Tim ghe theo vi tri, vd timGhe('C', 7). Rong neu o do la loi di. */
    public Optional<Ghe> timGhe(char hang, int soGhe) {
        return dsGhe.stream().filter(g -> g.getHang() == hang && g.getSoGhe() == soGhe).findFirst();
    }

    public int soGheTrong() { return dsGhe.size() - maGheDaDat.size(); }

    public SuatChieu getSuatChieu() { return suatChieu; }
    public PhongChieu getPhong() { return phong; }
    public List<Ghe> getDsGhe() { return dsGhe; }
    public Set<Integer> getMaGheDaDat() { return maGheDaDat; }
}


// ==========================================================================================
// TaiKhoanService   (truoc day: src/main/java/com/nhom/datve/service/TaiKhoanService.java)
// ==========================================================================================

/**
 * Dang nhap va dang ky tai khoan.
 * LoginFrame goi dangNhap() roi mo new MainFrame(phien).
 * DangKyDialog goi dangKy(...) de tao TaiKhoan + KhachHang moi.
 */
class TaiKhoanService {
    private final TaiKhoanDAO taiKhoanDAO;
    private final KhachHangDAO khachHangDAO;
    private final NhanVienDAO nhanVienDAO;

    public TaiKhoanService() {
        this(DAOFactory.taiKhoan(), DAOFactory.khachHang(), DAOFactory.nhanVien());
    }

    public TaiKhoanService(TaiKhoanDAO taiKhoanDAO, KhachHangDAO khachHangDAO, NhanVienDAO nhanVienDAO) {
        this.taiKhoanDAO = taiKhoanDAO;
        this.khachHangDAO = khachHangDAO;
        this.nhanVienDAO = nhanVienDAO;
    }

    public PhienDangNhap dangNhap(String tenDangNhap, String matKhau) throws NghiepVuException {
        if (tenDangNhap == null || tenDangNhap.isBlank() || matKhau == null || matKhau.isEmpty()) {
            throw new NghiepVuException("Vui lòng nhập tên đăng nhập và mật khẩu.");
        }
        TaiKhoan tk = taiKhoanDAO.findByTenDangNhap(tenDangNhap.trim())
                .filter(t -> PasswordUtil.khop(matKhau, t.getMatKhau()))
                .orElseThrow(() -> new NghiepVuException("Sai tên đăng nhập hoặc mật khẩu."));
        if (!tk.isTrangThai()) {
            throw new NghiepVuException("Tài khoản đã bị khóa.");
        }
        if (tk.getVaiTro() == VaiTro.KHACHHANG) {
            return new PhienDangNhap(tk, khachHangDAO.findByMaTK(tk.getMaTK())
                    .orElseThrow(() -> new NghiepVuException("Tài khoản chưa có hồ sơ khách hàng.")), null);
        }
        return new PhienDangNhap(tk, null, nhanVienDAO.findByMaTK(tk.getMaTK())
                .orElseThrow(() -> new NghiepVuException("Tài khoản chưa có hồ sơ nhân viên.")));
    }

    /**
     * Dang ky tai khoan khach hang moi (Task 28 & 29 - Trung).
     */
    public PhienDangNhap dangKy(String tenDangNhap, String matKhau, String xacNhanMatKhau,
                                String hoTen, String soDienThoai, String email) throws NghiepVuException {
        String ten = Validator.kiemTraTenDangNhap(tenDangNhap);
        String mk = Validator.kiemTraMatKhau(matKhau, xacNhanMatKhau);
        String tenKH = Validator.kiemTraHoTen(hoTen);
        String sdt = Validator.kiemTraSoDienThoai(soDienThoai);
        String mail = Validator.kiemTraEmail(email);

        if (taiKhoanDAO.existsByTenDangNhap(ten)) {
            throw new NghiepVuException("Tên đăng nhập \"" + ten + "\" đã được sử dụng.");
        }
        if (khachHangDAO.existsBySoDienThoai(sdt)) {
            throw new NghiepVuException("Số điện thoại \"" + sdt + "\" đã được đăng ký.");
        }
        if (mail != null && khachHangDAO.existsByEmail(mail)) {
            throw new NghiepVuException("Email \"" + mail + "\" đã được đăng ký.");
        }

        TaiKhoan tk = new TaiKhoan(0, ten, PasswordUtil.hash(mk), VaiTro.KHACHHANG);
        Integer maTK = taiKhoanDAO.insert(tk);
        tk.setMaTK(maTK);

        KhachHang kh = new KhachHang(0, maTK, tenKH, sdt, mail);
        Integer maKH = khachHangDAO.insert(kh);
        kh.setMaKH(maKH);

        return new PhienDangNhap(tk, kh, null);
    }
}


// ==========================================================================================
// TimKiemService   (truoc day: src/main/java/com/nhom/datve/service/TimKiemService.java)
// ==========================================================================================

/** Nghiep vu man hinh tim kiem phim. */
class TimKiemService {
    public static final int DO_DAI_TU_KHOA_TOI_DA = 100;

    private final TimKiemDAO timKiemDAO;
    private final TheLoaiDAO theLoaiDAO;

    public TimKiemService() {
        this(DAOFactory.timKiem(), DAOFactory.theLoai());
    }

    public TimKiemService(TimKiemDAO timKiemDAO, TheLoaiDAO theLoaiDAO) {
        this.timKiemDAO = timKiemDAO;
        this.theLoaiDAO = theLoaiDAO;
    }

    public List<TheLoai> dsTheLoai() {
        return theLoaiDAO.findAll().stream().sorted(Comparator.comparing(TheLoai::getTenTL)).toList();
    }

    /**
     * @param anToan false chi dung khi demo chuong 10 (noi chuoi, bi SQL injection)
     */
    public KetQuaTimKiem tim(BoLocTimKiem loc, boolean anToan) throws NghiepVuException {
        if (loc.coTuKhoa() && loc.tuKhoa().length() > DO_DAI_TU_KHOA_TOI_DA) {
            throw new NghiepVuException("Từ khóa tối đa " + DO_DAI_TU_KHOA_TOI_DA + " ký tự.");
        }
        if (loc.giaToiDa() != null && loc.giaToiDa().compareTo(BigDecimal.ZERO) < 0) {
            throw new NghiepVuException("Giá tối đa không được âm.");
        }
        return timKiemDAO.timSuatChieu(loc, anToan);
    }
}
