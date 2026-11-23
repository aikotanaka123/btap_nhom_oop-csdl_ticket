package com.nhom.datve.ui;

import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.service.TaiKhoanService;
import com.nhom.datve.util.AppConfig;
import com.nhom.datve.util.PhienDangNhap;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

/**
 * Cua so dang nhap chinh thuc cua ung dung.
 * Main.java tu dong goi constructor khong tham so LoginFrame().
 * Dang nhap thanh cong -> new MainFrame(phien).setVisible(true); dispose();
 */
public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final transient TaiKhoanService service = new TaiKhoanService();
    private final JTextField txtTenDangNhap = new JTextField(20);
    private final JPasswordField txtMatKhau = new JPasswordField(20);
    private final JButton btnDangNhap = Theme.nutChinh("Đăng nhập");
    private final JButton btnDangKy = Theme.nutPhu("Đăng ký tài khoản");
    private final JButton btnThoat = Theme.nutPhu("Thoát");

    public LoginFrame() {
        super("Đặt vé xem phim – Đăng nhập");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel goc = new JPanel(new BorderLayout());
        goc.setBackground(Theme.MAU_NEN);

        // Banner dau trang mau do rap phim
        JPanel banner = new JPanel(new BorderLayout(0, 4));
        banner.setBackground(Theme.MAU_CHINH);
        banner.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel lblTieuDe = new JLabel("🎬 RẠP CHIẾU PHIM PTIT");
        lblTieuDe.setFont(Theme.FONT_TIEU_DE);
        lblTieuDe.setForeground(Color.WHITE);
        JLabel lblMoTa = new JLabel("Hệ thống đặt vé xem phim trực tuyến"
                + (AppConfig.isMockMode() ? "  ·  Chế độ dữ liệu giả" : ""));
        lblMoTa.setForeground(new Color(0xFFE3E0));
        banner.add(lblTieuDe, BorderLayout.NORTH);
        banner.add(lblMoTa, BorderLayout.SOUTH);
        goc.add(banner, BorderLayout.NORTH);

        // Form nhap
        JPanel the = new JPanel(new BorderLayout(0, 14));
        the.setBackground(Theme.MAU_THE);
        the.setBorder(Theme.le(24));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 6, 7, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        form.add(Theme.tieuDe("Đăng nhập tài khoản"), c);

        c.gridy = 1;
        form.add(Theme.chuPhu("Tài khoản mẫu: khach / 123456 (Khách hàng)  |  admin / admin123 (Nhân viên)"), c);

        c.gridwidth = 1;
        c.gridy = 2;
        c.gridx = 0;
        c.weightx = 0;
        JLabel lblTen = new JLabel("Tên đăng nhập");
        lblTen.setFont(Theme.FONT_DAM);
        form.add(lblTen, c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(txtTenDangNhap, c);

        c.gridy = 3;
        c.gridx = 0;
        c.weightx = 0;
        JLabel lblMK = new JLabel("Mật khẩu");
        lblMK.setFont(Theme.FONT_DAM);
        form.add(lblMK, c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(txtMatKhau, c);

        the.add(form, BorderLayout.CENTER);

        // Khu vuc nut
        btnDangNhap.addActionListener(e -> xuLyDangNhap());
        btnDangKy.addActionListener(e -> moDangKy());
        btnThoat.addActionListener(e -> System.exit(0));
        txtTenDangNhap.addActionListener(e -> txtMatKhau.requestFocusInWindow());
        txtMatKhau.addActionListener(e -> xuLyDangNhap());

        JPanel khuNut = new JPanel(new BorderLayout());
        khuNut.setOpaque(false);
        khuNut.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.MAU_VIEN));

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        trai.setOpaque(false);
        trai.add(btnDangKy);

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        phai.setOpaque(false);
        phai.add(btnThoat);
        phai.add(btnDangNhap);

        khuNut.add(trai, BorderLayout.WEST);
        khuNut.add(phai, BorderLayout.EAST);
        the.add(khuNut, BorderLayout.SOUTH);

        goc.add(the, BorderLayout.CENTER);
        setContentPane(goc);
        getRootPane().setDefaultButton(btnDangNhap);
        setSize(new Dimension(500, 390));
        setLocationRelativeTo(null);
    }

    private void xuLyDangNhap() {
        String ten = txtTenDangNhap.getText();
        String mk = new String(txtMatKhau.getPassword());

        if (ten == null || ten.isBlank() || mk.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnDangNhap.setEnabled(false);
        new SwingWorker<PhienDangNhap, Void>() {
            @Override
            protected PhienDangNhap doInBackground() throws Exception {
                return service.dangNhap(ten, mk);
            }

            @Override
            protected void done() {
                btnDangNhap.setEnabled(true);
                try {
                    PhienDangNhap phien = get();
                    new MainFrame(phien).setVisible(true);
                    dispose();
                } catch (ExecutionException ex) {
                    Throwable goc = ex.getCause();
                    if (goc instanceof NghiepVuException) {
                        JOptionPane.showMessageDialog(LoginFrame.this, goc.getMessage(),
                                "Đăng nhập thất bại", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Không kết nối được CSDL: " + goc.getMessage(),
                                "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void moDangKy() {
        Optional<PhienDangNhap> kq = DangKyDialog.hienThi(this);
        kq.ifPresent(phien -> {
            txtTenDangNhap.setText(phien.getTaiKhoan().getTenDangNhap());
            txtMatKhau.setText("");
            txtMatKhau.requestFocusInWindow();
        });
    }
}
