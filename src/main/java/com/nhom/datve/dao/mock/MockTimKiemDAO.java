package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.TimKiemDAO;
import com.nhom.datve.dao.TimKiemSql;
import com.nhom.datve.model.BoLocTimKiem;
import com.nhom.datve.model.KetQuaTimKiem;
import com.nhom.datve.model.Phim;
import com.nhom.datve.model.SuatChieu;
import com.nhom.datve.model.TheLoai;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Tim kiem tren du lieu gia. Van hien cau SQL se duoc tao, nhung khong chay SQL that. */
public class MockTimKiemDAO implements TimKiemDAO {

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
