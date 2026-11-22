package com.nhom.datve.model;

import java.util.List;

/**
 * Ket qua tim kiem kem cau SQL thuc su da gui len CSDL, de man hinh tim kiem hien ra phuc vu
 * demo Dynamic SQL (chuong 10).
 *
 * @param dsSuat  cac suat chieu tim thay (tenPhim, theLoai, tenPhong da dien san)
 * @param sql     cau lenh SQL da chay
 * @param thamSo  gia tri cac dau ? theo thu tu (rong neu dung noi chuoi)
 * @param anToan  true = PreparedStatement, false = noi chuoi
 * @param ghiChu  thong bao them (loi cu phap SQL, che do du lieu gia...), null neu khong co
 */
public record KetQuaTimKiem(List<SuatChieu> dsSuat, String sql, List<Object> thamSo, boolean anToan, String ghiChu) {
}
