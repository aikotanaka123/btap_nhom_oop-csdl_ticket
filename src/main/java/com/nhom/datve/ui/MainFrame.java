package com.nhom.datve.ui;

import com.nhom.datve.Main;
import com.nhom.datve.model.VaiTro;
import com.nhom.datve.util.AppConfig;
import com.nhom.datve.util.PhienDangNhap;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

/**
 * Cua so chinh sau khi dang nhap.
 *  - CardLayout: moi man hinh (JPanel) la 1 "the"; chuyenDen() lat sang the khac, khong mo cua so moi.
 *  - JMenuBar theo vai tro: khach hang chi thay menu dat ve; nhan vien chi thay menu quan tri.
 *  - Man hinh duoc tao lan dau khi can (lazy) theo ten lop trong enum ManHinh.
 */
public class MainFrame extends JFrame implements DieuHuong {
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
        JLabel xinChao = new JLabel("Xin chào, " + phien.getTenHienThi() + "  (" + vaiTro
                + (AppConfig.isMockMode() ? " · dữ liệu giả" : "") + ")");
        xinChao.setForeground(Color.WHITE);
        JButton btnDangXuat = Theme.nutPhu("Đăng xuất");
        btnDangXuat.addActionListener(e -> dangXuat());
        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        phai.setOpaque(false);
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
