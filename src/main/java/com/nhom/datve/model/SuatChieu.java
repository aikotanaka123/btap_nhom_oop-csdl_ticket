package com.nhom.datve.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Bang SuatChieu. tenPhim, tenPhong chi de hien thi (lay khi JOIN), khong luu trong bang. */
public class SuatChieu {
    private int maSuat;
    private int maPhim;
    private int maPhong;
    private LocalDateTime gioBatDau;
    private LocalDateTime gioKetThuc;
    private BigDecimal giaCoBan;
    private String tenPhim;
    private String tenPhong;
    private String theLoai;   // vd "Hanh dong, Hai" - chi de hien thi

    public SuatChieu() { }

    public SuatChieu(int maSuat, int maPhim, int maPhong, LocalDateTime gioBatDau,
                     LocalDateTime gioKetThuc, BigDecimal giaCoBan) {
        this.maSuat = maSuat;
        this.maPhim = maPhim;
        this.maPhong = maPhong;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.giaCoBan = giaCoBan;
    }

    public int getMaSuat() { return maSuat; }
    public void setMaSuat(int maSuat) { this.maSuat = maSuat; }
    public int getMaPhim() { return maPhim; }
    public void setMaPhim(int maPhim) { this.maPhim = maPhim; }
    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }
    public LocalDateTime getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(LocalDateTime gioBatDau) { this.gioBatDau = gioBatDau; }
    public LocalDateTime getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(LocalDateTime gioKetThuc) { this.gioKetThuc = gioKetThuc; }
    public BigDecimal getGiaCoBan() { return giaCoBan; }
    public void setGiaCoBan(BigDecimal giaCoBan) { this.giaCoBan = giaCoBan; }
    public String getTenPhim() { return tenPhim; }
    public void setTenPhim(String tenPhim) { this.tenPhim = tenPhim; }
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    public String getTheLoai() { return theLoai; }
    public void setTheLoai(String theLoai) { this.theLoai = theLoai; }
}
