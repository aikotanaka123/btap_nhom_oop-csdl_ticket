package com.nhom.datve.model;

import java.time.LocalDateTime;

/** Bang TaiKhoan. matKhau luu chuoi bam SHA-256, khong luu mat khau goc. */
public class TaiKhoan {
    private int maTK;
    private String tenDangNhap;
    private String matKhau;
    private VaiTro vaiTro;
    private boolean trangThai = true;
    private LocalDateTime ngayTao;

    public TaiKhoan() { }

    public TaiKhoan(int maTK, String tenDangNhap, String matKhau, VaiTro vaiTro) {
        this.maTK = maTK;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.ngayTao = LocalDateTime.now();
    }

    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public VaiTro getVaiTro() { return vaiTro; }
    public void setVaiTro(VaiTro vaiTro) { this.vaiTro = vaiTro; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
