package com.nhom.datve.dao;

import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.DatVe;
import com.nhom.datve.model.PhuongThucThanhToan;
import java.util.List;
import java.util.Optional;

public interface DatVeDAO {
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
