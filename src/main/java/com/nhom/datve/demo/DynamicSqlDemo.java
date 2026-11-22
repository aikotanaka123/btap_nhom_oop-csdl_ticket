package com.nhom.datve.demo;

import com.nhom.datve.util.AppConfig;
import com.nhom.datve.util.DBConnection;
import com.nhom.datve.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Demo muc 10.2 (Dynamic SQL): cau lenh SQL duoc ghep/tao ra LUC CHAY.
 *
 *  Phan 1. Ghep chuoi nguoi dung nhap vao cau SQL -> bi SQL injection (dang nhap khong can mat khau).
 *  Phan 2. Cung du lieu do nhung dung PreparedStatement (tham so ?) -> an toan.
 *  Phan 3. SQL dong DUNG CACH: cau WHERE thay doi theo bo loc nhung gia tri van truyen qua ?.
 *
 * Chay: mvn -q compile exec:java -Pdemo
 * Chua co SQL Server (che do mock) thi demo chi in ra cau SQL duoc tao de giai thich.
 */
public final class DynamicSqlDemo {

    /** Nguoi dung go vao o "Ten dang nhap". Dau ' dong chuoi, -- bien phan con lai thanh chu thich. */
    static final String TEN_DANG_NHAP_DOC_HAI = "admin' --";
    static final String MAT_KHAU_BAT_KY = "sai_mat_khau";

    private DynamicSqlDemo() { }

    public static void main(String[] args) throws SQLException {
        boolean coCSDL = !AppConfig.isMockMode();
        System.out.println("=== DEMO DYNAMIC SQL & SQL INJECTION === (" + (coCSDL ? "SQL Server" : "khong ket noi CSDL") + ")\n");
        phan1KhongAnToan(coCSDL);
        phan2PreparedStatement(coCSDL);
        phan3TimKiemDong(coCSDL, "nhà", null, "T18");
    }

    /** SAI: noi chuoi truc tiep. */
    static String taoSqlDangNhapNoiChuoi(String tenDangNhap, String matKhau) {
        return "SELECT MaTK, TenDangNhap, VaiTro FROM TaiKhoan "
                + "WHERE TenDangNhap = '" + tenDangNhap + "' AND MatKhau = '" + PasswordUtil.hash(matKhau) + "'";
    }

    static void phan1KhongAnToan(boolean coCSDL) throws SQLException {
        System.out.println("--- Phan 1: Statement + noi chuoi (KHONG an toan) ---");
        String sql = taoSqlDangNhapNoiChuoi(TEN_DANG_NHAP_DOC_HAI, MAT_KHAU_BAT_KY);
        System.out.println("Nhap: ten = [" + TEN_DANG_NHAP_DOC_HAI + "], mat khau = [" + MAT_KHAU_BAT_KY + "]");
        System.out.println("SQL thuc te gui len server:\n  " + sql);
        System.out.println("=> Phan sau dau -- bi bien thanh chu thich, dieu kien MatKhau bien mat.");
        if (coCSDL) {
            try (Connection conn = DBConnection.getInstance().getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                System.out.println(rs.next()
                        ? "KET QUA: DANG NHAP THANH CONG vao tai khoan '" + rs.getString("TenDangNhap")
                          + "' (vai tro " + rs.getString("VaiTro") + ") ma KHONG CAN MAT KHAU!"
                        : "KET QUA: khong co tai khoan 'admin' trong du lieu mau.");
            }
        }
        System.out.println();
    }

    static void phan2PreparedStatement(boolean coCSDL) throws SQLException {
        System.out.println("--- Phan 2: PreparedStatement (an toan) ---");
        String sql = "SELECT MaTK, TenDangNhap, VaiTro FROM TaiKhoan WHERE TenDangNhap = ? AND MatKhau = ?";
        System.out.println("SQL co dinh, du lieu gui rieng:\n  " + sql);
        System.out.println("  ?1 = \"" + TEN_DANG_NHAP_DOC_HAI + "\"  (ca chuoi, ke ca dau ' va --, chi la DU LIEU)");
        if (coCSDL) {
            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, TEN_DANG_NHAP_DOC_HAI);
                ps.setString(2, PasswordUtil.hash(MAT_KHAU_BAT_KY));
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println(rs.next() ? "KET QUA: dang nhap duoc (khong mong doi!)"
                                                 : "KET QUA: dang nhap THAT BAI - tan cong khong co tac dung.");
                }
            }
        }
        System.out.println();
    }

    /** Ket qua tao cau SQL dong: chuoi SQL + danh sach gia tri tham so theo dung thu tu dau ?. */
    record CauTruyVan(String sql, List<Object> thamSo) { }

    /** Bo loc nao null thi khong them dieu kien do. Chi CAU TRUC cau lenh thay doi, gia tri luon qua ?. */
    static CauTruyVan taoTruyVanTimPhim(String tuKhoa, Integer maTL, String doTuoi) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT p.MaPhim, p.TenPhim, p.DoTuoi FROM Phim p");
        List<Object> thamSo = new ArrayList<>();
        if (maTL != null) {
            sql.append(" JOIN Phim_TheLoai pt ON pt.MaPhim = p.MaPhim");
        }
        sql.append(" WHERE 1 = 1");
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            sql.append(" AND p.TenPhim LIKE ?");
            thamSo.add("%" + tuKhoa.trim() + "%");
        }
        if (maTL != null) {
            sql.append(" AND pt.MaTL = ?");
            thamSo.add(maTL);
        }
        if (doTuoi != null) {
            sql.append(" AND p.DoTuoi = ?");
            thamSo.add(doTuoi);
        }
        sql.append(" ORDER BY p.TenPhim");
        return new CauTruyVan(sql.toString(), thamSo);
    }

    static void phan3TimKiemDong(boolean coCSDL, String tuKhoa, Integer maTL, String doTuoi) throws SQLException {
        System.out.println("--- Phan 3: SQL dong dung cach (tim phim theo bo loc tuy chon) ---");
        CauTruyVan q = taoTruyVanTimPhim(tuKhoa, maTL, doTuoi);
        System.out.println("Bo loc: tuKhoa=" + tuKhoa + ", maTL=" + maTL + ", doTuoi=" + doTuoi);
        System.out.println("SQL tao luc chay:\n  " + q.sql());
        System.out.println("Tham so: " + q.thamSo());
        if (coCSDL) {
            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(q.sql())) {
                for (int i = 0; i < q.thamSo().size(); i++) {
                    ps.setObject(i + 1, q.thamSo().get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    int n = 0;
                    while (rs.next()) {
                        System.out.println("  - " + rs.getString("TenPhim") + " (" + rs.getString("DoTuoi") + ")");
                        n++;
                    }
                    System.out.println("Tim thay " + n + " phim.");
                }
            }
        }
    }
}
