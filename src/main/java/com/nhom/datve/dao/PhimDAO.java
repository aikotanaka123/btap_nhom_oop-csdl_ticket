package com.nhom.datve.dao;

import com.nhom.datve.model.Phim;
import java.util.List;

public interface PhimDAO extends GenericDAO<Phim, Integer> {
    /** Phim co TrangThai = DANG_CHIEU, hien o trang chu khach hang. */
    List<Phim> findDangChieu();

    /**
     * Tim phim theo ten (chua tu khoa) va/hoac the loai. Tham so null = bo qua dieu kien do.
     * Cai dat bang SQL dong nhung PHAI dung PreparedStatement (xem demo/DynamicSqlDemo).
     */
    List<Phim> search(String tuKhoa, Integer maTL);

    /** Ghi lai danh sach the loai cua phim vao bang Phim_TheLoai (xoa cu, them moi). */
    void capNhatTheLoai(int maPhim, List<Integer> dsMaTL);
}
