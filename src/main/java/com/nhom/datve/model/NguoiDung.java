package com.nhom.datve.model;

/**
 * Lop truu tuong NguoiDung the hien 4 tinh chat OOP:
 * - Truu tuong: lop abstract voi phuong thuc abstract getVaiTro(), getMoTaVaiTro().
 * - Dong goi: cac thuoc tinh private, truy cap qua getter/setter co kiem tra hop le.
 * - Ke thua: KhachHang va NhanVien cung ke thua NguoiDung.
 * - Da hinh: getVaiTro() va getMoTaVaiTro() tra ket qua khac nhau tuy lop con.
 */
public abstract class NguoiDung {
    private int maTK;
    private String hoTen;
    private String soDienThoai;
    private String email;

    protected NguoiDung() { }

    protected NguoiDung(int maTK, String hoTen, String soDienThoai, String email) {
        this.maTK = maTK;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.email = email;
    }

    public abstract VaiTro getVaiTro();

    public abstract String getMoTaVaiTro();

    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return hoTen + " (" + getMoTaVaiTro() + ")";
    }
}
