package com.nhom.datve.model;

/** Khop voi cot ThanhToan.PhuongThuc (thanh toan gia lap). */
public enum PhuongThucThanhToan {
    TIEN_MAT("Tiền mặt tại quầy"),
    THE("Thẻ ngân hàng"),
    VI_DIEN_TU("Ví điện tử");

    private final String moTa;

    PhuongThucThanhToan(String moTa) { this.moTa = moTa; }

    public String getMoTa() { return moTa; }

    @Override
    public String toString() { return moTa; }
}
