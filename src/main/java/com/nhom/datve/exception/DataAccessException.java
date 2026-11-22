package com.nhom.datve.exception;

/**
 * Boc SQLException thanh loi unchecked de tang service/UI khong phai try-catch SQLException
 * o moi cho. Tang DAO bat SQLException roi nem loi nay kem nguyen nhan goc.
 */
public class DataAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
