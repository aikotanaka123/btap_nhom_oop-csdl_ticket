package com.nhom.datve.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Bam mat khau SHA-256 truoc khi luu/so sanh voi cot TaiKhoan.MatKhau. */
public final class PasswordUtil {
    private PasswordUtil() { }

    public static String hash(String matKhau) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(matKhau.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean khop(String matKhauNhap, String matKhauDaBam) {
        return hash(matKhauNhap).equalsIgnoreCase(matKhauDaBam);
    }
}
