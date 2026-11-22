package com.nhom.datve.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Bang Ve: 1 ve = 1 ghe cua 1 suat chieu. Cac truong ten* va gioBatDau chi de hien thi. */
public class Ve {
    public static final String HIEU_LUC = "HIEU_LUC";
    public static final String DA_HUY = "DA_HUY";

    private int maVe;
    private int maDatVe;
    private int maSuat;
    private int maGhe;
    private BigDecimal giaVe;
    private String trangThai = HIEU_LUC;
    private String tenGhe;
    private String tenPhim;
    private String tenPhong;
    private LocalDateTime gioBatDau;

    public Ve() { }

    public Ve(int maVe, int maDatVe, int maSuat, int maGhe, BigDecimal giaVe) {
        this.maVe = maVe;
        this.maDatVe = maDatVe;
        this.maSuat = maSuat;
        this.maGhe = maGhe;
        this.giaVe = giaVe;
    }

    public int getMaVe() { return maVe; }
    public void setMaVe(int maVe) { this.maVe = maVe; }
    public int getMaDatVe() { return maDatVe; }
    public void setMaDatVe(int maDatVe) { this.maDatVe = maDatVe; }
    public int getMaSuat() { return maSuat; }
    public void setMaSuat(int maSuat) { this.maSuat = maSuat; }
    public int getMaGhe() { return maGhe; }
    public void setMaGhe(int maGhe) { this.maGhe = maGhe; }
    public BigDecimal getGiaVe() { return giaVe; }
    public void setGiaVe(BigDecimal giaVe) { this.giaVe = giaVe; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getTenGhe() { return tenGhe; }
    public void setTenGhe(String tenGhe) { this.tenGhe = tenGhe; }
    public String getTenPhim() { return tenPhim; }
    public void setTenPhim(String tenPhim) { this.tenPhim = tenPhim; }
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    public LocalDateTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalDateTime gioBatDau) { this.gioBatDau = gioBatDau; }
}
