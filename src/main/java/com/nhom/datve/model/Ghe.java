package com.nhom.datve.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Bang Ghe. Moi ghe thuoc 1 phong, co 1 loai ghe.
 * TODO(Trung): co the chuyen thanh lop truu tuong Ghe voi cac lop con GheThuong/GheVIP/GheDoi
 * va override tinhGiaVe() de the hien da hinh.
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

    /** Ten hien thi tren so do ghe, vd "C7". */
    public String getTenGhe() { return hang + String.valueOf(soGhe); }

    /** Gia ve = gia co ban cua suat chieu x he so cua loai ghe, lam tron den dong. */
    public BigDecimal tinhGiaVe(BigDecimal giaCoBan) {
        BigDecimal heSo = loaiGhe == null ? BigDecimal.ONE : loaiGhe.getHeSoGia();
        return giaCoBan.multiply(heSo).setScale(0, RoundingMode.HALF_UP);
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
