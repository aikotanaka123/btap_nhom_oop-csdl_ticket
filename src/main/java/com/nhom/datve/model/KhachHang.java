package com.nhom.datve.model;

import java.time.LocalDate;

/** Bang KhachHang. TODO(Trung): co the tach lop cha NguoiDung (hoTen, soDienThoai) cho KhachHang va NhanVien ke thua. */
public class KhachHang {
    private int maKH;
    private int maTK;
    private String hoTen;
    private String soDienThoai;
    private String email;
    private LocalDate ngaySinh;

    public KhachHang() { }

    public KhachHang(int maKH, int maTK, String hoTen, String soDienThoai, String email) {
        this.maKH = maKH;
        this.maTK = maTK;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
    }

    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }
    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }
}
