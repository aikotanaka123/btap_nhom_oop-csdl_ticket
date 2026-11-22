package com.nhom.datve.dao;

import com.nhom.datve.model.BoLocTimKiem;
import java.util.ArrayList;
import java.util.List;

/**
 * Tao cau SQL tim suat chieu LUC CHAY tu bo loc (Dynamic SQL, muc 10.2.3).
 * Hai cach tao cung mot cau truy van:
 *  - taoAnToan(): gia tri di qua dau ?, gui rieng cho PreparedStatement.
 *  - taoNoiChuoi(): CONG CHUOI gia tri vao SQL -> bi SQL injection. CHI DUNG DE DEMO.
 *
 * Cau lenh viet tren MOT dong de thay ro tac dung cua "--" khi bi tan cong.
 * Cot TheLoai dung STRING_AGG (SQL Server 2017 tro len).
 */
public final class TimKiemSql {

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
