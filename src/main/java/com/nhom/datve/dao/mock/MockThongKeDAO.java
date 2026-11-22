package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.ThongKeDAO;
import com.nhom.datve.model.ThanhToan;
import com.nhom.datve.model.Ve;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/** Thong ke gia: cong tien cac ve con hieu luc. */
public class MockThongKeDAO implements ThongKeDAO {

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
