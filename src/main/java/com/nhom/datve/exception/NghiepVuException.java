package com.nhom.datve.exception;

/**
 * Loi nghiep vu (ghe da co nguoi dat, suat chieu da bat dau...).
 * Thong bao trong loi nay viet bang tieng Viet de UI hien thang cho nguoi dung.
 */
public class NghiepVuException extends Exception {
    private static final long serialVersionUID = 1L;

    public NghiepVuException(String message) {
        super(message);
    }
}
