package com.nhom.datve.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Ghe Doi (Sweetbox 2 nguoi) - he so gia 2.00 (200% gia co ban).
 * Ke thua {@link Ghe} va override {@link #tinhGiaVe(BigDecimal)}.
 */
public class GheDoi extends Ghe {
    public static final BigDecimal HE_SO_DOI = new BigDecimal("2.00");

    public GheDoi() {
        super();
    }

    public GheDoi(int maGhe, int maPhong, LoaiGhe loaiGhe, char hang, int soGhe) {
        super(maGhe, maPhong,
              loaiGhe != null ? loaiGhe : new LoaiGhe(3, "Đôi", HE_SO_DOI),
              hang, soGhe);
    }

    @Override
    protected BigDecimal getHeSoMacDinh() {
        return HE_SO_DOI;
    }

    @Override
    public BigDecimal tinhGiaVe(BigDecimal giaCoBan) {
        if (giaCoBan == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal heSo = (getLoaiGhe() != null && getLoaiGhe().getHeSoGia() != null)
                ? getLoaiGhe().getHeSoGia()
                : HE_SO_DOI;
        return giaCoBan.multiply(heSo).setScale(0, RoundingMode.HALF_UP);
    }
}
