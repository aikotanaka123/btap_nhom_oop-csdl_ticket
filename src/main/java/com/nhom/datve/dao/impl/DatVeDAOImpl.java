package com.nhom.datve.dao.impl;

import com.nhom.datve.dao.DatVeDAO;
import com.nhom.datve.exception.DataAccessException;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.DatVe;
import com.nhom.datve.model.PhuongThucThanhToan;
import com.nhom.datve.model.TrangThaiDatVe;
import com.nhom.datve.model.Ve;
import com.nhom.datve.util.DBConnection;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Cai dat DatVeDAO bang JDBC.
 *  - datVe(): goi stored procedure sp_DatVe bang CallableStatement (muc 10.3 + 10.4 cua chuong 10).
 *  - huy(): tu quan ly transaction o phia Java (setAutoCommit(false) / commit / rollback).
 */
public class DatVeDAOImpl implements DatVeDAO {

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
