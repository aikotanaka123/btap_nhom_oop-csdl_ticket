package com.nhom.datve.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Bang DatVe: 1 lan dat gom nhieu ve.
 * Tong tien KHONG luu trong bang (la thuoc tinh dan xuat) ma tinh tu danh sach ve.
 */
public class DatVe {
    private int maDatVe;
    private int maKH;
    private LocalDateTime ngayDat;
    private TrangThaiDatVe trangThai = TrangThaiDatVe.CHO_THANH_TOAN;
    private PhuongThucThanhToan phuongThuc;
    private List<Ve> ves = new ArrayList<>();

    public DatVe() { }

    public DatVe(int maDatVe, int maKH, LocalDateTime ngayDat, TrangThaiDatVe trangThai) {
        this.maDatVe = maDatVe;
        this.maKH = maKH;
        this.ngayDat = ngayDat;
        this.trangThai = trangThai;
    }

    public BigDecimal getTongTien() {
        return ves.stream().map(Ve::getGiaVe).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getMaDatVe() { return maDatVe; }
    public void setMaDatVe(int maDatVe) { this.maDatVe = maDatVe; }
    public int getMaKH() { return maKH; }
    public void setMaKH(int maKH) { this.maKH = maKH; }
    public LocalDateTime getNgayDat() { return ngayDat; }
    public void setNgayDat(LocalDateTime ngayDat) { this.ngayDat = ngayDat; }
    public TrangThaiDatVe getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiDatVe trangThai) { this.trangThai = trangThai; }
    public PhuongThucThanhToan getPhuongThuc() { return phuongThuc; }
    public void setPhuongThuc(PhuongThucThanhToan phuongThuc) { this.phuongThuc = phuongThuc; }
    public List<Ve> getVes() { return ves; }
    public void setVes(List<Ve> ves) { this.ves = ves; }
}
