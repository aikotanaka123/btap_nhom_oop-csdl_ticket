package com.nhom.datve.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Bang Phim; danh sach the loai lay tu bang trung gian Phim_TheLoai. */
public class Phim {
    public static final String DANG_CHIEU = "DANG_CHIEU";
    public static final String SAP_CHIEU = "SAP_CHIEU";
    public static final String NGUNG_CHIEU = "NGUNG_CHIEU";

    private int maPhim;
    private String tenPhim;
    private int thoiLuong;          // phut
    private String daoDien;
    private LocalDate ngayKhoiChieu;
    private String doTuoi;          // P, T13, T16, T18
    private String moTa;
    private String poster;          // duong dan anh
    private String trangThai = DANG_CHIEU;
    private List<TheLoai> theLoais = new ArrayList<>();

    public Phim() { }

    public Phim(int maPhim, String tenPhim, int thoiLuong, String daoDien, String doTuoi) {
        this.maPhim = maPhim;
        this.tenPhim = tenPhim;
        this.thoiLuong = thoiLuong;
        this.daoDien = daoDien;
        this.doTuoi = doTuoi;
    }

    public int getMaPhim() { return maPhim; }
    public void setMaPhim(int maPhim) { this.maPhim = maPhim; }
    public String getTenPhim() { return tenPhim; }
    public void setTenPhim(String tenPhim) { this.tenPhim = tenPhim; }
    public int getThoiLuong() { return thoiLuong; }
    public void setThoiLuong(int thoiLuong) { this.thoiLuong = thoiLuong; }
    public String getDaoDien() { return daoDien; }
    public void setDaoDien(String daoDien) { this.daoDien = daoDien; }
    public LocalDate getNgayKhoiChieu() { return ngayKhoiChieu; }
    public void setNgayKhoiChieu(LocalDate ngayKhoiChieu) { this.ngayKhoiChieu = ngayKhoiChieu; }
    public String getDoTuoi() { return doTuoi; }
    public void setDoTuoi(String doTuoi) { this.doTuoi = doTuoi; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public List<TheLoai> getTheLoais() { return theLoais; }
    public void setTheLoais(List<TheLoai> theLoais) { this.theLoais = theLoais; }

    @Override
    public String toString() { return tenPhim; }
}
