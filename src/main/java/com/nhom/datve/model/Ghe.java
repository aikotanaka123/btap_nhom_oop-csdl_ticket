package com.nhom.datve.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Bang Ghe. Moi ghe thuoc 1 phong, co 1 loai ghe.
 * Co cac lop con {@link GheThuong}, {@link GheVIP}, {@link GheDoi} ke thua va override
 * {@link #tinhGiaVe(BigDecimal)} de the hien tinh ke thua va da hinh.
 */
public class Ghe {
    private int maGhe;
    private int maPhong;
    private LoaiGhe loaiGhe;
    private char hang;      // A, B, C...
    private int soGhe;      // 1, 2, 3...

    public Ghe() { }

    public Ghe(int maGhe, int maPhong, LoaiGhe loaiGhe, char hang, int soGhe) {
        this.maGhe = maGhe;
        this.maPhong = maPhong;
        this.loaiGhe = loaiGhe;
        this.hang = hang;
        this.soGhe = soGhe;
    }

    /**
     * Factory method tao doi tuong Ghe dung lop con (GheThuong / GheVIP / GheDoi)
     * dua theo LoaiGhe de the hien tinh da hinh.
     */
    public static Ghe taoGhe(int maGhe, int maPhong, LoaiGhe loaiGhe, char hang, int soGhe) {
        if (loaiGhe != null && loaiGhe.getTenLoai() != null) {
            String ten = loaiGhe.getTenLoai().trim().toLowerCase();
            if (ten.contains("vip")) {
                return new GheVIP(maGhe, maPhong, loaiGhe, hang, soGhe);
            }
            if (ten.contains("đôi") || ten.contains("doi")) {
                return new GheDoi(maGhe, maPhong, loaiGhe, hang, soGhe);
            }
        }
        return new GheThuong(maGhe, maPhong, loaiGhe, hang, soGhe);
    }

    /** Ten hien thi tren so do ghe, vd "C7". */
    public String getTenGhe() { return hang + String.valueOf(soGhe); }

    /**
     * Gia ve = gia co ban cua suat chieu x he so cua loai ghe, lam tron den dong.
     * Cac lop con GheThuong, GheVIP, GheDoi override phuong thuc nay.
     */
    public BigDecimal tinhGiaVe(BigDecimal giaCoBan) {
        if (giaCoBan == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal heSo = loaiGhe != null && loaiGhe.getHeSoGia() != null
                ? loaiGhe.getHeSoGia()
                : getHeSoMacDinh();
        return giaCoBan.multiply(heSo).setScale(0, RoundingMode.HALF_UP);
    }

    /** He so gia mac dinh cua loai ghe. */
    protected BigDecimal getHeSoMacDinh() {
        return BigDecimal.ONE;
    }

    public int getMaGhe() { return maGhe; }
    public void setMaGhe(int maGhe) { this.maGhe = maGhe; }
    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }
    public LoaiGhe getLoaiGhe() { return loaiGhe; }
    public void setLoaiGhe(LoaiGhe loaiGhe) { this.loaiGhe = loaiGhe; }
    public char getHang() { return hang; }
    public void setHang(char hang) { this.hang = hang; }
    public int getSoGhe() { return soGhe; }
    public void setSoGhe(int soGhe) { this.soGhe = soGhe; }

    @Override
    public String toString() { return getTenGhe(); }
}
