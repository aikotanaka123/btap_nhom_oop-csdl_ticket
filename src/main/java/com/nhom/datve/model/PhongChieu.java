package com.nhom.datve.model;

/** Bang PhongChieu. soHang x soCot la kich thuoc luoi de ve so do ghe. */
public class PhongChieu {
    private int maPhong;
    private String tenPhong;
    private int soHang;
    private int soCot;
    private boolean hoatDong = true;

    public PhongChieu() { }

    public PhongChieu(int maPhong, String tenPhong, int soHang, int soCot) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.soHang = soHang;
        this.soCot = soCot;
    }

    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    public int getSoHang() { return soHang; }
    public void setSoHang(int soHang) { this.soHang = soHang; }
    public int getSoCot() { return soCot; }
    public void setSoCot(int soCot) { this.soCot = soCot; }
    public boolean isHoatDong() { return hoatDong; }
    public void setHoatDong(boolean hoatDong) { this.hoatDong = hoatDong; }

    @Override
    public String toString() { return tenPhong; }
}
