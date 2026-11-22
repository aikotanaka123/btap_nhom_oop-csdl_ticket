package com.nhom.datve.dao;

import com.nhom.datve.model.TaiKhoan;
import java.util.Optional;

public interface TaiKhoanDAO extends GenericDAO<TaiKhoan, Integer> {
    /** Dung cho dang nhap: tim theo ten dang nhap roi so sanh mat khau da bam o tang service. */
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);

    boolean existsByTenDangNhap(String tenDangNhap);
}
