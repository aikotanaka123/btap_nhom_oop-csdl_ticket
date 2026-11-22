package com.nhom.datve.model;

/** Bang NhanVien (tai khoan admin). */
public class NhanVien {
    private int maNV;
    private int maTK;
    private String hoTen;
    private String soDienThoai;
    private String chucVu;

    public NhanVien() { }

    public NhanVien(int maNV, int maTK, String hoTen, String chucVu) {
        this.maNV = maNV;
        this.maTK = maTK;
        this.hoTen = hoTen;
        this.chucVu = chucVu;
    }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }
    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }
}
