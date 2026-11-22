package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.DatVeDAO;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.DatVe;
import com.nhom.datve.model.Ghe;
import com.nhom.datve.model.PhuongThucThanhToan;
import com.nhom.datve.model.SuatChieu;
import com.nhom.datve.model.ThanhToan;
import com.nhom.datve.model.TrangThaiDatVe;
import com.nhom.datve.model.Ve;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** DAO gia cho DatVe: mo phong lai dung nhung gi sp_DatVe lam (kiem tra ghe trong roi tao ve). */
public class MockDatVeDAO implements DatVeDAO {

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
