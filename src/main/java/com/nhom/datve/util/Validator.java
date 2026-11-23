package com.nhom.datve.util;

import com.nhom.datve.exception.NghiepVuException;

/**
 * Kiem tra du lieu nhap tren cac man hinh giao dien.
 * Neu du lieu sai se nem {@link NghiepVuException} voi thong bao tieng Viet ro rang
 * de man hinh hien JOptionPane ma khong vang loi he thong.
 */
public final class Validator {
    private static final String TEN_DANG_NHAP_REGEX = "^[a-zA-Z0-9_]{3,30}$";
    private static final String SO_DIEN_THOAI_REGEX = "^(0|\\+84)[0-9]{9}$";
    private static final String EMAIL_REGEX = "^[\\w.+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$";

    private Validator() { }

    public static String batBuocNhap(String giaTri, String tenTruong) throws NghiepVuException {
        if (giaTri == null || giaTri.trim().isEmpty()) {
            throw new NghiepVuException("Vui lòng nhập " + tenTruong + ".");
        }
        return giaTri.trim();
    }

    public static String kiemTraTenDangNhap(String tenDangNhap) throws NghiepVuException {
        String s = batBuocNhap(tenDangNhap, "tên đăng nhập");
        if (!s.matches(TEN_DANG_NHAP_REGEX)) {
            throw new NghiepVuException("Tên đăng nhập phải từ 3–30 ký tự, chỉ gồm chữ cái không dấu, chữ số hoặc dấu gạch dưới (_).");
        }
        return s;
    }

    public static String kiemTraMatKhau(String matKhau, String xacNhanMatKhau) throws NghiepVuException {
        if (matKhau == null || matKhau.isEmpty()) {
            throw new NghiepVuException("Vui lòng nhập mật khẩu.");
        }
        if (matKhau.length() < 6) {
            throw new NghiepVuException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
        if (xacNhanMatKhau != null && !matKhau.equals(xacNhanMatKhau)) {
            throw new NghiepVuException("Mật khẩu xác nhận không khớp.");
        }
        return matKhau;
    }

    public static String kiemTraHoTen(String hoTen) throws NghiepVuException {
        String s = batBuocNhap(hoTen, "họ và tên");
        if (s.length() < 2 || s.length() > 100) {
            throw new NghiepVuException("Họ và tên phải từ 2 đến 100 ký tự.");
        }
        return s;
    }

    public static String kiemTraSoDienThoai(String soDienThoai) throws NghiepVuException {
        String s = batBuocNhap(soDienThoai, "số điện thoại");
        if (!s.matches(SO_DIEN_THOAI_REGEX)) {
            throw new NghiepVuException("Số điện thoại không hợp lệ (gồm 10 chữ số, bắt đầu bằng 0 hoặc +84).");
        }
        return s;
    }

    public static String kiemTraEmail(String email) throws NghiepVuException {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        String s = email.trim();
        if (!s.matches(EMAIL_REGEX)) {
            throw new NghiepVuException("Địa chỉ email không hợp lệ.");
        }
        return s;
    }
}
