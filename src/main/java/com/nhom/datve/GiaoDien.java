package com.nhom.datve;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

/*
 * GiaoDien.java: Giao dien chung: Theme, dang nhap/dang ky, MainFrame va dieu huong man hinh.
 * Gom cac lop: Theme, LoginFrame, DangKyDialog, DangNhapTamDialog, MainFrame, DieuHuong, ManHinh, ManHinhChoPanel, ManHinhCon
 */

// ==========================================================================================
// Theme   (truoc day: src/main/java/com/nhom/datve/ui/Theme.java)
// ==========================================================================================

/**
 * Mau, font, nut va dinh dang dung chung cho MOI man hinh. Ca nhom dung cac ham o day thay vi
 * tu dat mau/font, de giao dien thong nhat.
 */
final class Theme {
    public static final Color MAU_CHINH = new Color(0xB3261E);      // do rap phim: nut chinh, tieu de
    public static final Color MAU_CHINH_DAM = new Color(0x8C1D18);
    public static final Color MAU_NEN = new Color(0xF7F7F8);
    public static final Color MAU_THE = Color.WHITE;                 // nen o, the
    public static final Color MAU_CHU = new Color(0x1F2328);
    public static final Color MAU_CHU_PHU = new Color(0x6A7280);
    public static final Color MAU_VIEN = new Color(0xD9DCE1);
    public static final Color MAU_THANH_CONG = new Color(0x1E7F3E);
    public static final Color MAU_CANH_BAO = new Color(0xC0601A);

    // Mau trang thai ghe (Trung dung cho SoDoGhePanel)
    public static final Color GHE_TRONG = new Color(0xE9EEF5);
    public static final Color GHE_DANG_CHON = new Color(0x2E7D32);
    public static final Color GHE_DA_DAT = new Color(0x9AA0A6);
    public static final Color GHE_VIP = new Color(0xF3C969);
    public static final Color GHE_DOI = new Color(0xE7A6C9);

    private static final String HO_FONT = "Segoe UI";
    public static final Font FONT = new Font(HO_FONT, Font.PLAIN, 14);
    public static final Font FONT_DAM = FONT.deriveFont(Font.BOLD);
    public static final Font FONT_TIEU_DE = new Font(HO_FONT, Font.BOLD, 22);
    public static final Font FONT_TIEU_DE_NHO = new Font(HO_FONT, Font.BOLD, 16);
    public static final Font FONT_CODE = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");

    private Theme() { }

    /** Goi 1 lan khi khoi dong (trong Main), truoc khi tao cua so nao. */
    public static void apDung() {
        try {
            // FlatLaf co trong pom.xml; dung ten lop de van chay duoc neu thieu thu vien
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            UIManager.put("Component.arc", 8);
            UIManager.put("Button.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Component.focusColor", MAU_CHINH);
            UIManager.put("Table.selectionBackground", new Color(0xF6D5D2));
            UIManager.put("Table.selectionForeground", MAU_CHU);
        } catch (Exception khongCoFlatLaf) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // giu giao dien mac dinh cua Java
            }
        }
        UIManager.put("defaultFont", FONT);
        for (String k : new String[] {"Label.font", "Button.font", "TextField.font", "PasswordField.font",
                "ComboBox.font", "Table.font", "TableHeader.font", "Menu.font", "MenuItem.font",
                "CheckBox.font", "RadioButton.font", "Spinner.font", "TitledBorder.font", "TextArea.font",
                "OptionPane.messageFont", "OptionPane.buttonFont", "ToggleButton.font", "List.font"}) {
            UIManager.put(k, FONT);
        }
    }

    /** Nut hanh dong chinh (Tim, Thanh toan, Luu...). */
    public static JButton nutChinh(String text) {
        JButton b = new JButton(text);
        b.setBackground(MAU_CHINH);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_DAM);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "default");
        return b;
    }

    /** Nut phu (Huy, Quay lai, Xoa loc...). */
    public static JButton nutPhu(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JLabel tieuDe(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TIEU_DE);
        l.setForeground(MAU_CHU);
        return l;
    }

    public static JLabel chuPhu(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MAU_CHU_PHU);
        return l;
    }

    /**
     * Nhan nho cho biet app dang chay MOCK hay JDBC (dat tren thanh tieu de mau do).
     * Vang = mock / loi ket noi, xanh = dang dung SQL Server. Re chuot vao de xem ly do.
     */
    public static JLabel nhanCheDo() {
        boolean canhBao = CheDoChay.isMock() || CheDoChay.isLoiKetNoi();
        // Dung "●" (Segoe UI co san ky tu nay); "✔"/"⚠" hien thanh o vuong vi font khong co glyph
        JLabel l = new JLabel((canhBao ? "! " : "● ") + CheDoChay.dongThongBao());
        l.setOpaque(true);
        l.setFont(FONT_DAM.deriveFont(12f));
        l.setBackground(canhBao ? new Color(0xFFE08A) : new Color(0xD4F5DD));
        l.setForeground(canhBao ? new Color(0x5C3B00) : new Color(0x0F5128));
        l.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        l.setToolTipText(CheDoChay.lyDo());
        return l;
    }

    /** Le trong deu quanh noi dung man hinh. */
    public static Border le(int px) {
        return BorderFactory.createEmptyBorder(px, px, px, px);
    }

    public static Border khung(String tieuDe) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(MAU_VIEN), tieuDe),
                le(8));
    }

    /** Dinh dang chung cho moi JTable. */
    public static void dinhDangBang(JTable t) {
        t.setRowHeight(30);
        t.setFillsViewportHeight(true);
        t.setShowVerticalLines(false);
        t.getTableHeader().setFont(FONT_DAM);
        t.getTableHeader().setReorderingAllowed(false);
        t.setAutoCreateRowSorter(true);
    }

    /** 75000 -> "75.000 đ" */
    public static String tien(BigDecimal soTien) {
        if (soTien == null) {
            return "";
        }
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
        return nf.format(soTien) + " đ";
    }

    public static String ngayGio(LocalDateTime t) {
        return t == null ? "" : NGAY_GIO.format(t);
    }

    public static String gio(LocalDateTime t) {
        return t == null ? "" : GIO.format(t);
    }
}


// ==========================================================================================
// LoginFrame   (truoc day: src/main/java/com/nhom/datve/ui/LoginFrame.java)
// ==========================================================================================

/**
 * Cua so dang nhap chinh thuc cua ung dung.
 * Main.java tu dong goi constructor khong tham so LoginFrame().
 * Dang nhap thanh cong -> new MainFrame(phien).setVisible(true); dispose();
 */
class LoginFrame extends JFrame {
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
        JLabel lblTieuDe = new JLabel("RẠP CHIẾU PHIM PTIT");
        lblTieuDe.setFont(Theme.FONT_TIEU_DE);
        lblTieuDe.setForeground(Color.WHITE);
        JLabel lblMoTa = new JLabel("Hệ thống đặt vé xem phim trực tuyến");
        lblMoTa.setForeground(new Color(0xFFE3E0));
        JLabel lblCheDo = Theme.nhanCheDo();
        banner.add(lblTieuDe, BorderLayout.NORTH);
        banner.add(lblMoTa, BorderLayout.CENTER);
        banner.add(lblCheDo, BorderLayout.SOUTH);
        goc.add(banner, BorderLayout.NORTH);

        // Form nhap
        JPanel the = new JPanel(new BorderLayout(0, 14));
        the.setBackground(Theme.MAU_THE);
        the.setBorder(Theme.le(24));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 6, 8, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JLabel lblTieuDeForm = Theme.tieuDe("Đăng nhập tài khoản");
        // Dem tren/duoi de dau chong (ậ, ả, ẩ) khong bi cat khi Windows phong to 125-200%
        lblTieuDeForm.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));
        form.add(lblTieuDeForm, c);

        c.gridy = 1;
        // Tach 2 dong de khong bi cat chu khi cua so hep
        form.add(Theme.chuPhu("<html>Tài khoản mẫu:<br>"
                + "• khach / 123456 (Khách hàng)&nbsp;&nbsp;&nbsp;• admin / admin123 (Nhân viên)</html>"), c);

        c.gridwidth = 1;
        c.gridy = 2;
        c.gridx = 0;
        c.weightx = 0;
        JLabel lblTen = new JLabel("Tên đăng nhập");
        lblTen.setFont(Theme.FONT_DAM);
        lblTen.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 8));
        form.add(lblTen, c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(txtTenDangNhap, c);

        c.gridy = 3;
        c.gridx = 0;
        c.weightx = 0;
        JLabel lblMK = new JLabel("Mật khẩu");
        lblMK.setFont(Theme.FONT_DAM);
        lblMK.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 8));
        form.add(lblMK, c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(txtMatKhau, c);

        the.add(form, BorderLayout.CENTER);

        // Khu vuc nut
        // O nhap cao hon mot chut de chu khong bi sat vien
        for (JComponent o : new JComponent[] {txtTenDangNhap, txtMatKhau}) {
            Dimension d = o.getPreferredSize();
            o.setPreferredSize(new Dimension(Math.max(d.width, 260), Math.max(d.height, 34)));
        }
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
        // Tu tinh kich thuoc theo noi dung (pack) thay vi co dinh 500x390 -> khong bi che chu
        pack();
        setSize(new Dimension(Math.max(getWidth(), 560), getHeight()));
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


// ==========================================================================================
// DangKyDialog   (truoc day: src/main/java/com/nhom/datve/ui/DangKyDialog.java)
// ==========================================================================================

/**
 * Hop thoai dang ky tai khoan khach hang moi.
 * Kiem tra du lieu nhap va bao loi bang JOptionPane; thao tac CSDL chay trong SwingWorker.
 */
class DangKyDialog extends JDialog {
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


// ==========================================================================================
// DangNhapTamDialog   (truoc day: src/main/java/com/nhom/datve/ui/DangNhapTamDialog.java)
// ==========================================================================================

/**
 * Cua so dang nhap TAM de chay thu MainFrame truoc khi co LoginFrame cua Trung.
 * Khi co lop com.nhom.datve.LoginFrame, Main tu dung LoginFrame va lop nay co the xoa.
 */
class DangNhapTamDialog extends JDialog {
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


// ==========================================================================================
// MainFrame   (truoc day: src/main/java/com/nhom/datve/ui/MainFrame.java)
// ==========================================================================================

/**
 * Cua so chinh sau khi dang nhap.
 *  - CardLayout: moi man hinh (JPanel) la 1 "the"; chuyenDen() lat sang the khac, khong mo cua so moi.
 *  - JMenuBar theo vai tro: khach hang chi thay menu dat ve; nhan vien chi thay menu quan tri.
 *  - Man hinh duoc tao lan dau khi can (lazy) theo ten lop trong enum ManHinh.
 */
class MainFrame extends JFrame implements DieuHuong {
    private static final long serialVersionUID = 1L;

    private final transient PhienDangNhap phien;
    private final CardLayout cards = new CardLayout();
    private final JPanel noiDung = new JPanel(cards);
    private final Map<ManHinh, JComponent> daTao = new EnumMap<>(ManHinh.class);
    private final Deque<DaXem> lichSu = new ArrayDeque<>();
    private final JLabel lblManHinh = new JLabel();
    private ManHinh hienTai;
    private transient Object thamSoHienTai;

    /** Mot buoc trong lich su dieu huong, de "Quay lai" mo dung man hinh voi dung tham so. */
    private record DaXem(ManHinh manHinh, Object thamSo) { }

    public MainFrame(PhienDangNhap phien) {
        super("Đặt vé xem phim");
        this.phien = phien;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new java.awt.Dimension(960, 620));
        setLocationRelativeTo(null);

        setJMenuBar(taoMenu());
        JPanel goc = new JPanel(new BorderLayout());
        goc.add(taoThanhTieuDe(), BorderLayout.NORTH);
        noiDung.setBackground(Theme.MAU_NEN);
        goc.add(noiDung, BorderLayout.CENTER);
        setContentPane(goc);

        chuyenDen(phien.isAdmin() ? ManHinh.QL_PHIM : ManHinh.DANH_SACH_PHIM, null);
    }

    // ------------------------------------------------------------------ giao dien khung

    private JMenuBar taoMenu() {
        JMenuBar bar = new JMenuBar();
        if (phien.isAdmin()) {
            JMenu quanTri = new JMenu("Quản trị");
            quanTri.setMnemonic(KeyEvent.VK_Q);
            themMuc(quanTri, ManHinh.QL_PHIM, KeyEvent.VK_1);
            themMuc(quanTri, ManHinh.QL_PHONG, KeyEvent.VK_2);
            themMuc(quanTri, ManHinh.QL_SUAT_CHIEU, KeyEvent.VK_3);
            quanTri.addSeparator();
            themMuc(quanTri, ManHinh.THONG_KE, KeyEvent.VK_4);
            bar.add(quanTri);
        } else {
            JMenu datVe = new JMenu("Đặt vé");
            datVe.setMnemonic(KeyEvent.VK_D);
            themMuc(datVe, ManHinh.DANH_SACH_PHIM, KeyEvent.VK_1);
            themMuc(datVe, ManHinh.TIM_KIEM_PHIM, KeyEvent.VK_F);
            bar.add(datVe);
            JMenu ve = new JMenu("Vé của tôi");
            ve.setMnemonic(KeyEvent.VK_V);
            themMuc(ve, ManHinh.LICH_SU_VE, KeyEvent.VK_L);
            bar.add(ve);
        }
        JMenu taiKhoan = new JMenu("Tài khoản");
        taiKhoan.setMnemonic(KeyEvent.VK_T);
        JMenuItem thongTin = new JMenuItem("Đang đăng nhập: " + phien.getTaiKhoan().getTenDangNhap());
        thongTin.setEnabled(false);
        taiKhoan.add(thongTin);
        taiKhoan.addSeparator();
        JMenuItem dangXuat = new JMenuItem("Đăng xuất");
        dangXuat.addActionListener(e -> dangXuat());
        taiKhoan.add(dangXuat);
        JMenuItem thoat = new JMenuItem("Thoát");
        thoat.addActionListener(e -> System.exit(0));
        taiKhoan.add(thoat);
        bar.add(taiKhoan);
        return bar;
    }

    private void themMuc(JMenu menu, ManHinh mh, int phimTat) {
        JMenuItem item = new JMenuItem(mh.getTieuDe());
        item.setAccelerator(KeyStroke.getKeyStroke(phimTat, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(e -> chuyenDen(mh, null));
        menu.add(item);
    }

    private JPanel taoThanhTieuDe() {
        JPanel thanh = new JPanel(new BorderLayout());
        thanh.setBackground(Theme.MAU_CHINH);
        thanh.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 14));

        JLabel ten = new JLabel("RẠP PHIM  ·  ");
        ten.setFont(Theme.FONT_TIEU_DE_NHO);
        ten.setForeground(Color.WHITE);
        lblManHinh.setFont(Theme.FONT_TIEU_DE_NHO);
        lblManHinh.setForeground(new Color(0xFFE3E0));
        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        trai.setOpaque(false);
        trai.add(ten);
        trai.add(lblManHinh);

        String vaiTro = phien.isAdmin() ? "Quản trị viên" : "Khách hàng";
        JLabel xinChao = new JLabel("Xin chào, " + phien.getTenHienThi() + "  (" + vaiTro + ")");
        xinChao.setForeground(Color.WHITE);
        JButton btnDangXuat = Theme.nutPhu("Đăng xuất");
        btnDangXuat.addActionListener(e -> dangXuat());
        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        phai.setOpaque(false);
        phai.add(Theme.nhanCheDo());
        phai.add(xinChao);
        phai.add(btnDangXuat);

        thanh.add(trai, BorderLayout.WEST);
        thanh.add(phai, BorderLayout.EAST);
        return thanh;
    }

    // ------------------------------------------------------------------ DieuHuong

    @Override
    public void chuyenDen(ManHinh mh, Object thamSo) {
        VaiTro canCo = mh.getVaiTro();
        if (canCo != phien.getTaiKhoan().getVaiTro()) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền mở màn hình \"" + mh.getTieuDe() + "\".",
                    "Không có quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JComponent manHinh = daTao.computeIfAbsent(mh, this::taoManHinh);
        if (manHinh.getParent() != noiDung) {
            noiDung.add(manHinh, mh.name());
        }
        if (hienTai != null && hienTai != mh) {
            lichSu.push(new DaXem(hienTai, thamSoHienTai));
        }
        hienTai = mh;
        thamSoHienTai = thamSo;
        cards.show(noiDung, mh.name());
        lblManHinh.setText(mh.getTieuDe());
        if (manHinh instanceof ManHinhCon con) {
            con.khiHienThi(thamSo);
        }
    }

    @Override
    public void quayLai() {
        if (lichSu.isEmpty()) {
            return;
        }
        DaXem truoc = lichSu.pop();
        hienTai = null;                       // khong day man hinh dang roi khoi vao lich su
        chuyenDen(truoc.manHinh(), truoc.thamSo());
    }

    @Override
    public void dangXuat() {
        int chon = JOptionPane.showConfirmDialog(this, "Bạn muốn đăng xuất?", "Đăng xuất",
                JOptionPane.YES_NO_OPTION);
        if (chon == JOptionPane.YES_OPTION) {
            dispose();
            Main.moManHinhDangNhap();
        }
    }

    @Override
    public PhienDangNhap getPhien() { return phien; }

    @Override
    public Window getCuaSo() { return this; }

    /** Man hinh dang hien (dung khi kiem thu). */
    public ManHinh getManHinhHienTai() { return hienTai; }

    // ------------------------------------------------------------------ tao man hinh

    /** Tao man hinh theo ten lop; lop chua co thi tra ve man hinh "dang lam". */
    private JComponent taoManHinh(ManHinh mh) {
        try {
            Class<?> lop = Class.forName(mh.getTenLop());
            Object obj;
            try {
                obj = lop.getConstructor(DieuHuong.class).newInstance(this);
            } catch (NoSuchMethodException e) {
                obj = lop.getConstructor().newInstance();
            }
            return (JComponent) obj;
        } catch (ClassNotFoundException e) {
            return new ManHinhChoPanel(mh, this, null);
        } catch (ReflectiveOperationException | ClassCastException e) {
            Throwable goc = e.getCause() != null ? e.getCause() : e;
            goc.printStackTrace();
            return new ManHinhChoPanel(mh, this, goc);
        }
    }
}


// ==========================================================================================
// DieuHuong   (truoc day: src/main/java/com/nhom/datve/ui/DieuHuong.java)
// ==========================================================================================

/**
 * Nhung gi mot man hinh con duoc phep yeu cau khung chinh lam. MainFrame cai dat interface nay
 * va truyen chinh no vao constructor cua tung man hinh.
 *
 * Vi du trong DanhSachPhimPanel: dieuHuong.chuyenDen(ManHinh.CHON_SUAT_CHIEU, phim.getMaPhim());
 */
interface DieuHuong {
    void chuyenDen(ManHinh manHinh, Object thamSo);

    default void chuyenDen(ManHinh manHinh) { chuyenDen(manHinh, null); }

    /** Ve man hinh truoc do (nut "Quay lai"). */
    void quayLai();

    void dangXuat();

    PhienDangNhap getPhien();

    /** Cua so cha, dung lam owner cho JDialog/JOptionPane. */
    Window getCuaSo();
}


// ==========================================================================================
// ManHinh   (truoc day: src/main/java/com/nhom/datve/ui/ManHinh.java)
// ==========================================================================================

/**
 * Danh sach man hinh gan vao MainFrame. MainFrame tao man hinh theo TEN LOP o day
 * (constructor nhan DieuHuong, hoac khong tham so). Lop chua co -> hien man hinh "dang lam".
 * Nho vay moi nguoi them man hinh cua minh ma khong phai sua MainFrame.
 *
 * Tham so khi chuyen man hinh (DieuHuong.chuyenDen):
 *   CHON_SUAT_CHIEU -> Integer maPhim
 *   SO_DO_GHE       -> Integer maSuat
 *   cac man hinh khac -> null
 */
enum ManHinh {
    DANH_SACH_PHIM("Phim đang chiếu", "DanhSachPhimPanel", "Trung", VaiTro.KHACHHANG),
    TIM_KIEM_PHIM("Tìm kiếm phim", "TimKiemPhimPanel", "Khuê", VaiTro.KHACHHANG),
    CHON_SUAT_CHIEU("Chọn suất chiếu", "ChonSuatChieuPanel", "Trung", VaiTro.KHACHHANG),
    SO_DO_GHE("Chọn ghế", "SoDoGhePanel", "Trung", VaiTro.KHACHHANG),
    LICH_SU_VE("Vé của tôi", "LichSuVePanel", "Trung", VaiTro.KHACHHANG),
    QL_PHIM("Quản lý phim", "QuanLyPhimPanel", "Khánh", VaiTro.NHANVIEN),
    QL_PHONG("Phòng chiếu và ghế", "QuanLyPhongPanel", "Khánh", VaiTro.NHANVIEN),
    QL_SUAT_CHIEU("Lịch suất chiếu", "QuanLySuatChieuPanel", "Khánh", VaiTro.NHANVIEN),
    THONG_KE("Thống kê doanh thu", "ThongKePanel", "Khánh", VaiTro.NHANVIEN);

    private final String tieuDe;
    private final String tenLop;
    private final String nguoiLam;
    private final VaiTro vaiTro;

    ManHinh(String tieuDe, String lopTuongDoi, String nguoiLam, VaiTro vaiTro) {
        this.tieuDe = tieuDe;
        this.tenLop = "com.nhom.datve." + lopTuongDoi;
        this.nguoiLam = nguoiLam;
        this.vaiTro = vaiTro;
    }

    public String getTieuDe() { return tieuDe; }
    public String getTenLop() { return tenLop; }
    public String getNguoiLam() { return nguoiLam; }
    public VaiTro getVaiTro() { return vaiTro; }
}


// ==========================================================================================
// ManHinhChoPanel   (truoc day: src/main/java/com/nhom/datve/ui/ManHinhChoPanel.java)
// ==========================================================================================

/**
 * Hien thay cho man hinh chua co lop (dang duoc thanh vien khac lam) hoac bi loi khi tao.
 * Rieng SO_DO_GHE co nut thu de chay duoc luong dat ve truoc khi SoDoGhePanel xong.
 */
class ManHinhChoPanel extends JPanel implements ManHinhCon {
    private static final long serialVersionUID = 1L;

    private final ManHinh manHinh;
    private final transient DieuHuong dieuHuong;
    private final JLabel lblThamSo = Theme.chuPhu(" ");
    private final JButton btnThu = Theme.nutChinh("Thử đặt 2 ghế trống đầu tiên");
    private Integer maSuat;

    ManHinhChoPanel(ManHinh manHinh, DieuHuong dieuHuong, Throwable loi) {
        super(new GridBagLayout());
        this.manHinh = manHinh;
        this.dieuHuong = dieuHuong;
        setBackground(Theme.MAU_NEN);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(6, 6, 6, 6);

        add(Theme.tieuDe(manHinh.getTieuDe()), c);
        String moTa = loi == null
                ? "Màn hình này do " + manHinh.getNguoiLam() + " phụ trách, chưa có lớp " + manHinh.getTenLop() + "."
                : "Lỗi khi tạo " + manHinh.getTenLop() + ": " + loi;
        JLabel lbl = new JLabel(moTa, SwingConstants.CENTER);
        lbl.setForeground(loi == null ? Theme.MAU_CHU_PHU : Theme.MAU_CHINH);
        add(lbl, c);
        add(lblThamSo, c);
        if (manHinh == ManHinh.SO_DO_GHE) {
            btnThu.addActionListener(e -> thuDatVe());
            btnThu.setEnabled(false);
            add(btnThu, c);
        }
        JButton quayLai = Theme.nutPhu("Quay lại");
        quayLai.addActionListener(e -> dieuHuong.quayLai());
        add(quayLai, c);
    }

    @Override
    public void khiHienThi(Object thamSo) {
        lblThamSo.setText(thamSo == null ? " " : "Tham số nhận được: " + thamSo);
        maSuat = thamSo instanceof Integer i ? i : null;
        btnThu.setEnabled(maSuat != null);
    }

    private void thuDatVe() {
        try {
            DatVeService service = new DatVeService();
            SoDoGhe soDo = service.laySoDoGhe(maSuat);
            List<Ghe> trong = soDo.getDsGhe().stream().filter(g -> !soDo.isDaDat(g)).limit(2).toList();
            Optional<DatVe> kq = XacNhanDatVeDialog.hienThi(dieuHuong.getCuaSo(), dieuHuong.getPhien().getMaKH(),
                    soDo.getSuatChieu(), trong);
            if (kq.isPresent()) {
                dieuHuong.chuyenDen(ManHinh.LICH_SU_VE);
            }
        } catch (NghiepVuException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể đặt vé", JOptionPane.WARNING_MESSAGE);
        }
    }

    ManHinh getManHinh() { return manHinh; }
}


// ==========================================================================================
// ManHinhCon   (truoc day: src/main/java/com/nhom/datve/ui/ManHinhCon.java)
// ==========================================================================================

/**
 * Man hinh can nap lai du lieu moi lan duoc hien thi thi implement interface nay.
 * MainFrame goi khiHienThi(thamSo) ngay sau khi chuyen toi man hinh.
 */
interface ManHinhCon {
    void khiHienThi(Object thamSo);
}
