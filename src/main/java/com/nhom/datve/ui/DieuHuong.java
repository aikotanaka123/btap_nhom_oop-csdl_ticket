package com.nhom.datve.ui;

import com.nhom.datve.util.PhienDangNhap;
import java.awt.Window;

/**
 * Nhung gi mot man hinh con duoc phep yeu cau khung chinh lam. MainFrame cai dat interface nay
 * va truyen chinh no vao constructor cua tung man hinh.
 *
 * Vi du trong DanhSachPhimPanel: dieuHuong.chuyenDen(ManHinh.CHON_SUAT_CHIEU, phim.getMaPhim());
 */
public interface DieuHuong {
    void chuyenDen(ManHinh manHinh, Object thamSo);

    default void chuyenDen(ManHinh manHinh) { chuyenDen(manHinh, null); }

    /** Ve man hinh truoc do (nut "Quay lai"). */
    void quayLai();

    void dangXuat();

    PhienDangNhap getPhien();

    /** Cua so cha, dung lam owner cho JDialog/JOptionPane. */
    Window getCuaSo();
}
