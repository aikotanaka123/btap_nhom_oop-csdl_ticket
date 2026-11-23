package com.nhom.datve.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Ghe VIP - he so gia 1.30 (130% gia co ban).
 * Ke thua {@link Ghe} va override {@link #tinhGiaVe(BigDecimal)}.
 */
public class GheVIP extends Ghe {
    public static final BigDecimal HE_SO_VIP = new BigDecimal("1.30");

    public GheVIP() {
        super();
    }

    public GheVIP(int maGhe, int maPhong, LoaiGhe loaiGhe, char hang, int soGhe) {
        super(maGhe, maPhong,
              loaiGhe != null ? loaiGhe : new LoaiGhe(2, "VIP", HE_SO_VIP),
              hang, soGhe);
    }

    @Override
    protected BigDecimal getHeSoMacDinh() {
        return HE_SO_VIP;
    }

    @Override
    public BigDecimal tinhGiaVe(BigDecimal giaCoBan) {
        if (giaCoBan == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal heSo = (getLoaiGhe() != null && getLoaiGhe().getHeSoGia() != null)
                ? getLoaiGhe().getHeSoGia()
                : HE_SO_VIP;
        return giaCoBan.multiply(heSo).setScale(0, RoundingMode.HALF_UP);
    }
}
