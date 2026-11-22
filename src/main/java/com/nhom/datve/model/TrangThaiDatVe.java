package com.nhom.datve.model;

/** Khop voi cot DatVe.TrangThai. */
public enum TrangThaiDatVe {
    CHO_THANH_TOAN("Chờ thanh toán"),
    DA_THANH_TOAN("Đã thanh toán"),
    DA_HUY("Đã hủy");

    private final String moTa;

    TrangThaiDatVe(String moTa) { this.moTa = moTa; }

    public String getMoTa() { return moTa; }
}
