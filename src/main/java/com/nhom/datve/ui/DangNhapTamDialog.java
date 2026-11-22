package com.nhom.datve.ui;

import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.service.TaiKhoanService;
import com.nhom.datve.util.PhienDangNhap;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Cua so dang nhap TAM de chay thu MainFrame truoc khi co LoginFrame cua Trung.
 * Khi co lop com.nhom.datve.ui.LoginFrame, Main tu dung LoginFrame va lop nay co the xoa.
 */
public class DangNhapTamDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final JTextField txtTen = new JTextField("khach", 18);
    private final JPasswordField txtMatKhau = new JPasswordField("123456", 18);
    private transient PhienDangNhap ketQua;

    private DangNhapTamDialog() {
        super((java.awt.Frame) null, "Đăng nhập (tạm)", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(Theme.le(18));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        form.add(Theme.tieuDe("Đặt vé xem phim"), c);
        c.gridy++;
        form.add(Theme.chuPhu("Tài khoản mẫu: khach / 123456 hoặc admin / admin123"), c);
        c.gridwidth = 1;
        c.gridy++;
        form.add(new JLabel("Tên đăng nhập"), c);
        c.gridx = 1;
        form.add(txtTen, c);
        c.gridx = 0;
        c.gridy++;
        form.add(new JLabel("Mật khẩu"), c);
        c.gridx = 1;
        form.add(txtMatKhau, c);

        JButton btnDangNhap = Theme.nutChinh("Đăng nhập");
        btnDangNhap.addActionListener(e -> dangNhap());
        JButton btnThoat = Theme.nutPhu("Thoát");
        btnThoat.addActionListener(e -> dispose());
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        nut.add(btnThoat);
        nut.add(btnDangNhap);

        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(nut, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnDangNhap);
        pack();
        setLocationRelativeTo(null);
    }

    private void dangNhap() {
        try {
            ketQua = new TaiKhoanService().dangNhap(txtTen.getText(), new String(txtMatKhau.getPassword()));
            dispose();
        } catch (NghiepVuException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Đăng nhập thất bại", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Không kết nối được CSDL: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static Optional<PhienDangNhap> hienThi() {
        DangNhapTamDialog d = new DangNhapTamDialog();
        d.setVisible(true);   // modal: dung o day den khi dong
        return Optional.ofNullable(d.ketQua);
    }
}
