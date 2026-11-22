package com.nhom.datve.service;

import com.nhom.datve.dao.DAOFactory;
import com.nhom.datve.dao.KhachHangDAO;
import com.nhom.datve.dao.NhanVienDAO;
import com.nhom.datve.dao.TaiKhoanDAO;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.TaiKhoan;
import com.nhom.datve.model.VaiTro;
import com.nhom.datve.util.PasswordUtil;
import com.nhom.datve.util.PhienDangNhap;

/**
 * Dang nhap. LoginFrame (Trung) goi dangNhap() roi mo new MainFrame(phien).
 * TODO(Trung): them dangKy(...) khi lam DangKyDialog.
 */
public class TaiKhoanService {
    private final TaiKhoanDAO taiKhoanDAO;
    private final KhachHangDAO khachHangDAO;
    private final NhanVienDAO nhanVienDAO;

    public TaiKhoanService() {
        this(DAOFactory.taiKhoan(), DAOFactory.khachHang(), DAOFactory.nhanVien());
    }

    public TaiKhoanService(TaiKhoanDAO taiKhoanDAO, KhachHangDAO khachHangDAO, NhanVienDAO nhanVienDAO) {
        this.taiKhoanDAO = taiKhoanDAO;
        this.khachHangDAO = khachHangDAO;
        this.nhanVienDAO = nhanVienDAO;
    }

    public PhienDangNhap dangNhap(String tenDangNhap, String matKhau) throws NghiepVuException {
        if (tenDangNhap == null || tenDangNhap.isBlank() || matKhau == null || matKhau.isEmpty()) {
            throw new NghiepVuException("Vui lòng nhập tên đăng nhập và mật khẩu.");
        }
        TaiKhoan tk = taiKhoanDAO.findByTenDangNhap(tenDangNhap.trim())
                .filter(t -> PasswordUtil.khop(matKhau, t.getMatKhau()))
                .orElseThrow(() -> new NghiepVuException("Sai tên đăng nhập hoặc mật khẩu."));
        if (!tk.isTrangThai()) {
            throw new NghiepVuException("Tài khoản đã bị khóa.");
        }
        if (tk.getVaiTro() == VaiTro.KHACHHANG) {
            return new PhienDangNhap(tk, khachHangDAO.findByMaTK(tk.getMaTK())
                    .orElseThrow(() -> new NghiepVuException("Tài khoản chưa có hồ sơ khách hàng.")), null);
        }
        return new PhienDangNhap(tk, null, nhanVienDAO.findByMaTK(tk.getMaTK())
                .orElseThrow(() -> new NghiepVuException("Tài khoản chưa có hồ sơ nhân viên.")));
    }
}
