package com.nhom.datve;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * MockDAO.java: Ban DAO gia (du lieu trong bo nho) - dung khi chay che do MOCK, khong can SQL Server.
 * Gom cac lop: MockData, InMemoryDAO, MockDatVeDAO, MockGheDAO, MockKhachHangDAO, MockLoaiGheDAO, MockNhanVienDAO, MockPhimDAO, MockPhongChieuDAO, MockSuatChieuDAO, MockTaiKhoanDAO, MockTheLoaiDAO, MockThongKeDAO, MockTimKiemDAO, MockVeDAO
 */

// ==========================================================================================
// MockData   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockData.java)
// ==========================================================================================

/**
 * Du lieu gia dung chung cho cac Mock DAO (chi de code va test giao dien).
 * Tai khoan mau: khach / 123456 (khach hang) va admin / admin123 (nhan vien).
 */
final class MockData {
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


// ==========================================================================================
// InMemoryDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/InMemoryDAO.java)
// ==========================================================================================

/** Lop cha cho cac DAO gia: luu doi tuong trong Map, tu tang khoa nhu cot IDENTITY. */
abstract class InMemoryDAO<T> implements GenericDAO<T, Integer> {
    protected final Map<Integer, T> store;
    private final Function<T, Integer> getId;
    private final IdSetter<T> setId;

    interface IdSetter<T> {
        void set(T t, int id);
    }

    InMemoryDAO(Map<Integer, T> store, Function<T, Integer> getId, IdSetter<T> setId) {
        this.store = store;
        this.getId = getId;
        this.setId = setId;
    }

    @Override
    public List<T> findAll() {
        synchronized (store) {
            return new ArrayList<>(store.values());
        }
    }

    @Override
    public Optional<T> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Integer insert(T entity) {
        synchronized (store) {
            int id = store.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            setId.set(entity, id);
            store.put(id, entity);
            return id;
        }
    }

    @Override
    public boolean update(T entity) {
        Integer id = getId.apply(entity);
        if (!store.containsKey(id)) {
            return false;
        }
        store.put(id, entity);
        return true;
    }

    @Override
    public boolean delete(Integer id) {
        return store.remove(id) != null;
    }
}


// ==========================================================================================
// MockDatVeDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockDatVeDAO.java)
// ==========================================================================================

/** DAO gia cho DatVe: mo phong lai dung nhung gi sp_DatVe lam (kiem tra ghe trong roi tao ve). */
class MockDatVeDAO implements DatVeDAO {

    @Override
    public int datVe(int maKH, int maSuat, List<Integer> dsMaGhe, PhuongThucThanhToan phuongThuc)
            throws NghiepVuException {
        synchronized (MockData.class) {
            SuatChieu suat = MockData.SUAT.get(maSuat);
            if (suat == null) {
                throw new NghiepVuException("Suất chiếu không tồn tại.");
            }
            for (int maGhe : dsMaGhe) {
                boolean daDat = MockData.VE.values().stream().anyMatch(v -> v.getMaSuat() == maSuat
                        && v.getMaGhe() == maGhe && Ve.HIEU_LUC.equals(v.getTrangThai()));
                if (daDat) {
                    throw new NghiepVuException("Ghế " + MockData.GHE.get(maGhe).getTenGhe()
                            + " vừa có người khác đặt. Vui lòng chọn ghế khác.");
                }
            }
            int maDatVe = MockData.DAT_VE.size() + 1;
            DatVe dv = new DatVe(maDatVe, maKH, LocalDateTime.now(), TrangThaiDatVe.DA_THANH_TOAN);
            dv.setPhuongThuc(phuongThuc);
            MockData.DAT_VE.put(maDatVe, dv);
            for (int maGhe : dsMaGhe) {
                Ghe g = MockData.GHE.get(maGhe);
                int maVe = MockData.VE.size() + 1;
                Ve ve = new Ve(maVe, maDatVe, maSuat, maGhe, g.tinhGiaVe(suat.getGiaCoBan()));
                ve.setTenGhe(g.getTenGhe());
                ve.setTenPhim(suat.getTenPhim());
                ve.setTenPhong(suat.getTenPhong());
                ve.setGioBatDau(suat.getGioBatDau());
                MockData.VE.put(maVe, ve);
            }
            ThanhToan tt = new ThanhToan();
            tt.setMaTT(MockData.THANH_TOAN.size() + 1);
            tt.setMaDatVe(maDatVe);
            tt.setPhuongThuc(phuongThuc);
            tt.setThoiGian(LocalDateTime.now());
            tt.setSoTien(ganVe(dv).getTongTien());
            MockData.THANH_TOAN.put(tt.getMaTT(), tt);
            return maDatVe;
        }
    }

    @Override
    public boolean huy(int maDatVe) {
        synchronized (MockData.class) {
            DatVe dv = MockData.DAT_VE.get(maDatVe);
            if (dv == null || dv.getTrangThai() != TrangThaiDatVe.DA_THANH_TOAN) {
                return false;
            }
            dv.setTrangThai(TrangThaiDatVe.DA_HUY);
            MockData.VE.values().stream().filter(v -> v.getMaDatVe() == maDatVe)
                    .forEach(v -> v.setTrangThai(Ve.DA_HUY));
            MockData.THANH_TOAN.values().stream().filter(t -> t.getMaDatVe() == maDatVe)
                    .forEach(t -> t.setTrangThai(ThanhToan.HOAN_TIEN));
            return true;
        }
    }

    @Override
    public Optional<DatVe> findById(int maDatVe) {
        return Optional.ofNullable(MockData.DAT_VE.get(maDatVe)).map(this::ganVe);
    }

    @Override
    public List<DatVe> findByKhachHang(int maKH) {
        return MockData.DAT_VE.values().stream().filter(d -> d.getMaKH() == maKH)
                .map(this::ganVe)
                .sorted(Comparator.comparing(DatVe::getNgayDat).reversed()).toList();
    }

    private DatVe ganVe(DatVe dv) {
        dv.setVes(new ArrayList<>(MockData.VE.values().stream()
                .filter(v -> v.getMaDatVe() == dv.getMaDatVe()).toList()));
        return dv;
    }
}


// ==========================================================================================
// MockGheDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockGheDAO.java)
// ==========================================================================================

/** DAO gia cho Ghe (du lieu trong MockData). */
class MockGheDAO extends InMemoryDAO<Ghe> implements GheDAO {

    public MockGheDAO() {
        super(MockData.GHE, Ghe::getMaGhe, Ghe::setMaGhe);
    }

    @Override
    public List<Ghe> findByPhong(int maPhong) {
        return store.values().stream().filter(g -> g.getMaPhong() == maPhong)
                .sorted(Comparator.comparing(Ghe::getHang).thenComparing(Ghe::getSoGhe)).toList();
    }

    @Override
    public int insertBatch(List<Ghe> dsGhe) {
        dsGhe.forEach(this::insert);
        return dsGhe.size();
    }
}


// ==========================================================================================
// MockKhachHangDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockKhachHangDAO.java)
// ==========================================================================================

/** DAO gia cho KhachHang (du lieu trong MockData). */
class MockKhachHangDAO extends InMemoryDAO<KhachHang> implements KhachHangDAO {

    public MockKhachHangDAO() {
        super(MockData.KHACH_HANG, KhachHang::getMaKH, KhachHang::setMaKH);
    }

    @Override
    public Optional<KhachHang> findByMaTK(int maTK) {
        return store.values().stream().filter(k -> k.getMaTK() == maTK).findFirst();
    }

    @Override
    public boolean existsBySoDienThoai(String soDienThoai) {
        return store.values().stream().anyMatch(k -> soDienThoai.equals(k.getSoDienThoai()));
    }

    @Override
    public boolean existsByEmail(String email) {
        return store.values().stream().anyMatch(k -> email.equalsIgnoreCase(k.getEmail()));
    }
}


// ==========================================================================================
// MockLoaiGheDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockLoaiGheDAO.java)
// ==========================================================================================

/** DAO gia cho LoaiGhe (du lieu trong MockData). */
class MockLoaiGheDAO extends InMemoryDAO<LoaiGhe> implements LoaiGheDAO {

    public MockLoaiGheDAO() {
        super(MockData.LOAI_GHE, LoaiGhe::getMaLoaiGhe, LoaiGhe::setMaLoaiGhe);
    }
}


// ==========================================================================================
// MockNhanVienDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockNhanVienDAO.java)
// ==========================================================================================

/** DAO gia cho NhanVien (du lieu trong MockData). */
class MockNhanVienDAO extends InMemoryDAO<NhanVien> implements NhanVienDAO {

    public MockNhanVienDAO() {
        super(MockData.NHAN_VIEN, NhanVien::getMaNV, NhanVien::setMaNV);
    }

    @Override
    public Optional<NhanVien> findByMaTK(int maTK) {
        return store.values().stream().filter(n -> n.getMaTK() == maTK).findFirst();
    }
}


// ==========================================================================================
// MockPhimDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockPhimDAO.java)
// ==========================================================================================

/** DAO gia cho Phim (du lieu trong MockData). */
class MockPhimDAO extends InMemoryDAO<Phim> implements PhimDAO {

    public MockPhimDAO() {
        super(MockData.PHIM, Phim::getMaPhim, Phim::setMaPhim);
    }

    @Override
    public List<Phim> findDangChieu() {
        return store.values().stream().filter(p -> Phim.DANG_CHIEU.equals(p.getTrangThai())).toList();
    }

    @Override
    public List<Phim> search(String tuKhoa, Integer maTL) {
        return store.values().stream()
                .filter(p -> tuKhoa == null || p.getTenPhim().toLowerCase().contains(tuKhoa.toLowerCase()))
                .filter(p -> maTL == null || p.getTheLoais().stream().anyMatch(t -> t.getMaTL() == maTL))
                .toList();
    }

    @Override
    public void capNhatTheLoai(int maPhim, List<Integer> dsMaTL) {
        Phim p = store.get(maPhim);
        if (p != null) {
            p.setTheLoais(new ArrayList<>(dsMaTL.stream().map(MockData.THE_LOAI::get).toList()));
        }
    }
}


// ==========================================================================================
// MockPhongChieuDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockPhongChieuDAO.java)
// ==========================================================================================

/** DAO gia cho PhongChieu (du lieu trong MockData). */
class MockPhongChieuDAO extends InMemoryDAO<PhongChieu> implements PhongChieuDAO {

    public MockPhongChieuDAO() {
        super(MockData.PHONG, PhongChieu::getMaPhong, PhongChieu::setMaPhong);
    }

    @Override
    public List<PhongChieu> findDangHoatDong() {
        return store.values().stream().filter(PhongChieu::isHoatDong).toList();
    }
}


// ==========================================================================================
// MockSuatChieuDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockSuatChieuDAO.java)
// ==========================================================================================

/** DAO gia cho SuatChieu (du lieu trong MockData). */
class MockSuatChieuDAO extends InMemoryDAO<SuatChieu> implements SuatChieuDAO {

    public MockSuatChieuDAO() {
        super(MockData.SUAT, SuatChieu::getMaSuat, SuatChieu::setMaSuat);
    }

    @Override
    public List<SuatChieu> findByPhimVaNgay(int maPhim, LocalDate ngay) {
        return store.values().stream()
                .filter(s -> s.getMaPhim() == maPhim && s.getGioBatDau().toLocalDate().equals(ngay))
                .sorted(Comparator.comparing(SuatChieu::getGioBatDau)).toList();
    }

    @Override
    public List<SuatChieu> findSapChieu(int maPhim, LocalDateTime tu) {
        return store.values().stream()
                .filter(s -> s.getMaPhim() == maPhim && s.getGioBatDau().isAfter(tu))
                .sorted(Comparator.comparing(SuatChieu::getGioBatDau)).toList();
    }

    @Override
    public boolean biTrungLich(int maPhong, LocalDateTime batDau, LocalDateTime ketThuc, Integer boQuaMaSuat) {
        return store.values().stream()
                .filter(s -> s.getMaPhong() == maPhong)
                .filter(s -> boQuaMaSuat == null || s.getMaSuat() != boQuaMaSuat)
                .anyMatch(s -> s.getGioBatDau().isBefore(ketThuc) && batDau.isBefore(s.getGioKetThuc()));
    }
}


// ==========================================================================================
// MockTaiKhoanDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockTaiKhoanDAO.java)
// ==========================================================================================

/** DAO gia cho TaiKhoan (du lieu trong MockData). */
class MockTaiKhoanDAO extends InMemoryDAO<TaiKhoan> implements TaiKhoanDAO {

    public MockTaiKhoanDAO() {
        super(MockData.TAI_KHOAN, TaiKhoan::getMaTK, TaiKhoan::setMaTK);
    }

    @Override
    public Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap) {
        return store.values().stream().filter(t -> t.getTenDangNhap().equalsIgnoreCase(tenDangNhap)).findFirst();
    }

    @Override
    public boolean existsByTenDangNhap(String tenDangNhap) {
        return findByTenDangNhap(tenDangNhap).isPresent();
    }
}


// ==========================================================================================
// MockTheLoaiDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockTheLoaiDAO.java)
// ==========================================================================================

/** DAO gia cho TheLoai (du lieu trong MockData). */
class MockTheLoaiDAO extends InMemoryDAO<TheLoai> implements TheLoaiDAO {

    public MockTheLoaiDAO() {
        super(MockData.THE_LOAI, TheLoai::getMaTL, TheLoai::setMaTL);
    }

    @Override
    public List<TheLoai> findByPhim(int maPhim) {
        Phim p = MockData.PHIM.get(maPhim);
        return p == null ? List.of() : List.copyOf(p.getTheLoais());
    }
}


// ==========================================================================================
// MockThongKeDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockThongKeDAO.java)
// ==========================================================================================

/** Thong ke gia: cong tien cac ve con hieu luc. */
class MockThongKeDAO implements ThongKeDAO {

    @Override
    public BigDecimal tongDoanhThu(LocalDate tu, LocalDate den) {
        return MockData.THANH_TOAN.values().stream()
                .filter(t -> ThanhToan.THANH_CONG.equals(t.getTrangThai()))
                .filter(t -> trongKhoang(t.getThoiGian().toLocalDate(), tu, den))
                .map(ThanhToan::getSoTien).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Map<String, BigDecimal> doanhThuTheoPhim(LocalDate tu, LocalDate den) {
        Map<String, BigDecimal> kq = new LinkedHashMap<>();
        for (Ve v : MockData.VE.values()) {
            if (Ve.HIEU_LUC.equals(v.getTrangThai())
                    && trongKhoang(MockData.DAT_VE.get(v.getMaDatVe()).getNgayDat().toLocalDate(), tu, den)) {
                kq.merge(v.getTenPhim(), v.getGiaVe(), BigDecimal::add);
            }
        }
        return kq;
    }

    @Override
    public Map<LocalDate, BigDecimal> doanhThuTheoNgay(LocalDate tu, LocalDate den) {
        Map<LocalDate, BigDecimal> kq = new TreeMap<>();
        MockData.THANH_TOAN.values().stream()
                .filter(t -> ThanhToan.THANH_CONG.equals(t.getTrangThai()))
                .filter(t -> trongKhoang(t.getThoiGian().toLocalDate(), tu, den))
                .forEach(t -> kq.merge(t.getThoiGian().toLocalDate(), t.getSoTien(), BigDecimal::add));
        return kq;
    }

    private static boolean trongKhoang(LocalDate d, LocalDate tu, LocalDate den) {
        return !d.isBefore(tu) && !d.isAfter(den);
    }
}


// ==========================================================================================
// MockTimKiemDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockTimKiemDAO.java)
// ==========================================================================================

/** Tim kiem tren du lieu gia. Van hien cau SQL se duoc tao, nhung khong chay SQL that. */
class MockTimKiemDAO implements TimKiemDAO {

    @Override
    public KetQuaTimKiem timSuatChieu(BoLocTimKiem loc, boolean anToan) {
        String ghiChu = "Đang dùng dữ liệu giả: câu SQL chỉ hiển thị, không gửi tới SQL Server.";
        if (anToan) {
            TimKiemSql.CauLenh cl = TimKiemSql.taoAnToan(loc);
            return new KetQuaTimKiem(loc(loc), cl.sql(), cl.thamSo(), true, ghiChu);
        }
        String sql = TimKiemSql.taoNoiChuoi(loc);
        if (loc.coTuKhoa() && loc.tuKhoa().contains("'")) {
            return new KetQuaTimKiem(List.of(), sql, List.of(), false, ghiChu
                    + " Hãy chạy với SQL Server (app.mode=jdbc) để thấy chuỗi tấn công lấy được bảng TaiKhoan.");
        }
        return new KetQuaTimKiem(loc(loc), sql, List.of(), false, ghiChu);
    }

    private List<SuatChieu> loc(BoLocTimKiem loc) {
        LocalDateTime now = LocalDateTime.now();
        return MockData.SUAT.values().stream()
                .filter(s -> s.getGioBatDau().isAfter(now))
                .filter(s -> !loc.coTuKhoa() || phim(s).getTenPhim().toLowerCase().contains(loc.tuKhoa().trim().toLowerCase()))
                .filter(s -> loc.maTL() == null || phim(s).getTheLoais().stream().anyMatch(t -> t.getMaTL() == loc.maTL()))
                .filter(s -> loc.ngayChieu() == null || s.getGioBatDau().toLocalDate().equals(loc.ngayChieu()))
                .filter(s -> loc.giaToiDa() == null || s.getGiaCoBan().compareTo(loc.giaToiDa()) <= 0)
                .sorted(Comparator.comparing(SuatChieu::getGioBatDau))
                .map(s -> {
                    s.setTheLoai(phim(s).getTheLoais().stream().map(TheLoai::getTenTL).collect(Collectors.joining(", ")));
                    return s;
                })
                .toList();
    }

    private static Phim phim(SuatChieu s) {
        return MockData.PHIM.get(s.getMaPhim());
    }
}


// ==========================================================================================
// MockVeDAO   (truoc day: src/main/java/com/nhom/datve/dao/mock/MockVeDAO.java)
// ==========================================================================================

/** DAO gia cho Ve. */
class MockVeDAO implements VeDAO {

    @Override
    public Set<Integer> findMaGheDaDat(int maSuat) {
        synchronized (MockData.class) {
            return MockData.VE.values().stream()
                    .filter(v -> v.getMaSuat() == maSuat && Ve.HIEU_LUC.equals(v.getTrangThai()))
                    .map(Ve::getMaGhe).collect(Collectors.toSet());
        }
    }

    @Override
    public List<Ve> findByDatVe(int maDatVe) {
        return MockData.VE.values().stream().filter(v -> v.getMaDatVe() == maDatVe).toList();
    }
}
