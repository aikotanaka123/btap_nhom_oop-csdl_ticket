package com.nhom.datve.model;

import java.time.LocalDate;

/**
 * Bang KhachHang ke thua lop truu tuong {@link NguoiDung}.
 */
public class KhachHang extends NguoiDung {
    private int maKH;
    private LocalDate ngaySinh;

    public KhachHang() {
        super();
    }

    public KhachHang(int maKH, int maTK, String hoTen, String soDienThoai, String email) {
        super(maTK, hoTen, soDienThoai, email);
        this.maKH = maKH;
    }

    @Override
    public VaiTro getVaiTro() {
        return VaiTro.KHACHHANG;
    }

    @Override
    public String getMoTaVaiTro() {
        return "Khách hàng";
    }

    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }
}
