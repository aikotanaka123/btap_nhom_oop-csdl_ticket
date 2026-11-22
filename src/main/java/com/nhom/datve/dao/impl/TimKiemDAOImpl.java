package com.nhom.datve.dao.impl;

import com.nhom.datve.dao.TimKiemDAO;
import com.nhom.datve.dao.TimKiemSql;
import com.nhom.datve.exception.DataAccessException;
import com.nhom.datve.model.BoLocTimKiem;
import com.nhom.datve.model.KetQuaTimKiem;
import com.nhom.datve.model.SuatChieu;
import com.nhom.datve.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Tim kiem suat chieu bang JDBC, ho tro 2 che do de demo Dynamic SQL tren giao dien. */
public class TimKiemDAOImpl implements TimKiemDAO {

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
