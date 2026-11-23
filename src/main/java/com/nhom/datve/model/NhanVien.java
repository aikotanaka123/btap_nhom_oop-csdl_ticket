package com.nhom.datve.model;

/**
 * Bang NhanVien (tai khoan admin) ke thua lop truu tuong {@link NguoiDung}.
 */
public class NhanVien extends NguoiDung {
    private int maNV;
    private String chucVu;

    public NhanVien() {
        super();
    }

    public NhanVien(int maNV, int maTK, String hoTen, String chucVu) {
        super(maTK, hoTen, null, null);
        this.maNV = maNV;
        this.chucVu = chucVu;
    }

    @Override
    public VaiTro getVaiTro() {
        return VaiTro.NHANVIEN;
    }

    @Override
    public String getMoTaVaiTro() {
        return chucVu != null && !chucVu.isBlank() ? "Nhân viên - " + chucVu : "Nhân viên";
    }

    public int getMaNV() { return maNV; }
    public void setMaNV(int maNV) { this.maNV = maNV; }

    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { this.chucVu = chucVu; }
}
