package com.nhom.datve.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Bang ThanhToan (gia lap): moi lan dat ve co toi da 1 thanh toan. */
public class ThanhToan {
    public static final String THANH_CONG = "THANH_CONG";
    public static final String HOAN_TIEN = "HOAN_TIEN";

    private int maTT;
    private int maDatVe;
    private BigDecimal soTien;
    private PhuongThucThanhToan phuongThuc;
    private LocalDateTime thoiGian;
    private String trangThai = THANH_CONG;

    public ThanhToan() { }

    public int getMaTT() { return maTT; }
    public void setMaTT(int maTT) { this.maTT = maTT; }
    public int getMaDatVe() { return maDatVe; }
    public void setMaDatVe(int maDatVe) { this.maDatVe = maDatVe; }
    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }
    public PhuongThucThanhToan getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(PhuongThucThanhToan phuongThuc) { this.phuongThuc = phuongThuc; }
    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
