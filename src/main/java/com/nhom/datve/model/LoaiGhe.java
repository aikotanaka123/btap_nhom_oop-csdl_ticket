package com.nhom.datve.model;

import java.math.BigDecimal;

/** Bang LoaiGhe: Thuong (1.0), VIP (1.3), Doi (2.0)... */
public class LoaiGhe {
    private int maLoaiGhe;
    private String tenLoai;
    private BigDecimal heSoGia;

    public LoaiGhe() { }

    public LoaiGhe(int maLoaiGhe, String tenLoai, BigDecimal heSoGia) {
        this.maLoaiGhe = maLoaiGhe;
        this.tenLoai = tenLoai;
        this.heSoGia = heSoGia;
    }

    public int getMaLoaiGhe() { return maLoaiGhe; }
    public void setMaLoaiGhe(int maLoaiGhe) { this.maLoaiGhe = maLoaiGhe; }
    public String getTenLoai() { return tenLoai; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }
    public BigDecimal getHeSoGia() { return heSoGia; }
    public void setHeSoGia(BigDecimal heSoGia) { this.heSoGia = heSoGia; }

    @Override
    public String toString() { return tenLoai; }
}
