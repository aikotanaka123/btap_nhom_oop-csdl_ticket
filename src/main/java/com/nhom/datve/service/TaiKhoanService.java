package com.nhom.datve.service;

import com.nhom.datve.dao.DAOFactory;
import com.nhom.datve.dao.KhachHangDAO;
import com.nhom.datve.dao.NhanVienDAO;
import com.nhom.datve.dao.TaiKhoanDAO;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.KhachHang;
import com.nhom.datve.model.TaiKhoan;
import com.nhom.datve.model.VaiTro;
import com.nhom.datve.util.PasswordUtil;
import com.nhom.datve.util.PhienDangNhap;
import com.nhom.datve.util.Validator;

/**
 * Dang nhap va dang ky tai khoan.
 * LoginFrame goi dangNhap() roi mo new MainFrame(phien).
 * DangKyDialog goi dangKy(...) de tao TaiKhoan + KhachHang moi.
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

    /**
     * Dang ky tai khoan khach hang moi (Task 28 & 29 - Trung).
     */
    public PhienDangNhap dangKy(String tenDangNhap, String matKhau, String xacNhanMatKhau,
                                String hoTen, String soDienThoai, String email) throws NghiepVuException {
        String ten = Validator.kiemTraTenDangNhap(tenDangNhap);
        String mk = Validator.kiemTraMatKhau(matKhau, xacNhanMatKhau);
        String tenKH = Validator.kiemTraHoTen(hoTen);
        String sdt = Validator.kiemTraSoDienThoai(soDienThoai);
        String mail = Validator.kiemTraEmail(email);

        if (taiKhoanDAO.existsByTenDangNhap(ten)) {
            throw new NghiepVuException("Tên đăng nhập \"" + ten + "\" đã được sử dụng.");
        }
        if (khachHangDAO.existsBySoDienThoai(sdt)) {
            throw new NghiepVuException("Số điện thoại \"" + sdt + "\" đã được đăng ký.");
        }
        if (mail != null && khachHangDAO.existsByEmail(mail)) {
            throw new NghiepVuException("Email \"" + mail + "\" đã được đăng ký.");
        }

        TaiKhoan tk = new TaiKhoan(0, ten, PasswordUtil.hash(mk), VaiTro.KHACHHANG);
        Integer maTK = taiKhoanDAO.insert(tk);
        tk.setMaTK(maTK);

        KhachHang kh = new KhachHang(0, maTK, tenKH, sdt, mail);
        Integer maKH = khachHangDAO.insert(kh);
        kh.setMaKH(maKH);

        return new PhienDangNhap(tk, kh, null);
    }
}
