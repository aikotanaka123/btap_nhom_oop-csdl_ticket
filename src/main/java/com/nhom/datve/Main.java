package com.nhom.datve;

import com.nhom.datve.ui.DangNhapTamDialog;
import com.nhom.datve.ui.MainFrame;
import com.nhom.datve.ui.Theme;
import java.awt.Window;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/** Diem khoi chay ung dung. */
public final class Main {
    /** Lop dang nhap cua Trung. Chua co thi dung DangNhapTamDialog. */
    private static final String LOP_DANG_NHAP = "com.nhom.datve.ui.LoginFrame";

    private Main() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.apDung();
            moManHinhDangNhap();
        });
    }

    /**
     * Mo man hinh dang nhap (luc khoi dong va sau khi dang xuat).
     * LoginFrame khi dang nhap thanh cong chi can: new MainFrame(phien).setVisible(true); dispose();
     */
    public static void moManHinhDangNhap() {
        try {
            Window login = (Window) Class.forName(LOP_DANG_NHAP).getDeclaredConstructor().newInstance();
            login.setVisible(true);
        } catch (ClassNotFoundException chuaCo) {
            DangNhapTamDialog.hienThi().ifPresentOrElse(
                    phien -> new MainFrame(phien).setVisible(true),
                    () -> System.exit(0));
        } catch (ReflectiveOperationException e) {
            JOptionPane.showMessageDialog(null, "Không mở được màn hình đăng nhập: " + e, "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
