package com.nhom.datve;

import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/*
 * Main.java: Diem khoi chay + tien ich (cau hinh, ket noi CSDL, phien dang nhap, kiem tra du lieu) + exception.
 * Gom cac lop: Main, AppConfig, CheDoChay, DBConnection, TuCauHinh, PasswordUtil, PhienDangNhap, Validator, DataAccessException, NghiepVuException
 */

// ==========================================================================================
// Main   (truoc day: src/main/java/com/nhom/datve/Main.java)
// ==========================================================================================

/** Diem khoi chay ung dung. */
public final class Main {
    /** Lop dang nhap cua Trung. Chua co thi dung DangNhapTamDialog. */
    private static final String LOP_DANG_NHAP = "com.nhom.datve.LoginFrame";

    private Main() { }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(Theme::apDung);
        // Ban exe: lan dau chay (hoac mat ket noi) thi tu cau hinh SQL Server de chay JDBC.
        TuCauHinh.chayNeuCan();
        // Kiem tra che do MOCK/JDBC truoc khi mo giao dien (thu ket noi co the mat vai giay).
        CheDoChay.kiemTra();
        SwingUtilities.invokeLater(() -> {
            if (CheDoChay.isLoiKetNoi()) {
                JOptionPane.showMessageDialog(null,
                        CheDoChay.dongThongBao() + "\n\n" + CheDoChay.lyDo(),
                        "Không kết nối được SQL Server", JOptionPane.WARNING_MESSAGE);
            }
            moManHinhDangNhap();
        });
    }

    /**
     * Mo man hinh dang nhap (luc khoi dong va sau khi dang xuat).
     * LoginFrame khi dang nhap thanh cong chi can: new MainFrame(phien).setVisible(true); dispose();
     */
    public static void moManHinhDangNhap() {
        try {
            Window login = (Window) Class.forName(LOP_DANG_NHAP).getDeclaredConstructor().newInstance();
            login.setVisible(true);
        } catch (ClassNotFoundException chuaCo) {
            DangNhapTamDialog.hienThi().ifPresentOrElse(
                    phien -> new MainFrame(phien).setVisible(true),
                    () -> System.exit(0));
        } catch (ReflectiveOperationException e) {
            JOptionPane.showMessageDialog(null, "Không mở được màn hình đăng nhập: " + e, "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}


// ==========================================================================================
// AppConfig   (truoc day: src/main/java/com/nhom/datve/util/AppConfig.java)
// ==========================================================================================

/**
 * Doc file cau hinh db.properties.
 *  - Chay bang mvn / IDE: doc src/main/resources/db.properties (tren classpath).
 *  - Chay bang file exe (co -Ddatve.exe=true): doc %APPDATA%\DatVeXemPhim\db.properties,
 *    file nay do app tu tao lan dau qua script tu_cau_hinh.ps1 (xem TuCauHinh).
 * Neu khong co file -> chay che do mock (du lieu gia) de code giao dien khong can SQL Server.
 */
final class AppConfig {
    private static final Properties PROPS = new Properties();
    private static volatile boolean coFile;
    private static volatile String nguonCauHinh;

    static {
        reload();
    }

    private AppConfig() { }

    /** true neu app dang chay tu file exe dong goi bang jpackage. */
    public static boolean isChayTuExe() {
        return Boolean.getBoolean("datve.exe");
    }

    /** File cau hinh rieng cua ban exe (nam ngoai file exe, khong bi dong goi kem mat khau). */
    public static Path fileCauHinhExe() {
        String appData = System.getenv("APPDATA");
        Path goc = appData != null ? Paths.get(appData) : Paths.get(System.getProperty("user.home"));
        return goc.resolve("DatVeXemPhim").resolve("db.properties");
    }

    /** Doc lai cau hinh (dung sau khi script tu cau hinh vua ghi file moi). */
    public static synchronized void reload() {
        PROPS.clear();
        coFile = false;
        nguonCauHinh = null;
        if (isChayTuExe()) {
            Path f = fileCauHinhExe();
            if (Files.isRegularFile(f)) {
                try (InputStream in = Files.newInputStream(f)) {
                    PROPS.load(in);
                    coFile = true;
                    nguonCauHinh = f.toString();
                } catch (IOException e) {
                    System.err.println("Khong doc duoc " + f + ": " + e.getMessage());
                }
            }
            return;
        }
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                PROPS.load(in);
                coFile = true;
                nguonCauHinh = "src/main/resources/db.properties";
            }
        } catch (IOException e) {
            System.err.println("Khong doc duoc db.properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return PROPS.getProperty(key);
    }

    /** true neu co file cau hinh. */
    public static boolean coFileCauHinh() {
        return coFile;
    }

    /** Duong dan file cau hinh dang dung (null neu khong co). */
    public static String nguonCauHinh() {
        return nguonCauHinh;
    }

    /** true neu chay voi du lieu gia. Mac dinh la mock khi chua co db.properties. */
    public static boolean isMockMode() {
        return !coFile || !"jdbc".equalsIgnoreCase(PROPS.getProperty("app.mode", "mock").trim());
    }

    public static void setMockMode(boolean mock) {
        PROPS.setProperty("app.mode", mock ? "mock" : "jdbc");
    }
}


// ==========================================================================================
// CheDoChay   (truoc day: src/main/java/com/nhom/datve/util/CheDoChay.java)
// ==========================================================================================

/**
 * Cho biet app dang chay che do nao (MOCK hay JDBC) va vi sao, de hien thong bao cho nguoi dung.
 *
 * Goi kiemTra() 1 lan luc khoi dong (o luong main, KHONG goi tren luong giao dien vi thu ket noi
 * SQL Server co the mat vai giay). Sau do giao dien chi doc ket qua qua dongThongBao().
 */
final class CheDoChay {
    private static volatile boolean daKiemTra;
    private static volatile boolean ketNoiDuoc;

    private CheDoChay() { }

    /** Thu ket noi (neu o che do jdbc) va in dong thong bao ra console. */
    public static synchronized void kiemTra() {
        if (!AppConfig.isMockMode()) {
            try {
                ketNoiDuoc = DBConnection.getInstance().testConnection();
            } catch (RuntimeException e) {
                System.err.println("Cau hinh ket noi loi: " + e.getMessage());
                ketNoiDuoc = false;
            }
        }
        daKiemTra = true;
        System.out.println("==================================================");
        System.out.println(" " + boDau(dongThongBao()));
        System.out.println(" Ly do: " + boDau(lyDo()));
        System.out.println("==================================================");
    }

    public static boolean isMock() {
        return AppConfig.isMockMode();
    }

    /** true neu dang o che do jdbc nhung khong ket noi duoc SQL Server. */
    public static boolean isLoiKetNoi() {
        return !isMock() && daKiemTra && !ketNoiDuoc;
    }

    /** Dong thong bao ngan, dung cho nhan tren giao dien. */
    public static String dongThongBao() {
        if (isMock()) {
            return "Chế độ MOCK – dữ liệu giả trong RAM, KHÔNG lưu vào SQL Server";
        }
        if (!daKiemTra) {
            return "Chế độ JDBC – SQL Server (" + tenDatabase() + ")";
        }
        return ketNoiDuoc
                ? "Chế độ JDBC – đã kết nối SQL Server (" + tenDatabase() + ")"
                : "Chế độ JDBC – KHÔNG kết nối được SQL Server (" + tenDatabase() + ")";
    }

    /** Giai thich vi sao app dang o che do hien tai. */
    public static String lyDo() {
        if (!AppConfig.coFileCauHinh()) {
            return AppConfig.isChayTuExe()
                    ? "Chưa có cấu hình " + AppConfig.fileCauHinhExe() + " nên app chạy dữ liệu giả."
                    : "Không tìm thấy file db.properties nên app tự chạy dữ liệu giả.";
        }
        String file = AppConfig.nguonCauHinh();
        if (isMock()) {
            return "Trong " + file + " đang đặt app.mode=" + AppConfig.get("app.mode")
                    + " (muốn dùng SQL Server hãy đặt app.mode=jdbc).";
        }
        if (isLoiKetNoi()) {
            return "app.mode=jdbc nhưng không kết nối được. Kiểm tra SQL Server đã bật, cổng 1433, "
                    + "user/mật khẩu trong " + file + ".";
        }
        return "app.mode=jdbc trong " + file + " – dữ liệu được đọc/ghi vào SQL Server.";
    }

    private static String tenDatabase() {
        String url = AppConfig.get("db.url");
        if (url != null) {
            Matcher m = Pattern.compile("databaseName=([^;]+)", Pattern.CASE_INSENSITIVE).matcher(url);
            if (m.find()) {
                return m.group(1);
            }
        }
        return "chưa cấu hình db.url";
    }

    /** Console Windows hay loi font tieng Viet nen bo dau khi in ra console. */
    private static String boDau(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .replace('–', '-');
    }
}


// ==========================================================================================
// DBConnection   (truoc day: src/main/java/com/nhom/datve/util/DBConnection.java)
// ==========================================================================================

/**
 * Lop ket noi CSDL theo mau Singleton: ca chuong trinh chi co DUY NHAT 1 doi tuong DBConnection,
 * giu thong tin ket noi (url, user, password) doc 1 lan tu db.properties.
 *
 * Moi lan goi getConnection() tra ve 1 Connection moi; noi goi phai dong bang try-with-resources:
 * <pre>
 * try (Connection conn = DBConnection.getInstance().getConnection();
 *      PreparedStatement ps = conn.prepareStatement("SELECT ...")) { ... }
 * </pre>
 * Khong dung chung 1 Connection cho ca app vi 2 thao tac chay cung luc (vd 2 cua so) co the
 * lam roi transaction cua nhau.
 */
final class DBConnection {
    private static volatile DBConnection instance;

    private final String url;
    private final String user;
    private final String password;

    /** Constructor private: ben ngoai khong the new DBConnection(). */
    private DBConnection() {
        this.url = AppConfig.get("db.url");
        this.user = AppConfig.get("db.user");
        this.password = AppConfig.get("db.password");
        if (url == null) {
            throw new IllegalStateException(
                "Chua cau hinh db.url. Hay chay database\\tu_cau_hinh.ps1 (xem README).");
        }
    }

    /** Lay doi tuong duy nhat (double-checked locking, an toan khi nhieu luong). */
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    /** Bo doi tuong cu de lan getInstance() sau doc lai cau hinh (sau khi AppConfig.reload()). */
    public static synchronized void reset() {
        instance = null;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /** Thu ket noi, dung cho man hinh khoi dong hoac kiem tra cau hinh. */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(3);
        } catch (SQLException e) {
            System.err.println("Ket noi that bai: " + e.getMessage());
            return false;
        }
    }
}


// ==========================================================================================
// TuCauHinh   (truoc day: src/main/java/com/nhom/datve/util/TuCauHinh.java)
// ==========================================================================================

/**
 * Tu cau hinh SQL Server khi chay bang file exe (chi dung khi AppConfig.isChayTuExe()).
 *
 * Lan dau mo exe (chua co %APPDATA%\DatVeXemPhim\db.properties) hoac khi khong ket noi duoc,
 * app hoi nguoi dung roi chay database/tu_cau_hinh.ps1 voi quyen Administrator (hien hop UAC).
 * Script bat TCP/IP cong 1433, tao database tu cac file .sql neu chua co, tao login datve_exe
 * va ghi file cau hinh voi app.mode=jdbc. Xong thi app doc lai cau hinh va chay che do JDBC.
 *
 * Goi o luong main (khong phai luong giao dien) vi script chay mat vai chuc giay.
 */
final class TuCauHinh {
    private TuCauHinh() { }

    /**
     * true khi mo bang run.exe (-Ddatve.tudong=true): luon chay JDBC, tu cau hinh SQL Server
     * ma khong hoi lai (chi con hop UAC cua Windows lan dau).
     */
    public static boolean isTuDong() {
        return Boolean.getBoolean("datve.tudong");
    }

    public static void chayNeuCan() {
        if (!AppConfig.isChayTuExe()) {
            return;
        }
        if (AppConfig.coFileCauHinh() && AppConfig.isMockMode() && !isTuDong()) {
            return; // nguoi dung tu dat app.mode=mock trong file cau hinh -> ton trong lua chon do
        }
        if (AppConfig.coFileCauHinh() && !AppConfig.isMockMode() && thuKetNoi()) {
            return; // da cau hinh JDBC va ket noi duoc
        }

        String loiMo = AppConfig.coFileCauHinh()
                ? "Không kết nối được SQL Server với cấu hình hiện tại."
                : "Đây là lần đầu chạy ứng dụng trên máy này.";
        int chon = isTuDong() ? JOptionPane.YES_OPTION : JOptionPane.showConfirmDialog(null,
                loiMo + "\n\nỨng dụng sẽ tự cấu hình SQL Server để chạy chế độ JDBC:\n"
                        + "  • bật kết nối TCP/IP cổng 1433\n"
                        + "  • tạo database DatVeXemPhim (nếu chưa có)\n"
                        + "  • tạo tài khoản kết nối và lưu cấu hình\n\n"
                        + "Cần quyền Administrator (Windows sẽ hỏi xác nhận).\n"
                        + "Chọn No để chạy bằng dữ liệu giả.",
                "Cấu hình SQL Server", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) {
            return;
        }

        Path thuMucApp = Paths.get(System.getProperty("datve.appdir", "."));
        Path script = thuMucApp.resolve("database").resolve("tu_cau_hinh.ps1");
        if (!Files.isRegularFile(script)) {
            baoLoi("Không tìm thấy script cấu hình:\n" + script);
            return;
        }
        Path fileCauHinh = AppConfig.fileCauHinhExe();
        Path fileLog = fileCauHinh.resolveSibling("cau_hinh.log");

        int maThoat;
        try {
            maThoat = chayVoiQuyenAdmin(script, fileCauHinh, thuMucApp.resolve("database"));
        } catch (IOException e) {
            baoLoi("Không chạy được PowerShell: " + e.getMessage());
            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        AppConfig.reload();
        DBConnection.reset();
        DAOFactory.reset();
        if (maThoat != 0 || !AppConfig.coFileCauHinh() || !thuKetNoi()) {
            baoLoi("Cấu hình SQL Server chưa thành công (mã " + maThoat + ").\n\n"
                    + cuoiLog(fileLog) + "\n\nNhật ký đầy đủ: " + fileLog
                    + "\nỨng dụng sẽ chạy bằng dữ liệu giả.");
        }
    }

    private static boolean thuKetNoi() {
        try {
            DBConnection.reset();
            return DBConnection.getInstance().testConnection();
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Chay script bang PowerShell voi Start-Process -Verb RunAs (hien hop UAC), cho den khi xong. */
    private static int chayVoiQuyenAdmin(Path script, Path fileCauHinh, Path thuMucSql)
            throws IOException, InterruptedException {
        String lenh = "try { $p = Start-Process -FilePath powershell.exe -Verb RunAs -Wait -PassThru"
                + " -ArgumentList @('-NoProfile','-ExecutionPolicy','Bypass','-File',"
                + thamSo(script) + ",'-ConfigPath'," + thamSo(fileCauHinh)
                + ",'-SqlDir'," + thamSo(thuMucSql) + "); exit $p.ExitCode }"
                + " catch { Write-Error $_; exit 99 }";
        Process p = new ProcessBuilder("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass",
                "-Command", lenh)
                .redirectErrorStream(true)
                .start();
        p.getInputStream().transferTo(System.out);
        return p.waitFor();
    }

    /** Start-Process noi cac tham so bang dau cach, nen duong dan co dau cach phai boc trong "...". */
    private static String thamSo(Path duongDan) {
        return "'\"" + duongDan.toAbsolutePath().toString().replace("'", "''") + "\"'";
    }

    private static String cuoiLog(Path fileLog) {
        try {
            // Doc kieu "de dai" (khong nem loi khi gap byte khong phai UTF-8 tu sqlcmd)
            List<String> dong = new String(Files.readAllBytes(fileLog), StandardCharsets.UTF_8)
                    .lines().filter(s -> !s.isBlank()).toList();
            return String.join("\n", dong.subList(Math.max(0, dong.size() - 8), dong.size()));
        } catch (IOException | RuntimeException e) {
            return "(Không đọc được nhật ký – có thể bạn đã từ chối quyền Administrator.)";
        }
    }

    private static void baoLoi(String thongBao) {
        JOptionPane.showMessageDialog(null, thongBao, "Cấu hình SQL Server", JOptionPane.WARNING_MESSAGE);
    }
}


// ==========================================================================================
// PasswordUtil   (truoc day: src/main/java/com/nhom/datve/util/PasswordUtil.java)
// ==========================================================================================

/** Bam mat khau SHA-256 truoc khi luu/so sanh voi cot TaiKhoan.MatKhau. */
final class PasswordUtil {
    private PasswordUtil() { }

    public static String hash(String matKhau) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(matKhau.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean khop(String matKhauNhap, String matKhauDaBam) {
        return hash(matKhauNhap).equalsIgnoreCase(matKhauDaBam);
    }
}


// ==========================================================================================
// PhienDangNhap   (truoc day: src/main/java/com/nhom/datve/util/PhienDangNhap.java)
// ==========================================================================================

/**
 * Thong tin nguoi dang dang nhap. Tao boi TaiKhoanService.dangNhap(), truyen vao MainFrame.
 * Dung getMaKH() khi dat ve, isAdmin() de an/hien chuc nang quan tri.
 */
final class PhienDangNhap {
    private final TaiKhoan taiKhoan;
    private final KhachHang khachHang;   // null neu la nhan vien
    private final NhanVien nhanVien;     // null neu la khach hang

    public PhienDangNhap(TaiKhoan taiKhoan, KhachHang khachHang, NhanVien nhanVien) {
        this.taiKhoan = taiKhoan;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
    }

    public boolean isAdmin() { return taiKhoan.getVaiTro() == VaiTro.NHANVIEN; }

    public String getTenHienThi() {
        if (khachHang != null) {
            return khachHang.getHoTen();
        }
        return nhanVien != null ? nhanVien.getHoTen() : taiKhoan.getTenDangNhap();
    }

    /** Ma khach hang dung khi dat ve; nem loi neu nguoi dang nhap khong phai khach hang. */
    public int getMaKH() {
        if (khachHang == null) {
            throw new IllegalStateException("Tài khoản đang đăng nhập không phải khách hàng.");
        }
        return khachHang.getMaKH();
    }

    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public KhachHang getKhachHang() { return khachHang; }
    public NhanVien getNhanVien() { return nhanVien; }
}


// ==========================================================================================
// Validator   (truoc day: src/main/java/com/nhom/datve/util/Validator.java)
// ==========================================================================================

/**
 * Kiem tra du lieu nhap tren cac man hinh giao dien.
 * Neu du lieu sai se nem {@link NghiepVuException} voi thong bao tieng Viet ro rang
 * de man hinh hien JOptionPane ma khong vang loi he thong.
 */
final class Validator {
    private static final String TEN_DANG_NHAP_REGEX = "^[a-zA-Z0-9_]{3,30}$";
    private static final String SO_DIEN_THOAI_REGEX = "^(0|\\+84)[0-9]{9}$";
    private static final String EMAIL_REGEX = "^[\\w.+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$";

    private Validator() { }

    public static String batBuocNhap(String giaTri, String tenTruong) throws NghiepVuException {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            throw new NghiepVuException("Vui lòng nhập " + tenTruong + ".");
        }
        return giaTri.trim();
    }

    public static String kiemTraTenDangNhap(String tenDangNhap) throws NghiepVuException {
        String s = batBuocNhap(tenDangNhap, "tên đăng nhập");
        if (!s.matches(TEN_DANG_NHAP_REGEX)) {
            throw new NghiepVuException("Tên đăng nhập phải từ 3–30 ký tự, chỉ gồm chữ cái không dấu, chữ số hoặc dấu gạch dưới (_).");
        }
        return s;
    }

    public static String kiemTraMatKhau(String matKhau, String xacNhanMatKhau) throws NghiepVuException {
        if (matKhau == null || matKhau.isEmpty()) {
            throw new NghiepVuException("Vui lòng nhập mật khẩu.");
        }
        if (matKhau.length() < 6) {
            throw new NghiepVuException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
        if (xacNhanMatKhau != null && !matKhau.equals(xacNhanMatKhau)) {
            throw new NghiepVuException("Mật khẩu xác nhận không khớp.");
        }
        return matKhau;
    }

    public static String kiemTraHoTen(String hoTen) throws NghiepVuException {
        String s = batBuocNhap(hoTen, "họ và tên");
        if (s.length() < 2 || s.length() > 100) {
            throw new NghiepVuException("Họ và tên phải từ 2 đến 100 ký tự.");
        }
        return s;
    }

    public static String kiemTraSoDienThoai(String soDienThoai) throws NghiepVuException {
        String s = batBuocNhap(soDienThoai, "số điện thoại");
        if (!s.matches(SO_DIEN_THOAI_REGEX)) {
            throw new NghiepVuException("Số điện thoại không hợp lệ (gồm 10 chữ số, bắt đầu bằng 0 hoặc +84).");
        }
        return s;
    }

    public static String kiemTraEmail(String email) throws NghiepVuException {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        String s = email.trim();
        if (!s.matches(EMAIL_REGEX)) {
            throw new NghiepVuException("Địa chỉ email không hợp lệ.");
        }
        return s;
    }
}


// ==========================================================================================
// DataAccessException   (truoc day: src/main/java/com/nhom/datve/exception/DataAccessException.java)
// ==========================================================================================

/**
 * Boc SQLException thanh loi unchecked de tang service/UI khong phai try-catch SQLException
 * o moi cho. Tang DAO bat SQLException roi nem loi nay kem nguyen nhan goc.
 */
class DataAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}


// ==========================================================================================
// NghiepVuException   (truoc day: src/main/java/com/nhom/datve/exception/NghiepVuException.java)
// ==========================================================================================

/**
 * Loi nghiep vu (ghe da co nguoi dat, suat chieu da bat dau...).
 * Thong bao trong loi nay viet bang tieng Viet de UI hien thang cho nguoi dung.
 */
class NghiepVuException extends Exception {
    private static final long serialVersionUID = 1L;

    public NghiepVuException(String message) {
        super(message);
    }
}
