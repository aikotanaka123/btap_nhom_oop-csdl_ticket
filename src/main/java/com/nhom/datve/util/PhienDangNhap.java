package com.nhom.datve.util;

import com.nhom.datve.model.KhachHang;
import com.nhom.datve.model.NhanVien;
import com.nhom.datve.model.TaiKhoan;
import com.nhom.datve.model.VaiTro;

/**
 * Thong tin nguoi dang dang nhap. Tao boi TaiKhoanService.dangNhap(), truyen vao MainFrame.
 * Dung getMaKH() khi dat ve, isAdmin() de an/hien chuc nang quan tri.
 */
public final class PhienDangNhap {
    private final TaiKhoan taiKhoan;
    private final KhachHang khachHang;   // null neu la nhan vien
    private final NhanVien nhanVien;     // null neu la khach hang

    public PhienDangNhap(TaiKhoan taiKhoan, KhachHang khachHang, NhanVien nhanVien) {
        this.taiKhoan = taiKhoan;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
    }

    public boolean isAdmin() { return taiKhoan.getVaiTro() == VaiTro.NHANVIEN; }

    public String getTenHienThi() {
        if (khachHang != null) {
            return khachHang.getHoTen();
        }
        return nhanVien != null ? nhanVien.getHoTen() : taiKhoan.getTenDangNhap();
    }

    /** Ma khach hang dung khi dat ve; nem loi neu nguoi dang nhap khong phai khach hang. */
    public int getMaKH() {
        if (khachHang == null) {
            throw new IllegalStateException("Tài khoản đang đăng nhập không phải khách hàng.");
        }
        return khachHang.getMaKH();
    }

    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public KhachHang getKhachHang() { return khachHang; }
    public NhanVien getNhanVien() { return nhanVien; }
}
