package com.nhom.datve;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.nhom.datve.JdbcHelper.exists;
import static com.nhom.datve.JdbcHelper.query;
import static com.nhom.datve.JdbcHelper.queryOne;

/*
 * DAO.java: Tang truy cap du lieu: interface DAO, DAOFactory, va ban JDBC (SQL Server) cua tung DAO.
 * Gom cac lop: GenericDAO, DAOFactory, DatVeDAO, GheDAO, KhachHangDAO, LoaiGheDAO, NhanVienDAO, PhimDAO, PhongChieuDAO, SuatChieuDAO, TaiKhoanDAO, TheLoaiDAO, ThongKeDAO, TimKiemDAO, TimKiemSql, VeDAO, JdbcHelper, DatVeDAOImpl, GheDAOImpl, KhachHangDAOImpl, LoaiGheDAOImpl, NhanVienDAOImpl, PhimDAOImpl, PhongChieuDAOImpl, SuatChieuDAOImpl, TaiKhoanDAOImpl, TheLoaiDAOImpl, ThongKeDAOImpl, TimKiemDAOImpl, VeDAOImpl
 */

// ==========================================================================================
// GenericDAO   (truoc day: src/main/java/com/nhom/datve/dao/GenericDAO.java)
// ==========================================================================================

/**
 * Cac thao tac CRUD chung cho moi bang. T = lop model, K = kieu khoa chinh.
 * Cac interface DAO cu the ke thua interface nay va bo sung truy van rieng.
 */
interface GenericDAO<T, K> {
    List<T> findAll();

    Optional<T> findById(K id);

    /** Them moi, tra ve khoa chinh vua sinh (cot IDENTITY). */
    K insert(T entity);

    boolean update(T entity);

    boolean delete(K id);
}


// ==========================================================================================
// DAOFactory   (truoc day: src/main/java/com/nhom/datve/dao/DAOFactory.java)
// ==========================================================================================

/**
 * Noi duy nhat tao doi tuong DAO. Service va UI chi lam viec voi INTERFACE, khong new ...Impl truc tiep.
 *
 * Quy uoc ten lop:
 *  - Ban JDBC: com.nhom.datve.[TenInterface]Impl     (vd PhimDAOImpl)
 *  - Ban gia : com.nhom.datve.Mock[TenInterface]     (vd MockPhimDAO)
 *
 * Che do jdbc ma lop Impl chua ton tai (ban chua code xong) -> tu dong dung ban mock va in canh bao,
 * nho vay ca nhom chay app duoc trong khi tung DAO dang lam dan.
 */
final class DAOFactory {
    private static final Map<Class<?>, Object> CACHE = new ConcurrentHashMap<>();

    private DAOFactory() { }

    public static TaiKhoanDAO taiKhoan() { return get(TaiKhoanDAO.class); }
    public static KhachHangDAO khachHang() { return get(KhachHangDAO.class); }
    public static NhanVienDAO nhanVien() { return get(NhanVienDAO.class); }
    public static TheLoaiDAO theLoai() { return get(TheLoaiDAO.class); }
    public static PhimDAO phim() { return get(PhimDAO.class); }
    public static PhongChieuDAO phongChieu() { return get(PhongChieuDAO.class); }
    public static LoaiGheDAO loaiGhe() { return get(LoaiGheDAO.class); }
    public static GheDAO ghe() { return get(GheDAO.class); }
    public static SuatChieuDAO suatChieu() { return get(SuatChieuDAO.class); }
    public static VeDAO ve() { return get(VeDAO.class); }
    public static DatVeDAO datVe() { return get(DatVeDAO.class); }
    public static ThongKeDAO thongKe() { return get(ThongKeDAO.class); }
    public static TimKiemDAO timKiem() { return get(TimKiemDAO.class); }

    /** Xoa cache (dung khi doi che do mock/jdbc trong luc chay hoac trong test). */
    public static void reset() { CACHE.clear(); }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> type) {
        return (T) CACHE.computeIfAbsent(type, DAOFactory::create);
    }

    private static Object create(Class<?> type) {
        String ten = type.getSimpleName();
        String lopMock = "com.nhom.datve.Mock" + ten;
        if (!AppConfig.isMockMode()) {
            String lopImpl = "com.nhom.datve." + ten + "Impl";
            try {
                return newInstance(lopImpl);
            } catch (ClassNotFoundException e) {
                System.err.println("[DAOFactory] Chua co " + lopImpl + " -> tam dung du lieu gia.");
            }
        }
        try {
            return newInstance(lopMock);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Khong tim thay ca ban JDBC lan ban mock cua " + ten, e);
        }
    }

    private static Object newInstance(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Khong tao duoc " + className, e);
        }
    }
}


// ==========================================================================================
// DatVeDAO   (truoc day: src/main/java/com/nhom/datve/dao/DatVeDAO.java)
// ==========================================================================================

interface DatVeDAO {
    /**
     * Dat ve + thanh toan trong 1 giao tac: goi stored procedure sp_DatVe.
     * @return MaDatVe vua tao
     * @throws NghiepVuException khi ghe da co nguoi dat, suat da bat dau... (loi do sp nem ra)
     */
    int datVe(int maKH, int maSuat, List<Integer> dsMaGhe, PhuongThucThanhToan phuongThuc)
            throws NghiepVuException;

    /** Huy dat ve: ve -> DA_HUY, dat ve -> DA_HUY, thanh toan -> HOAN_TIEN (1 giao tac). */
    boolean huy(int maDatVe);

    /** Lay 1 lan dat kem danh sach ve. */
    Optional<DatVe> findById(int maDatVe);

    /** Lich su dat ve cua khach, moi nhat truoc. */
    List<DatVe> findByKhachHang(int maKH);
}


// ==========================================================================================
// GheDAO   (truoc day: src/main/java/com/nhom/datve/dao/GheDAO.java)
// ==========================================================================================

interface GheDAO extends GenericDAO<Ghe, Integer> {
    /** Toan bo ghe cua 1 phong (kem LoaiGhe), sap xep theo Hang, SoGhe de ve so do. */
    List<Ghe> findByPhong(int maPhong);

    /** Sinh hang loat ghe khi tao phong moi (dung batch insert). */
    int insertBatch(List<Ghe> dsGhe);
}


// ==========================================================================================
// KhachHangDAO   (truoc day: src/main/java/com/nhom/datve/dao/KhachHangDAO.java)
// ==========================================================================================

interface KhachHangDAO extends GenericDAO<KhachHang, Integer> {
    Optional<KhachHang> findByMaTK(int maTK);

    boolean existsBySoDienThoai(String soDienThoai);

    boolean existsByEmail(String email);
}


// ==========================================================================================
// LoaiGheDAO   (truoc day: src/main/java/com/nhom/datve/dao/LoaiGheDAO.java)
// ==========================================================================================

interface LoaiGheDAO extends GenericDAO<LoaiGhe, Integer> {
}


// ==========================================================================================
// NhanVienDAO   (truoc day: src/main/java/com/nhom/datve/dao/NhanVienDAO.java)
// ==========================================================================================

interface NhanVienDAO extends GenericDAO<NhanVien, Integer> {
    Optional<NhanVien> findByMaTK(int maTK);
}


// ==========================================================================================
// PhimDAO   (truoc day: src/main/java/com/nhom/datve/dao/PhimDAO.java)
// ==========================================================================================

interface PhimDAO extends GenericDAO<Phim, Integer> {
    /** Phim co TrangThai = DANG_CHIEU, hien o trang chu khach hang. */
    List<Phim> findDangChieu();

    /**
     * Tim phim theo ten (chua tu khoa) va/hoac the loai. Tham so null = bo qua dieu kien do.
     * Cai dat bang SQL dong nhung PHAI dung PreparedStatement (xem demo/DynamicSqlDemo).
     */
    List<Phim> search(String tuKhoa, Integer maTL);

    /** Ghi lai danh sach the loai cua phim vao bang Phim_TheLoai (xoa cu, them moi). */
    void capNhatTheLoai(int maPhim, List<Integer> dsMaTL);
}


// ==========================================================================================
// PhongChieuDAO   (truoc day: src/main/java/com/nhom/datve/dao/PhongChieuDAO.java)
// ==========================================================================================

interface PhongChieuDAO extends GenericDAO<PhongChieu, Integer> {
    List<PhongChieu> findDangHoatDong();
}


// ==========================================================================================
// SuatChieuDAO   (truoc day: src/main/java/com/nhom/datve/dao/SuatChieuDAO.java)
// ==========================================================================================

interface SuatChieuDAO extends GenericDAO<SuatChieu, Integer> {
    /** Cac suat cua 1 phim trong 1 ngay (kem tenPhim, tenPhong). */
    List<SuatChieu> findByPhimVaNgay(int maPhim, LocalDate ngay);

    /** Cac suat tu thoi diem 'tu' tro di cua 1 phim. */
    List<SuatChieu> findSapChieu(int maPhim, LocalDateTime tu);

    /**
     * Kiem tra phong da co suat khac chong lan khoang [batDau, ketThuc) chua.
     * boQuaMaSuat: ma suat dang sua (null khi them moi).
     */
    boolean biTrungLich(int maPhong, LocalDateTime batDau, LocalDateTime ketThuc, Integer boQuaMaSuat);
}


// ==========================================================================================
// TaiKhoanDAO   (truoc day: src/main/java/com/nhom/datve/dao/TaiKhoanDAO.java)
// ==========================================================================================

interface TaiKhoanDAO extends GenericDAO<TaiKhoan, Integer> {
    /** Dung cho dang nhap: tim theo ten dang nhap roi so sanh mat khau da bam o tang service. */
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);

    boolean existsByTenDangNhap(String tenDangNhap);
}


// ==========================================================================================
// TheLoaiDAO   (truoc day: src/main/java/com/nhom/datve/dao/TheLoaiDAO.java)
// ==========================================================================================

interface TheLoaiDAO extends GenericDAO<TheLoai, Integer> {
    List<TheLoai> findByPhim(int maPhim);
}


// ==========================================================================================
// ThongKeDAO   (truoc day: src/main/java/com/nhom/datve/dao/ThongKeDAO.java)
// ==========================================================================================

/** Thong ke doanh thu cho admin (cai dat goi ham fn_DoanhThu). */
interface ThongKeDAO {
    /** Tong doanh thu trong khoang ngay [tu, den]. */
    BigDecimal tongDoanhThu(LocalDate tu, LocalDate den);

    /** Doanh thu theo tung phim, giu thu tu giam dan (LinkedHashMap). */
    Map<String, BigDecimal> doanhThuTheoPhim(LocalDate tu, LocalDate den);

    /** Doanh thu theo tung ngay. */
    Map<LocalDate, BigDecimal> doanhThuTheoNgay(LocalDate tu, LocalDate den);
}


// ==========================================================================================
// TimKiemDAO   (truoc day: src/main/java/com/nhom/datve/dao/TimKiemDAO.java)
// ==========================================================================================

/** Tim suat chieu sap toi theo bo loc. */
interface TimKiemDAO {
    /**
     * @param anToan true = PreparedStatement; false = noi chuoi (demo SQL injection)
     */
    KetQuaTimKiem timSuatChieu(BoLocTimKiem boLoc, boolean anToan);
}


// ==========================================================================================
// TimKiemSql   (truoc day: src/main/java/com/nhom/datve/dao/TimKiemSql.java)
// ==========================================================================================

/**
 * Tao cau SQL tim suat chieu LUC CHAY tu bo loc (Dynamic SQL, muc 10.2.3).
 * Hai cach tao cung mot cau truy van:
 *  - taoAnToan(): gia tri di qua dau ?, gui rieng cho PreparedStatement.
 *  - taoNoiChuoi(): CONG CHUOI gia tri vao SQL -> bi SQL injection. CHI DUNG DE DEMO.
 *
 * Cau lenh viet tren MOT dong de thay ro tac dung cua "--" khi bi tan cong.
 * Cot TheLoai dung STRING_AGG (SQL Server 2017 tro len).
 */
final class TimKiemSql {

    /** 6 cot: MaSuat, TenPhim, TheLoai, GioBatDau, TenPhong, GiaCoBan. */
    static final String SELECT = "SELECT sc.MaSuat, p.TenPhim, "
            + "(SELECT STRING_AGG(tl.TenTL, N', ') FROM Phim_TheLoai pt JOIN TheLoai tl ON tl.MaTL = pt.MaTL "
            + "WHERE pt.MaPhim = p.MaPhim) AS TheLoai, sc.GioBatDau, pc.TenPhong, sc.GiaCoBan "
            + "FROM SuatChieu sc JOIN Phim p ON p.MaPhim = sc.MaPhim JOIN PhongChieu pc ON pc.MaPhong = sc.MaPhong "
            + "WHERE sc.GioBatDau > GETDATE()";
    static final String ORDER = " ORDER BY sc.GioBatDau";
    static final String DK_THE_LOAI = " AND EXISTS (SELECT 1 FROM Phim_TheLoai x WHERE x.MaPhim = p.MaPhim AND x.MaTL = ";

    /**
     * Chuoi tan cong mau: dong chuoi bang dau ', ghep them 1 cau SELECT doc bang TaiKhoan (UNION, cung 6 cot)
     * va bien phan con lai thanh chu thich bang --.
     */
    public static final String CHUOI_TAN_CONG =
            "' UNION SELECT MaTK, TenDangNhap, MatKhau, NgayTao, VaiTro, 0 FROM TaiKhoan --";

    private TimKiemSql() { }

    /** Cau SQL + danh sach tham so cho PreparedStatement. */
    public record CauLenh(String sql, List<Object> thamSo) { }

    public static CauLenh taoAnToan(BoLocTimKiem loc) {
        StringBuilder sql = new StringBuilder(SELECT);
        List<Object> ts = new ArrayList<>();
        if (loc.coTuKhoa()) {
            sql.append(" AND p.TenPhim LIKE ?");
            ts.add("%" + loc.tuKhoa().trim() + "%");
        }
        if (loc.maTL() != null) {
            sql.append(DK_THE_LOAI).append("?)");
            ts.add(loc.maTL());
        }
        if (loc.ngayChieu() != null) {
            sql.append(" AND CAST(sc.GioBatDau AS DATE) = ?");
            ts.add(loc.ngayChieu());
        }
        if (loc.giaToiDa() != null) {
            sql.append(" AND sc.GiaCoBan <= ?");
            ts.add(loc.giaToiDa());
        }
        return new CauLenh(sql.append(ORDER).toString(), ts);
    }

    /** KHONG AN TOAN: chi de minh hoa SQL injection. */
    public static String taoNoiChuoi(BoLocTimKiem loc) {
        StringBuilder sql = new StringBuilder(SELECT);
        if (loc.coTuKhoa()) {
            sql.append(" AND p.TenPhim LIKE N'%").append(loc.tuKhoa().trim()).append("%'");
        }
        if (loc.maTL() != null) {
            sql.append(DK_THE_LOAI).append(loc.maTL()).append(")");
        }
        if (loc.ngayChieu() != null) {
            sql.append(" AND CAST(sc.GioBatDau AS DATE) = '").append(loc.ngayChieu()).append("'");
        }
        if (loc.giaToiDa() != null) {
            sql.append(" AND sc.GiaCoBan <= ").append(loc.giaToiDa().toPlainString());
        }
        return sql.append(ORDER).toString();
    }
}


// ==========================================================================================
// VeDAO   (truoc day: src/main/java/com/nhom/datve/dao/VeDAO.java)
// ==========================================================================================

interface VeDAO {
    /** MaGhe cua cac ve con hieu luc trong suat chieu (de to mau ghe da dat tren so do). */
    Set<Integer> findMaGheDaDat(int maSuat);

    List<Ve> findByDatVe(int maDatVe);
}


// ==========================================================================================
// JdbcHelper   (truoc day: src/main/java/com/nhom/datve/dao/impl/JdbcHelper.java)
// ==========================================================================================

/**
 * Ham dung chung cho cac lop DAO JDBC: mo ket noi bang try-with-resources,
 * gan tham so ? cho PreparedStatement va doi SQLException thanh DataAccessException.
 */
final class JdbcHelper {

    /** Doc 1 dong ResultSet thanh 1 doi tuong. */
    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /** Loi vi pham khoa ngoai (vd xoa phim dang co suat chieu). */
    static final int LOI_KHOA_NGOAI = 547;

    private JdbcHelper() { }

    static Connection ketNoi() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    static <T> List<T> query(String sql, RowMapper<T> mapper, Object... thamSo) {
        try (Connection conn = ketNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ganThamSo(ps, thamSo);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> ds = new ArrayList<>();
                while (rs.next()) {
                    ds.add(mapper.map(rs));
                }
                return ds;
            }
        } catch (SQLException e) {
            throw loi("đọc dữ liệu", e);
        }
    }

    static <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... thamSo) {
        return query(sql, mapper, thamSo).stream().findFirst();
    }

    static boolean exists(String sql, Object... thamSo) {
        return !query(sql, rs -> Boolean.TRUE, thamSo).isEmpty();
    }

    /** INSERT/UPDATE/DELETE, tra ve so dong bi anh huong. */
    static int update(String sql, Object... thamSo) {
        try (Connection conn = ketNoi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ganThamSo(ps, thamSo);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw loi("ghi dữ liệu", e);
        }
    }

    /** INSERT vao bang co cot IDENTITY, tra ve khoa vua sinh. */
    static int insert(String sql, Object... thamSo) {
        try (Connection conn = ketNoi()) {
            return insert(conn, sql, thamSo);
        } catch (SQLException e) {
            throw loi("thêm dữ liệu", e);
        }
    }

    /** Ban dung chung Connection, de goi trong 1 transaction. */
    static int insert(Connection conn, String sql, Object... thamSo) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ganThamSo(ps, thamSo);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Không lấy được khóa vừa sinh.");
                }
                return rs.getInt(1);
            }
        }
    }

    static void ganThamSo(PreparedStatement ps, Object... thamSo) throws SQLException {
        for (int i = 0; i < thamSo.length; i++) {
            Object v = thamSo[i];
            if (v instanceof LocalDateTime t) {
                ps.setTimestamp(i + 1, Timestamp.valueOf(t));
            } else if (v instanceof LocalDate d) {
                ps.setDate(i + 1, java.sql.Date.valueOf(d));
            } else if (v instanceof Character c) {
                ps.setString(i + 1, String.valueOf(c));
            } else {
                ps.setObject(i + 1, v);
            }
        }
    }

    static LocalDateTime thoiGian(ResultSet rs, String cot) throws SQLException {
        Timestamp t = rs.getTimestamp(cot);
        return t == null ? null : t.toLocalDateTime();
    }

    static LocalDate ngay(ResultSet rs, String cot) throws SQLException {
        java.sql.Date d = rs.getDate(cot);
        return d == null ? null : d.toLocalDate();
    }

    static DataAccessException loi(String viec, SQLException e) {
        if (e.getErrorCode() == LOI_KHOA_NGOAI) {
            return new DataAccessException(
                    "Không thể " + viec + ": dữ liệu đang được dùng ở bảng khác (vd phim/phòng đã có suất chiếu hoặc vé).", e);
        }
        return new DataAccessException("Lỗi CSDL khi " + viec + ": " + e.getMessage(), e);
    }
}


// ==========================================================================================
// DatVeDAOImpl   (truoc day: src/main/java/com/nhom/datve/dao/impl/DatVeDAOImpl.java)
// ==========================================================================================

/**
 * Cai dat DatVeDAO bang JDBC.
 *  - datVe(): goi stored procedure sp_DatVe bang CallableStatement (muc 10.3 + 10.4 cua chuong 10).
 *  - huy(): tu quan ly transaction o phia Java (setAutoCommit(false) / commit / rollback).
 */
class DatVeDAOImpl implements DatVeDAO {

    /** Loi do THROW trong T-SQL nem ra co ma >= 50000; 2601/2627 la vi pham khoa UNIQUE. */
    private static final int MA_LOI_NGUOI_DUNG = 50000;
    private static final int LOI_TRUNG_UNIQUE_INDEX = 2601;
    private static final int LOI_TRUNG_UNIQUE_KEY = 2627;

    private static final String SQL_CHI_TIET = """
            SELECT dv.MaDatVe, dv.MaKH, dv.NgayDat, dv.TrangThai, tt.PhuongThuc,
                   v.MaVe, v.MaSuat, v.MaGhe, v.GiaVe, v.TrangThai AS TrangThaiVe,
                   g.Hang, g.SoGhe, p.TenPhim, pc.TenPhong, sc.GioBatDau
            FROM DatVe dv
                 LEFT JOIN ThanhToan tt ON tt.MaDatVe = dv.MaDatVe
                 JOIN Ve v          ON v.MaDatVe = dv.MaDatVe
                 JOIN SuatChieu sc  ON sc.MaSuat = v.MaSuat
                 JOIN Phim p        ON p.MaPhim  = sc.MaPhim
                 JOIN PhongChieu pc ON pc.MaPhong = sc.MaPhong
                 JOIN Ghe g         ON g.MaGhe   = v.MaGhe
            """;

    @Override
    public int datVe(int maKH, int maSuat, List<Integer> dsMaGhe, PhuongThucThanhToan phuongThuc)
            throws NghiepVuException {
        String dsGhe = dsMaGhe.stream().map(String::valueOf).collect(Collectors.joining(","));
        try (Connection conn = DBConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall("{call sp_DatVe(?, ?, ?, ?, ?)}")) {
            cs.setInt(1, maKH);
            cs.setInt(2, maSuat);
            cs.setString(3, dsGhe);                 // vd "12,13,14"
            cs.setString(4, phuongThuc.name());     // TIEN_MAT / THE / VI_DIEN_TU
            cs.registerOutParameter(5, Types.INTEGER);
            cs.execute();
            return cs.getInt(5);
        } catch (SQLException e) {
            int ma = e.getErrorCode();
            if (ma >= MA_LOI_NGUOI_DUNG) {
                throw new NghiepVuException(e.getMessage());
            }
            if (ma == LOI_TRUNG_UNIQUE_INDEX || ma == LOI_TRUNG_UNIQUE_KEY) {
                throw new NghiepVuException("Có ghế vừa được người khác đặt. Vui lòng chọn lại ghế.");
            }
            throw new DataAccessException("Lỗi CSDL khi đặt vé", e);
        }
    }

    @Override
    public boolean huy(int maDatVe) {
        String sqlDatVe = "UPDATE DatVe SET TrangThai = 'DA_HUY' WHERE MaDatVe = ? AND TrangThai = 'DA_THANH_TOAN'";
        String sqlVe = "UPDATE Ve SET TrangThai = 'DA_HUY' WHERE MaDatVe = ? AND TrangThai = 'HIEU_LUC'";
        String sqlTT = "UPDATE ThanhToan SET TrangThai = 'HOAN_TIEN' WHERE MaDatVe = ?";
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);              // bat dau giao tac
            try (PreparedStatement ps1 = conn.prepareStatement(sqlDatVe);
                 PreparedStatement ps2 = conn.prepareStatement(sqlVe);
                 PreparedStatement ps3 = conn.prepareStatement(sqlTT)) {
                ps1.setInt(1, maDatVe);
                if (ps1.executeUpdate() == 0) {     // khong ton tai hoac da huy truoc do
                    conn.rollback();
                    return false;
                }
                ps2.setInt(1, maDatVe);
                ps2.executeUpdate();
                ps3.setInt(1, maDatVe);
                ps3.executeUpdate();
                conn.commit();                      // ca 3 lenh cung thanh cong
                return true;
            } catch (SQLException e) {
                conn.rollback();                    // 1 lenh loi -> huy ca giao tac
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi CSDL khi hủy vé", e);
        }
    }

    @Override
    public Optional<DatVe> findById(int maDatVe) {
        List<DatVe> kq = truyVan(SQL_CHI_TIET + " WHERE dv.MaDatVe = ? ORDER BY g.Hang, g.SoGhe", maDatVe);
        return kq.stream().findFirst();
    }

    @Override
    public List<DatVe> findByKhachHang(int maKH) {
        return truyVan(SQL_CHI_TIET + " WHERE dv.MaKH = ? ORDER BY dv.NgayDat DESC, dv.MaDatVe DESC, g.Hang, g.SoGhe", maKH);
    }

    /** Chay truy van chi tiet, gom cac dong (moi dong 1 ve) thanh tung DatVe. */
    private List<DatVe> truyVan(String sql, int thamSo) {
        Map<Integer, DatVe> gom = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thamSo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {                 // con tro duyet tung dong ket qua
                    int maDatVe = rs.getInt("MaDatVe");
                    DatVe dv = gom.get(maDatVe);
                    if (dv == null) {
                        dv = new DatVe(maDatVe, rs.getInt("MaKH"),
                                rs.getTimestamp("NgayDat").toLocalDateTime(),
                                TrangThaiDatVe.valueOf(rs.getString("TrangThai")));
                        String pt = rs.getString("PhuongThuc");
                        dv.setPhuongThuc(pt == null ? null : PhuongThucThanhToan.valueOf(pt));
                        dv.setVes(new ArrayList<>());
                        gom.put(maDatVe, dv);
                    }
                    Ve ve = new Ve(rs.getInt("MaVe"), maDatVe, rs.getInt("MaSuat"),
                            rs.getInt("MaGhe"), rs.getBigDecimal("GiaVe"));
                    ve.setTrangThai(rs.getString("TrangThaiVe"));
                    ve.setTenGhe(rs.getString("Hang").trim() + rs.getInt("SoGhe"));
                    ve.setTenPhim(rs.getString("TenPhim"));
                    ve.setTenPhong(rs.getString("TenPhong"));
                    ve.setGioBatDau(rs.getTimestamp("GioBatDau").toLocalDateTime());
                    dv.getVes().add(ve);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi CSDL khi đọc lịch sử đặt vé", e);
        }
        return new ArrayList<>(gom.values());
    }
}


// ==========================================================================================
// GheDAOImpl   (truoc day: src/main/java/com/nhom/datve/dao/impl/GheDAOImpl.java)
// ==========================================================================================

/** GheDAO bang JDBC (bang Ghe + LoaiGhe). Tao dung lop con GheThuong/GheVIP/GheDoi qua Ghe.taoGhe. */
class GheDAOImpl implements GheDAO {

    private static final String SELECT = """
            SELECT g.MaGhe, g.MaPhong, g.Hang, g.SoGhe, lg.MaLoaiGhe, lg.TenLoai, lg.HeSoGia
            FROM Ghe g JOIN LoaiGhe lg ON lg.MaLoaiGhe = g.MaLoaiGhe""";
    private static final String SQL_INSERT = "INSERT INTO Ghe (MaPhong, MaLoaiGhe, Hang, SoGhe) VALUES (?, ?, ?, ?)";

    @Override
    public List<Ghe> findAll() {
        return query(SELECT + " ORDER BY g.MaPhong, g.Hang, g.SoGhe", GheDAOImpl::doc);
    }

    @Override
    public Optional<Ghe> findById(Integer id) {
        return queryOne(SELECT + " WHERE g.MaGhe = ?", GheDAOImpl::doc, id);
    }

    @Override
    public List<Ghe> findByPhong(int maPhong) {
        return query(SELECT + " WHERE g.MaPhong = ? ORDER BY g.Hang, g.SoGhe", GheDAOImpl::doc, maPhong);
    }

    @Override
    public Integer insert(Ghe g) {
        int ma = JdbcHelper.insert(SQL_INSERT, g.getMaPhong(), g.getLoaiGhe().getMaLoaiGhe(), g.getHang(), g.getSoGhe());
        g.setMaGhe(ma);
        return ma;
    }

    /** Them nhieu ghe bang addBatch/executeBatch trong 1 transaction. */
    @Override
    public int insertBatch(List<Ghe> dsGhe) {
        if (dsGhe.isEmpty()) {
            return 0;
        }
        try (Connection conn = JdbcHelper.ketNoi()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
                for (Ghe g : dsGhe) {
                    JdbcHelper.ganThamSo(ps, g.getMaPhong(), g.getLoaiGhe().getMaLoaiGhe(), g.getHang(), g.getSoGhe());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                return dsGhe.size();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw JdbcHelper.loi("thêm ghế", e);
        }
    }

    @Override
    public boolean update(Ghe g) {
        return JdbcHelper.update("UPDATE Ghe SET MaPhong = ?, MaLoaiGhe = ?, Hang = ?, SoGhe = ? WHERE MaGhe = ?",
                g.getMaPhong(), g.getLoaiGhe().getMaLoaiGhe(), g.getHang(), g.getSoGhe(), g.getMaGhe()) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return JdbcHelper.update("DELETE FROM Ghe WHERE MaGhe = ?", id) > 0;
    }

    private static Ghe doc(ResultSet rs) throws SQLException {
        LoaiGhe lg = new LoaiGhe(rs.getInt("MaLoaiGhe"), rs.getString("TenLoai"), rs.getBigDecimal("HeSoGia"));
        return Ghe.taoGhe(rs.getInt("MaGhe"), rs.getInt("MaPhong"), lg,
                rs.getString("Hang").trim().charAt(0), rs.getInt("SoGhe"));
    }
}


// ==========================================================================================
// KhachHangDAOImpl   (truoc day: src/main/java/com/nhom/datve/dao/impl/KhachHangDAOImpl.java)
// ==========================================================================================

/** KhachHangDAO bang JDBC (bang KhachHang). */
class KhachHangDAOImpl implements KhachHangDAO {

    private static final String SELECT =
            "SELECT MaKH, MaTK, HoTen, SoDienThoai, Email, NgaySinh FROM KhachHang";

    @Override
    public List<KhachHang> findAll() {
        return query(SELECT + " ORDER BY MaKH", KhachHangDAOImpl::doc);
    }

    @Override
    public Optional<KhachHang> findById(Integer id) {
        return queryOne(SELECT + " WHERE MaKH = ?", KhachHangDAOImpl::doc, id);
    }

    @Override
    public Optional<KhachHang> findByMaTK(int maTK) {
        return queryOne(SELECT + " WHERE MaTK = ?", KhachHangDAOImpl::doc, maTK);
    }

    @Override
    public boolean existsBySoDienThoai(String soDienThoai) {
        return exists("SELECT 1 FROM KhachHang WHERE SoDienThoai = ?", soDienThoai);
    }

    @Override
    public boolean existsByEmail(String email) {
        return exists("SELECT 1 FROM KhachHang WHERE LOWER(Email) = LOWER(?)", email);
    }

    /** Them khach hang va dong bo HoTen/Email/SoDienThoai sang TaiKhoan trong cung 1 transaction. */
    @Override
    public Integer insert(KhachHang kh) {
        try (Connection conn = JdbcHelper.ketNoi()) {
            conn.setAutoCommit(false);
            try {
                int ma = JdbcHelper.insert(conn,
                        "INSERT INTO KhachHang (MaTK, HoTen, SoDienThoai, Email, NgaySinh) VALUES (?, ?, ?, ?, ?)",
                        kh.getMaTK() == 0 ? null : kh.getMaTK(), kh.getHoTen(), kh.getSoDienThoai(),
                        kh.getEmail(), kh.getNgaySinh());
                if (kh.getMaTK() != 0) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE TaiKhoan SET HoTen = ?, Email = ?, SoDienThoai = ? WHERE MaTK = ?")) {
                        JdbcHelper.ganThamSo(ps, kh.getHoTen(), kh.getEmail(), kh.getSoDienThoai(), kh.getMaTK());
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                kh.setMaKH(ma);
                return ma;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw JdbcHelper.loi("thêm khách hàng", e);
        }
    }

    @Override
    public boolean update(KhachHang kh) {
        return JdbcHelper.update("UPDATE KhachHang SET HoTen = ?, SoDienThoai = ?, Email = ?, NgaySinh = ? WHERE MaKH = ?",
                kh.getHoTen(), kh.getSoDienThoai(), kh.getEmail(), kh.getNgaySinh(), kh.getMaKH()) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return JdbcHelper.update("DELETE FROM KhachHang WHERE MaKH = ?", id) > 0;
    }

    private static KhachHang doc(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang(rs.getInt("MaKH"), rs.getInt("MaTK"), rs.getString("HoTen"),
                rs.getString("SoDienThoai"), rs.getString("Email"));
        kh.setNgaySinh(JdbcHelper.ngay(rs, "NgaySinh"));
        return kh;
    }
}


// ==========================================================================================
// LoaiGheDAOImpl   (truoc day: src/main/java/com/nhom/datve/dao/impl/LoaiGheDAOImpl.java)
// ==========================================================================================

/** LoaiGheDAO bang JDBC (bang LoaiGhe). */
class LoaiGheDAOImpl implements LoaiGheDAO {

    @Override
    public List<LoaiGhe> findAll() {
        return query("SELECT MaLoaiGhe, TenLoai, HeSoGia FROM LoaiGhe ORDER BY MaLoaiGhe", LoaiGheDAOImpl::doc);
    }

    @Override
    public Optional<LoaiGhe> findById(Integer id) {
        return queryOne("SELECT MaLoaiGhe, TenLoai, HeSoGia FROM LoaiGhe WHERE MaLoaiGhe = ?", LoaiGheDAOImpl::doc, id);
    }

    @Override
    public Integer insert(LoaiGhe lg) {
        int ma = JdbcHelper.insert("INSERT INTO LoaiGhe (TenLoai, HeSoGia) VALUES (?, ?)", lg.getTenLoai(), lg.getHeSoGia());
        lg.setMaLoaiGhe(ma);
        return ma;
    }

    @Override
    public boolean update(LoaiGhe lg) {
        return JdbcHelper.update("UPDATE LoaiGhe SET TenLoai = ?, HeSoGia = ? WHERE MaLoaiGhe = ?",
                lg.getTenLoai(), lg.getHeSoGia(), lg.getMaLoaiGhe()) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return JdbcHelper.update("DELETE FROM LoaiGhe WHERE MaLoaiGhe = ?", id) > 0;
    }

    private static LoaiGhe doc(ResultSet rs) throws SQLException {
        return new LoaiGhe(rs.getInt("MaLoaiGhe"), rs.getString("TenLoai"), rs.getBigDecimal("HeSoGia"));
    }
}


// ==========================================================================================
// NhanVienDAOImpl   (truoc day: src/main/java/com/nhom/datve/dao/impl/NhanVienDAOImpl.java)
// ==========================================================================================

/** NhanVienDAO bang JDBC (bang NhanVien). */
class NhanVienDAOImpl implements NhanVienDAO {

    private static final String SELECT = "SELECT MaNV, MaTK, HoTen, ChucVu, SoDienThoai FROM NhanVien";

    @Override
    public List<NhanVien> findAll() {
        return query(SELECT + " ORDER BY MaNV", NhanVienDAOImpl::doc);
    }

    @Override
    public Optional<NhanVien> findById(Integer id) {
        return queryOne(SELECT + " WHERE MaNV = ?", NhanVienDAOImpl::doc, id);
    }

    @Override
    public Optional<NhanVien> findByMaTK(int maTK) {
        return queryOne(SELECT + " WHERE MaTK = ?", NhanVienDAOImpl::doc, maTK);
    }

    @Override
    public Integer insert(NhanVien nv) {
        int ma = JdbcHelper.insert("INSERT INTO NhanVien (MaTK, HoTen, ChucVu, SoDienThoai) VALUES (?, ?, ?, ?)",
                nv.getMaTK(), nv.getHoTen(), nv.getChucVu() == null ? "Nhân viên" : nv.getChucVu(), nv.getSoDienThoai());
        nv.setMaNV(ma);
        return ma;
    }

    @Override
    public boolean update(NhanVien nv) {
        return JdbcHelper.update("UPDATE NhanVien SET HoTen = ?, ChucVu = ?, SoDienThoai = ? WHERE MaNV = ?",
                nv.getHoTen(), nv.getChucVu(), nv.getSoDienThoai(), nv.getMaNV()) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return JdbcHelper.update("DELETE FROM NhanVien WHERE MaNV = ?", id) > 0;
    }

    private static NhanVien doc(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien(rs.getInt("MaNV"), rs.getInt("MaTK"), rs.getString("HoTen"), rs.getString("ChucVu"));
        nv.setSoDienThoai(rs.getString("SoDienThoai"));
        return nv;
    }
}


// ==========================================================================================
// PhimDAOImpl   (truoc day: src/main/java/com/nhom/datve/dao/impl/PhimDAOImpl.java)
// ==========================================================================================

/** PhimDAO bang JDBC (bang Phim, Phim_TheLoai). Moi Phim tra ve deu kem danh sach the loai. */
class PhimDAOImpl implements PhimDAO {

    private static final String SELECT = """
            SELECT MaPhim, TenPhim, ThoiLuong, DaoDien, NgayKhoiChieu, DoTuoi, TrangThai, Poster, MoTa
            FROM Phim p""";

    @Override
    public List<Phim> findAll() {
        return kemTheLoai(query(SELECT + " ORDER BY MaPhim", PhimDAOImpl::doc));
    }

    @Override
    public Optional<Phim> findById(Integer id) {
        return kemTheLoai(query(SELECT + " WHERE MaPhim = ?", PhimDAOImpl::doc, id)).stream().findFirst();
    }

    @Override
    public List<Phim> findDangChieu() {
        return kemTheLoai(query(SELECT + " WHERE TrangThai = ? ORDER BY NgayKhoiChieu DESC, MaPhim",
                PhimDAOImpl::doc, Phim.DANG_CHIEU));
    }

    @Override
    public List<Phim> search(String tuKhoa, Integer maTL) {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1 = 1");
        List<Object> ts = new ArrayList<>();
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            sql.append(" AND p.TenPhim LIKE ?");
            ts.add("%" + tuKhoa.trim() + "%");
        }
        if (maTL != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM Phim_TheLoai x WHERE x.MaPhim = p.MaPhim AND x.MaTL = ?)");
            ts.add(maTL);
        }
        sql.append(" ORDER BY MaPhim");
        return kemTheLoai(query(sql.toString(), PhimDAOImpl::doc, ts.toArray()));
    }

    @Override
    public Integer insert(Phim p) {
        int ma = JdbcHelper.insert("""
                INSERT INTO Phim (TenPhim, ThoiLuong, DaoDien, NgayKhoiChieu, DoTuoi, TrangThai, Poster, MoTa)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
                p.getTenPhim(), p.getThoiLuong(), p.getDaoDien(), ngayKhoiChieu(p), p.getDoTuoi(),
                p.getTrangThai(), p.getPoster(), p.getMoTa());
        p.setMaPhim(ma);
        return ma;
    }

    @Override
    public boolean update(Phim p) {
        return JdbcHelper.update("""
                UPDATE Phim SET TenPhim = ?, ThoiLuong = ?, DaoDien = ?, NgayKhoiChieu = ?, DoTuoi = ?,
                       TrangThai = ?, Poster = ?, MoTa = ?
                WHERE MaPhim = ?""",
                p.getTenPhim(), p.getThoiLuong(), p.getDaoDien(), ngayKhoiChieu(p), p.getDoTuoi(),
                p.getTrangThai(), p.getPoster(), p.getMoTa(), p.getMaPhim()) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return JdbcHelper.update("DELETE FROM Phim WHERE MaPhim = ?", id) > 0;
    }

    /** Xoa het the loai cu roi them lai, trong 1 transaction. */
    @Override
    public void capNhatTheLoai(int maPhim, List<Integer> dsMaTL) {
        try (Connection conn = JdbcHelper.ketNoi()) {
            conn.setAutoCommit(false);
            try (PreparedStatement xoa = conn.prepareStatement("DELETE FROM Phim_TheLoai WHERE MaPhim = ?");
                 PreparedStatement them = conn.prepareStatement("INSERT INTO Phim_TheLoai (MaPhim, MaTL) VALUES (?, ?)")) {
                xoa.setInt(1, maPhim);
                xoa.executeUpdate();
                for (Integer maTL : dsMaTL.stream().distinct().toList()) {
                    them.setInt(1, maPhim);
                    them.setInt(2, maTL);
                    them.addBatch();
                }
                them.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw JdbcHelper.loi("cập nhật thể loại phim", e);
        }
    }

    /** Nap the loai cho ca danh sach bang 1 truy van. */
    private static List<Phim> kemTheLoai(List<Phim> ds) {
        if (ds.isEmpty()) {
            return ds;
        }
        Map<Integer, Phim> theoMa = new HashMap<>();
        ds.forEach(p -> theoMa.put(p.getMaPhim(), p));
        query("""
                SELECT pt.MaPhim, tl.MaTL, tl.TenTL FROM Phim_TheLoai pt JOIN TheLoai tl ON tl.MaTL = pt.MaTL
                ORDER BY tl.MaTL""", rs -> {
                    Phim p = theoMa.get(rs.getInt("MaPhim"));
                    if (p != null) {
                        p.getTheLoais().add(new TheLoai(rs.getInt("MaTL"), rs.getString("TenTL")));
                    }
                    return Boolean.TRUE;
                });
        return ds;
    }

    private static LocalDate ngayKhoiChieu(Phim p) {
        return p.getNgayKhoiChieu() == null ? LocalDate.now() : p.getNgayKhoiChieu();
    }

    private static Phim doc(ResultSet rs) throws SQLException {
        Phim p = new Phim(rs.getInt("MaPhim"), rs.getString("TenPhim"), rs.getInt("ThoiLuong"),
                rs.getString("DaoDien"), rs.getString("DoTuoi"));
        p.setNgayKhoiChieu(JdbcHelper.ngay(rs, "NgayKhoiChieu"));
        p.setTrangThai(rs.getString("TrangThai"));
        p.setPoster(rs.getString("Poster"));
        p.setMoTa(rs.getString("MoTa"));
        p.setTheLoais(new ArrayList<>());
        return p;
    }
}


// ==========================================================================================
// PhongChieuDAOImpl   (truoc day: src/main/java/com/nhom/datve/dao/impl/PhongChieuDAOImpl.java)
// ==========================================================================================

/** PhongChieuDAO bang JDBC (bang PhongChieu). */
class PhongChieuDAOImpl implements PhongChieuDAO {

    private static final String SELECT = "SELECT MaPhong, TenPhong, SoHang, SoCot, HoatDong FROM PhongChieu";

    @Override
    public List<PhongChieu> findAll() {
        return query(SELECT + " ORDER BY MaPhong", PhongChieuDAOImpl::doc);
    }

    @Override
    public Optional<PhongChieu> findById(Integer id) {
        return queryOne(SELECT + " WHERE MaPhong = ?", PhongChieuDAOImpl::doc, id);
    }

    @Override
    public List<PhongChieu> findDangHoatDong() {
        return query(SELECT + " WHERE HoatDong = 1 ORDER BY MaPhong", PhongChieuDAOImpl::doc);
    }

    @Override
    public Integer insert(PhongChieu pc) {
        int ma = JdbcHelper.insert("INSERT INTO PhongChieu (TenPhong, SoHang, SoCot, HoatDong) VALUES (?, ?, ?, ?)",
                pc.getTenPhong(), pc.getSoHang(), pc.getSoCot(), pc.isHoatDong());
        pc.setMaPhong(ma);
        return ma;
    }

    @Override
    public boolean update(PhongChieu pc) {
        return JdbcHelper.update("UPDATE PhongChieu SET TenPhong = ?, SoHang = ?, SoCot = ?, HoatDong = ? WHERE MaPhong = ?",
                pc.getTenPhong(), pc.getSoHang(), pc.getSoCot(), pc.isHoatDong(), pc.getMaPhong()) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return JdbcHelper.update("DELETE FROM PhongChieu WHERE MaPhong = ?", id) > 0;
    }

    private static PhongChieu doc(ResultSet rs) throws SQLException {
        PhongChieu pc = new PhongChieu(rs.getInt("MaPhong"), rs.getString("TenPhong"),
                rs.getInt("SoHang"), rs.getInt("SoCot"));
        pc.setHoatDong(rs.getBoolean("HoatDong"));
        return pc;
    }
}


// ==========================================================================================
// SuatChieuDAOImpl   (truoc day: src/main/java/com/nhom/datve/dao/impl/SuatChieuDAOImpl.java)
// ==========================================================================================

/** SuatChieuDAO bang JDBC (bang SuatChieu, kem ten phim/ten phong de hien thi). */
class SuatChieuDAOImpl implements SuatChieuDAO {

    private static final String SELECT = """
            SELECT sc.MaSuat, sc.MaPhim, sc.MaPhong, sc.GioBatDau, sc.GioKetThuc, sc.GiaCoBan,
                   p.TenPhim, pc.TenPhong
            FROM SuatChieu sc
                 JOIN Phim p        ON p.MaPhim = sc.MaPhim
                 JOIN PhongChieu pc ON pc.MaPhong = sc.MaPhong""";

    @Override
    public List<SuatChieu> findAll() {
        return query(SELECT + " ORDER BY sc.GioBatDau", SuatChieuDAOImpl::doc);
    }

    @Override
    public Optional<SuatChieu> findById(Integer id) {
        return queryOne(SELECT + " WHERE sc.MaSuat = ?", SuatChieuDAOImpl::doc, id);
    }

    @Override
    public List<SuatChieu> findByPhimVaNgay(int maPhim, LocalDate ngay) {
        return query(SELECT + " WHERE sc.MaPhim = ? AND CAST(sc.GioBatDau AS DATE) = ? ORDER BY sc.GioBatDau",
                SuatChieuDAOImpl::doc, maPhim, ngay);
    }

    @Override
    public List<SuatChieu> findSapChieu(int maPhim, LocalDateTime tu) {
        return query(SELECT + " WHERE sc.MaPhim = ? AND sc.GioBatDau > ? ORDER BY sc.GioBatDau",
                SuatChieuDAOImpl::doc, maPhim, tu);
    }

    @Override
    public boolean biTrungLich(int maPhong, LocalDateTime batDau, LocalDateTime ketThuc, Integer boQuaMaSuat) {
        int boQua = boQuaMaSuat == null ? -1 : boQuaMaSuat;
        return exists("SELECT 1 FROM SuatChieu WHERE MaPhong = ? AND GioBatDau < ? AND GioKetThuc > ? AND MaSuat <> ?",
                maPhong, ketThuc, batDau, boQua);
    }

    @Override
    public Integer insert(SuatChieu s) {
        int ma = JdbcHelper.insert(
                "INSERT INTO SuatChieu (MaPhim, MaPhong, GioBatDau, GioKetThuc, GiaCoBan) VALUES (?, ?, ?, ?, ?)",
                s.getMaPhim(), s.getMaPhong(), s.getGioBatDau(), s.getGioKetThuc(), s.getGiaCoBan());
        s.setMaSuat(ma);
        return ma;
    }

    @Override
    public boolean update(SuatChieu s) {
        return JdbcHelper.update("""
                UPDATE SuatChieu SET MaPhim = ?, MaPhong = ?, GioBatDau = ?, GioKetThuc = ?, GiaCoBan = ?
                WHERE MaSuat = ?""",
                s.getMaPhim(), s.getMaPhong(), s.getGioBatDau(), s.getGioKetThuc(), s.getGiaCoBan(), s.getMaSuat()) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return JdbcHelper.update("DELETE FROM SuatChieu WHERE MaSuat = ?", id) > 0;
    }

    private static SuatChieu doc(ResultSet rs) throws SQLException {
        SuatChieu s = new SuatChieu(rs.getInt("MaSuat"), rs.getInt("MaPhim"), rs.getInt("MaPhong"),
                JdbcHelper.thoiGian(rs, "GioBatDau"), JdbcHelper.thoiGian(rs, "GioKetThuc"), rs.getBigDecimal("GiaCoBan"));
        s.setTenPhim(rs.getString("TenPhim"));
        s.setTenPhong(rs.getString("TenPhong"));
        return s;
    }
}


// ==========================================================================================
// TaiKhoanDAOImpl   (truoc day: src/main/java/com/nhom/datve/dao/impl/TaiKhoanDAOImpl.java)
// ==========================================================================================

/** TaiKhoanDAO bang JDBC (bang TaiKhoan). */
class TaiKhoanDAOImpl implements TaiKhoanDAO {

    private static final String SELECT =
            "SELECT MaTK, TenDangNhap, MatKhau, VaiTro, TrangThai, NgayTao FROM TaiKhoan";

    @Override
    public List<TaiKhoan> findAll() {
        return query(SELECT + " ORDER BY MaTK", TaiKhoanDAOImpl::doc);
    }

    @Override
    public Optional<TaiKhoan> findById(Integer id) {
        return queryOne(SELECT + " WHERE MaTK = ?", TaiKhoanDAOImpl::doc, id);
    }

    @Override
    public Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap) {
        return queryOne(SELECT + " WHERE TenDangNhap = ?", TaiKhoanDAOImpl::doc, tenDangNhap);
    }

    @Override
    public boolean existsByTenDangNhap(String tenDangNhap) {
        return exists("SELECT 1 FROM TaiKhoan WHERE TenDangNhap = ?", tenDangNhap);
    }

    /**
     * Bang TaiKhoan bat buoc HoTen va MaVaiTro. Luc tao tai khoan chua biet ho ten nen tam dung
     * ten dang nhap; KhachHangDAOImpl.insert se cap nhat lai HoTen/Email/SoDienThoai.
     * MaVaiTro tra theo ten vai tro trong bang VaiTro.
     */
    @Override
    public Integer insert(TaiKhoan tk) {
        String vaiTro = tk.getVaiTro() == null ? VaiTro.KHACHHANG.name() : tk.getVaiTro().name();
        int ma = JdbcHelper.insert("""
                INSERT INTO TaiKhoan (TenDangNhap, MatKhau, HoTen, MaVaiTro, VaiTro, TrangThai)
                VALUES (?, ?, ?, (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = ?), ?, ?)""",
                tk.getTenDangNhap(), tk.getMatKhau(), tk.getTenDangNhap(), vaiTro, vaiTro, tk.isTrangThai());
        tk.setMaTK(ma);
        return ma;
    }

    @Override
    public boolean update(TaiKhoan tk) {
        return JdbcHelper.update("UPDATE TaiKhoan SET TenDangNhap = ?, MatKhau = ?, VaiTro = ?, TrangThai = ? WHERE MaTK = ?",
                tk.getTenDangNhap(), tk.getMatKhau(), tk.getVaiTro().name(), tk.isTrangThai(), tk.getMaTK()) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return JdbcHelper.update("DELETE FROM TaiKhoan WHERE MaTK = ?", id) > 0;
    }

    private static TaiKhoan doc(ResultSet rs) throws SQLException {
        TaiKhoan tk = new TaiKhoan(rs.getInt("MaTK"), rs.getString("TenDangNhap"), rs.getString("MatKhau"),
                VaiTro.valueOf(rs.getString("VaiTro").trim()));
        tk.setTrangThai(rs.getBoolean("TrangThai"));
        tk.setNgayTao(JdbcHelper.thoiGian(rs, "NgayTao"));
        return tk;
    }
}


// ==========================================================================================
// TheLoaiDAOImpl   (truoc day: src/main/java/com/nhom/datve/dao/impl/TheLoaiDAOImpl.java)
// ==========================================================================================

/** TheLoaiDAO bang JDBC (bang TheLoai, Phim_TheLoai). */
class TheLoaiDAOImpl implements TheLoaiDAO {

    @Override
    public List<TheLoai> findAll() {
        return query("SELECT MaTL, TenTL FROM TheLoai ORDER BY MaTL", TheLoaiDAOImpl::doc);
    }

    @Override
    public Optional<TheLoai> findById(Integer id) {
        return queryOne("SELECT MaTL, TenTL FROM TheLoai WHERE MaTL = ?", TheLoaiDAOImpl::doc, id);
    }

    @Override
    public List<TheLoai> findByPhim(int maPhim) {
        return query("""
                SELECT tl.MaTL, tl.TenTL FROM TheLoai tl JOIN Phim_TheLoai pt ON pt.MaTL = tl.MaTL
                WHERE pt.MaPhim = ? ORDER BY tl.MaTL""", TheLoaiDAOImpl::doc, maPhim);
    }

    @Override
    public Integer insert(TheLoai tl) {
        int ma = JdbcHelper.insert("INSERT INTO TheLoai (TenTL) VALUES (?)", tl.getTenTL());
        tl.setMaTL(ma);
        return ma;
    }

    @Override
    public boolean update(TheLoai tl) {
        return JdbcHelper.update("UPDATE TheLoai SET TenTL = ? WHERE MaTL = ?", tl.getTenTL(), tl.getMaTL()) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return JdbcHelper.update("DELETE FROM TheLoai WHERE MaTL = ?", id) > 0;
    }

    private static TheLoai doc(ResultSet rs) throws SQLException {
        return new TheLoai(rs.getInt("MaTL"), rs.getString("TenTL"));
    }
}


// ==========================================================================================
// ThongKeDAOImpl   (truoc day: src/main/java/com/nhom/datve/dao/impl/ThongKeDAOImpl.java)
// ==========================================================================================

/** ThongKeDAO bang JDBC. Tong doanh thu goi ham dbo.fn_DoanhThu trong 03_procedure_trigger.sql. */
class ThongKeDAOImpl implements ThongKeDAO {

    @Override
    public BigDecimal tongDoanhThu(LocalDate tu, LocalDate den) {
        return query("SELECT dbo.fn_DoanhThu(?, ?) AS Tong", rs -> rs.getBigDecimal("Tong"), tu, den)
                .stream().findFirst().orElse(BigDecimal.ZERO);
    }

    @Override
    public Map<String, BigDecimal> doanhThuTheoPhim(LocalDate tu, LocalDate den) {
        Map<String, BigDecimal> kq = new LinkedHashMap<>();
        query("""
                SELECT p.TenPhim, SUM(v.GiaVe) AS DoanhThu
                FROM Ve v
                     JOIN DatVe dv     ON dv.MaDatVe = v.MaDatVe
                     JOIN SuatChieu sc ON sc.MaSuat = v.MaSuat
                     JOIN Phim p       ON p.MaPhim = sc.MaPhim
                WHERE v.TrangThai = 'HIEU_LUC' AND CAST(dv.NgayDat AS DATE) BETWEEN ? AND ?
                GROUP BY p.TenPhim
                ORDER BY DoanhThu DESC""", rs -> kq.put(rs.getString("TenPhim"), rs.getBigDecimal("DoanhThu")), tu, den);
        return kq;
    }

    @Override
    public Map<LocalDate, BigDecimal> doanhThuTheoNgay(LocalDate tu, LocalDate den) {
        Map<LocalDate, BigDecimal> kq = new TreeMap<>();
        query("""
                SELECT CAST(NgayThanhToan AS DATE) AS Ngay, SUM(SoTien) AS DoanhThu
                FROM ThanhToan
                WHERE TrangThai = 'THANH_CONG' AND CAST(NgayThanhToan AS DATE) BETWEEN ? AND ?
                GROUP BY CAST(NgayThanhToan AS DATE)""",
                rs -> kq.put(JdbcHelper.ngay(rs, "Ngay"), rs.getBigDecimal("DoanhThu")), tu, den);
        return kq;
    }
}


// ==========================================================================================
// TimKiemDAOImpl   (truoc day: src/main/java/com/nhom/datve/dao/impl/TimKiemDAOImpl.java)
// ==========================================================================================

/** Tim kiem suat chieu bang JDBC, ho tro 2 che do de demo Dynamic SQL tren giao dien. */
class TimKiemDAOImpl implements TimKiemDAO {

    @Override
    public KetQuaTimKiem timSuatChieu(BoLocTimKiem loc, boolean anToan) {
        return anToan ? timAnToan(loc) : timNoiChuoi(loc);
    }

    /** Dung: cau truc SQL tao luc chay, gia tri gui rieng qua tham so ?. */
    private KetQuaTimKiem timAnToan(BoLocTimKiem loc) {
        TimKiemSql.CauLenh cl = TimKiemSql.taoAnToan(loc);
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(cl.sql())) {
            for (int i = 0; i < cl.thamSo().size(); i++) {
                ps.setObject(i + 1, cl.thamSo().get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return new KetQuaTimKiem(doc(rs), cl.sql(), cl.thamSo(), true, null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi CSDL khi tìm kiếm", e);
        }
    }

    /** SAI CO CHU Y: cong chuoi nguoi dung nhap vao SQL roi chay bang Statement. */
    private KetQuaTimKiem timNoiChuoi(BoLocTimKiem loc) {
        String sql = TimKiemSql.taoNoiChuoi(loc);
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return new KetQuaTimKiem(doc(rs), sql, List.of(), false, null);
        } catch (SQLException e) {
            // Vd go ten phim co dau ' -> cau SQL hong. Tra ve loi de man hinh hien cho nguoi xem demo.
            return new KetQuaTimKiem(List.of(), sql, List.of(), false, "SQL Server báo lỗi: " + e.getMessage());
        }
    }

    private static List<SuatChieu> doc(ResultSet rs) throws SQLException {
        List<SuatChieu> ds = new ArrayList<>();
        while (rs.next()) {
            SuatChieu s = new SuatChieu();
            s.setMaSuat(rs.getInt(1));
            s.setTenPhim(rs.getString(2));
            s.setTheLoai(rs.getString(3));
            Timestamp t = rs.getTimestamp(4);
            s.setGioBatDau(t == null ? null : t.toLocalDateTime());
            s.setTenPhong(rs.getString(5));
            s.setGiaCoBan(rs.getBigDecimal(6));
            ds.add(s);
        }
        return ds;
    }
}


// ==========================================================================================
// VeDAOImpl   (truoc day: src/main/java/com/nhom/datve/dao/impl/VeDAOImpl.java)
// ==========================================================================================

/** VeDAO bang JDBC (bang Ve). */
class VeDAOImpl implements VeDAO {

    @Override
    public Set<Integer> findMaGheDaDat(int maSuat) {
        return new HashSet<>(query("SELECT MaGhe FROM Ve WHERE MaSuat = ? AND TrangThai <> 'DA_HUY'",
                rs -> rs.getInt("MaGhe"), maSuat));
    }

    @Override
    public List<Ve> findByDatVe(int maDatVe) {
        return query("""
                SELECT v.MaVe, v.MaDatVe, v.MaSuat, v.MaGhe, v.GiaVe, v.TrangThai,
                       g.Hang, g.SoGhe, p.TenPhim, pc.TenPhong, sc.GioBatDau
                FROM Ve v
                     JOIN Ghe g         ON g.MaGhe = v.MaGhe
                     JOIN SuatChieu sc  ON sc.MaSuat = v.MaSuat
                     JOIN Phim p        ON p.MaPhim = sc.MaPhim
                     JOIN PhongChieu pc ON pc.MaPhong = sc.MaPhong
                WHERE v.MaDatVe = ? ORDER BY g.Hang, g.SoGhe""", rs -> {
                    Ve v = new Ve(rs.getInt("MaVe"), rs.getInt("MaDatVe"), rs.getInt("MaSuat"),
                            rs.getInt("MaGhe"), rs.getBigDecimal("GiaVe"));
                    v.setTrangThai(rs.getString("TrangThai"));
                    v.setTenGhe(rs.getString("Hang").trim() + rs.getInt("SoGhe"));
                    v.setTenPhim(rs.getString("TenPhim"));
                    v.setTenPhong(rs.getString("TenPhong"));
                    v.setGioBatDau(JdbcHelper.thoiGian(rs, "GioBatDau"));
                    return v;
                }, maDatVe);
    }
}
