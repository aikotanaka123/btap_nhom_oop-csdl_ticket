package com.nhom.datve.ui;

import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.service.TaiKhoanService;
import com.nhom.datve.util.PhienDangNhap;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 * Hop thoai dang ky tai khoan khach hang moi.
 * Kiem tra du lieu nhap va bao loi bang JOptionPane; thao tac CSDL chay trong SwingWorker.
 */
public class DangKyDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final transient TaiKhoanService service = new TaiKhoanService();
    private final JTextField txtTenDangNhap = new JTextField(20);
    private final JPasswordField txtMatKhau = new JPasswordField(20);
    private final JPasswordField txtXacNhan = new JPasswordField(20);
    private final JTextField txtHoTen = new JTextField(20);
    private final JTextField txtSoDienThoai = new JTextField(20);
    private final JTextField txtEmail = new JTextField(20);
    private final JButton btnDangKy = Theme.nutChinh("Đăng ký");
    private final JButton btnHuy = Theme.nutPhu("Hủy");

    private transient PhienDangNhap ketQua;

    public DangKyDialog(Window owner) {
        super(owner, "Đăng ký tài khoản khách hàng", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel goc = new JPanel(new BorderLayout(0, 14));
        goc.setBorder(Theme.le(22));
        goc.setBackground(Theme.MAU_THE);

        JPanel dau = new JPanel(new BorderLayout(0, 4));
        dau.setOpaque(false);
        dau.add(Theme.tieuDe("Đăng ký tài khoản"), BorderLayout.NORTH);
        dau.add(Theme.chuPhu("Các mục có dấu (*) là bắt buộc nhập."), BorderLayout.SOUTH);
        goc.add(dau, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        themDong(form, c, 0, "Tên đăng nhập (*)", txtTenDangNhap);
        themDong(form, c, 1, "Mật khẩu (*)", txtMatKhau);
        themDong(form, c, 2, "Nhập lại mật khẩu (*)", txtXacNhan);
        themDong(form, c, 3, "Họ và tên (*)", txtHoTen);
        themDong(form, c, 4, "Số điện thoại (*)", txtSoDienThoai);
        themDong(form, c, 5, "Email (tùy chọn)", txtEmail);
        goc.add(form, BorderLayout.CENTER);

        btnDangKy.addActionListener(e -> xuLyDangKy());
        btnHuy.addActionListener(e -> dispose());
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.setOpaque(false);
        nut.add(btnHuy);
        nut.add(btnDangKy);
        goc.add(nut, BorderLayout.SOUTH);

        setContentPane(goc);
        getRootPane().setDefaultButton(btnDangKy);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }

    public static Optional<PhienDangNhap> hienThi(Window owner) {
        DangKyDialog dlg = new DangKyDialog(owner);
        dlg.setVisible(true);
        return Optional.ofNullable(dlg.ketQua);
    }

    private static void themDong(JPanel form, GridBagConstraints c, int dong, String nhan, javax.swing.JComponent oNhap) {
        c.gridy = dong;
        c.gridx = 0;
        c.weightx = 0;
        JLabel lbl = new JLabel(nhan);
        lbl.setFont(Theme.FONT_DAM);
        form.add(lbl, c);

        c.gridx = 1;
        c.weightx = 1;
        form.add(oNhap, c);
    }

    private void xuLyDangKy() {
        String ten = txtTenDangNhap.getText();
        String mk = new String(txtMatKhau.getPassword());
        String xn = new String(txtXacNhan.getPassword());
        String hoTen = txtHoTen.getText();
        String sdt = txtSoDienThoai.getText();
        String email = txtEmail.getText();

        btnDangKy.setEnabled(false);
        new SwingWorker<PhienDangNhap, Void>() {
            @Override
            protected PhienDangNhap doInBackground() throws Exception {
                return service.dangKy(ten, mk, xn, hoTen, sdt, email);
            }

            @Override
            protected void done() {
                btnDangKy.setEnabled(true);
                try {
                    ketQua = get();
                    JOptionPane.showMessageDialog(DangKyDialog.this,
                            "Đăng ký tài khoản \"" + ketQua.getTaiKhoan().getTenDangNhap() + "\" thành công!",
                            "Đăng ký thành công", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (ExecutionException ex) {
                    Throwable goc = ex.getCause();
                    if (goc instanceof NghiepVuException) {
                        JOptionPane.showMessageDialog(DangKyDialog.this, goc.getMessage(),
                                "Kiểm tra lại thông tin", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(DangKyDialog.this,
                                "Lỗi khi đăng ký tài khoản: " + goc.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}
