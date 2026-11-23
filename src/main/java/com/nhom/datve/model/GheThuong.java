package com.nhom.datve.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Ghe thuong - he so gia 1.00 (100% gia co ban).
 * Ke thua {@link Ghe} va override {@link #tinhGiaVe(BigDecimal)}.
 */
public class GheThuong extends Ghe {
    public static final BigDecimal HE_SO_THUONG = new BigDecimal("1.00");

    public GheThuong() {
        super();
    }

    public GheThuong(int maGhe, int maPhong, LoaiGhe loaiGhe, char hang, int soGhe) {
        super(maGhe, maPhong,
              loaiGhe != null ? loaiGhe : new LoaiGhe(1, "Thường", HE_SO_THUONG),
              hang, soGhe);
    }

    @Override
    protected BigDecimal getHeSoMacDinh() {
        return HE_SO_THUONG;
    }

    @Override
    public BigDecimal tinhGiaVe(BigDecimal giaCoBan) {
        if (giaCoBan == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal heSo = (getLoaiGhe() != null && getLoaiGhe().getHeSoGia() != null)
                ? getLoaiGhe().getHeSoGia()
                : HE_SO_THUONG;
        return giaCoBan.multiply(heSo).setScale(0, RoundingMode.HALF_UP);
    }
}
