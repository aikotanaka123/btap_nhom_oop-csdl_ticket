package com.nhom.datve;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/*
 * GiaoDienKhachHang.java: Cac man hinh cua KHACH HANG: xem phim, tim kiem, chon suat, chon ghe, dat ve, lich su ve.
 * Gom cac lop: DanhSachPhimPanel, ChiTietPhimDialog, TimKiemPhimPanel, ChonSuatChieuPanel, SoDoGhePanel, XacNhanDatVeDialog, LichSuVePanel
 */

// ==========================================================================================
// DanhSachPhimPanel   (truoc day: src/main/java/com/nhom/datve/ui/khachhang/DanhSachPhimPanel.java)
// ==========================================================================================

/**
 * Man hinh danh sach phim va chi tiet phim.
 * - Chi goi tang Service (PhimService), khong goi DAO truc tiep.
 * - Dung mau/font chuan tu Theme.
 * - Hien thi luoi the phim: moi the co poster (ImageIcon), ten phim, the loai, thoi luong, do tuoi.
 * - Bam vao the phim hoac nut "Chi tiet phim" -> mo ChiTietPhimDialog.
 * - Bam "Chon suat chieu" -> dieuHuong.chuyenDen(ManHinh.CHON_SUAT_CHIEU, phim.getMaPhim()).
 */
class DanhSachPhimPanel extends JPanel implements ManHinhCon {
    private static final long serialVersionUID = 1L;
    private static final String TAT_CA_THE_LOAI = "Tất cả thể loại";
    private static final Color[] BANG_MAU_POSTER = {
        Theme.MAU_CHINH,
        Theme.MAU_CHINH_DAM,
        Theme.MAU_THANH_CONG,
        Theme.MAU_CANH_BAO,
        Theme.MAU_CHU
    };

    private final transient DieuHuong dieuHuong;
    private final transient PhimService phimService = new PhimService();

    private final JTextField txtTuKhoa = new JTextField(18);
    private final JComboBox<Object> cboTheLoai = new JComboBox<>();
    private final JPanel luoiPhim = new JPanel(new GridLayout(0, 3, 16, 16));
    private final JLabel lblTrangThai = Theme.chuPhu(" ");

    public DanhSachPhimPanel(DieuHuong dieuHuong) {
        super(new BorderLayout(0, 12));
        this.dieuHuong = dieuHuong;
        setBackground(Theme.MAU_NEN);
        setBorder(Theme.le(16));

        add(taoThanhDau(), BorderLayout.NORTH);

        luoiPhim.setOpaque(false);
        JPanel boc = new JPanel(new BorderLayout());
        boc.setBackground(Theme.MAU_NEN);
        boc.add(luoiPhim, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(boc);
        cuon.setBorder(BorderFactory.createEmptyBorder());
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        add(cuon, BorderLayout.CENTER);

        add(lblTrangThai, BorderLayout.SOUTH);
        napTheLoai();
    }

    private JPanel taoThanhDau() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        trai.setOpaque(false);
        trai.add(Theme.tieuDe("Phim đang chiếu"));
        p.add(trai, BorderLayout.WEST);

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);
        JButton btnLoc = Theme.nutChinh("Lọc phim");
        JButton btnTimNangCao = Theme.nutPhu("Tìm kiếm nâng cao & Demo SQL");
        btnLoc.addActionListener(e -> napPhim());
        txtTuKhoa.addActionListener(e -> napPhim());
        cboTheLoai.addActionListener(e -> napPhim());
        btnTimNangCao.addActionListener(e -> dieuHuong.chuyenDen(ManHinh.TIM_KIEM_PHIM));

        phai.add(new JLabel("Tên phim:"));
        phai.add(txtTuKhoa);
        phai.add(new JLabel("Thể loại:"));
        phai.add(cboTheLoai);
        phai.add(btnLoc);
        phai.add(btnTimNangCao);
        p.add(phai, BorderLayout.EAST);
        return p;
    }

    @Override
    public void khiHienThi(Object thamSo) {
        napPhim();
    }

    private void napTheLoai() {
        new SwingWorker<List<TheLoai>, Void>() {
            @Override
            protected List<TheLoai> doInBackground() {
                return phimService.layTatCaTheLoai();
            }

            @Override
            protected void done() {
                try {
                    DefaultComboBoxModel<Object> m = new DefaultComboBoxModel<>();
                    m.addElement(TAT_CA_THE_LOAI);
                    get().forEach(m::addElement);
                    cboTheLoai.setModel(m);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable goc = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(DanhSachPhimPanel.this,
                            "Không tải được thể loại: " + goc.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void napPhim() {
        String tuKhoa = txtTuKhoa.getText();
        Object chonTL = cboTheLoai.getSelectedItem();
        Integer maTL = chonTL instanceof TheLoai t ? t.getMaTL() : null;

        lblTrangThai.setText("Đang tải danh sách phim...");
        new SwingWorker<List<Phim>, Void>() {
            @Override
            protected List<Phim> doInBackground() {
                return phimService.layPhimDangChieu(tuKhoa, maTL);
            }

            @Override
            protected void done() {
                try {
                    List<Phim> ds = get();
                    veDanhSachThePhim(ds);
                    lblTrangThai.setText("Hiển thị " + ds.size() + " phim đang chiếu. Bấm vào thẻ phim để xem chi tiết.");
                } catch (ExecutionException ex) {
                    Throwable goc = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(DanhSachPhimPanel.this,
                            "Không tải được danh sách phim: " + goc.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void veDanhSachThePhim(List<Phim> dsPhim) {
        luoiPhim.removeAll();
        if (dsPhim.isEmpty()) {
            luoiPhim.add(Theme.chuPhu("Không tìm thấy phim phù hợp."));
        } else {
            for (int i = 0; i < dsPhim.size(); i++) {
                Phim phim = dsPhim.get(i);
                Color mau = BANG_MAU_POSTER[i % BANG_MAU_POSTER.length];
                luoiPhim.add(taoThePhim(phim, mau));
            }
        }
        luoiPhim.revalidate();
        luoiPhim.repaint();
    }

    private JPanel taoThePhim(Phim phim, Color mauPoster) {
        JPanel the = new JPanel(new BorderLayout(0, 10));
        the.setBackground(Theme.MAU_THE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.MAU_VIEN),
                Theme.le(12)));

        ImageIcon posterIcon = taoPosterIcon(phim, mauPoster, 260, 165);
        JLabel lblPoster = new JLabel(posterIcon, SwingConstants.CENTER);
        lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        the.add(lblPoster, BorderLayout.NORTH);

        JPanel thongTin = new JPanel();
        thongTin.setLayout(new BoxLayout(thongTin, BoxLayout.Y_AXIS));
        thongTin.setOpaque(false);

        JLabel lblTen = new JLabel(phim.getTenPhim());
        lblTen.setFont(Theme.FONT_TIEU_DE_NHO);
        lblTen.setForeground(Theme.MAU_CHU);

        String chuoiTheLoai = phim.getTheLoais() == null || phim.getTheLoais().isEmpty()
                ? "Chưa phân loại"
                : phim.getTheLoais().stream().map(TheLoai::getTenTL).collect(Collectors.joining(", "));
        JLabel lblTheLoai = Theme.chuPhu("Thể loại: " + chuoiTheLoai);

        String doTuoi = phim.getDoTuoi() != null ? "  ·  Độ tuổi: " + phim.getDoTuoi() : "";
        JLabel lblThoiLuong = Theme.chuPhu("Thời lượng: " + phim.getThoiLuong() + " phút" + doTuoi);

        thongTin.add(lblTen);
        thongTin.add(Box.createVerticalStrut(4));
        thongTin.add(lblTheLoai);
        thongTin.add(Box.createVerticalStrut(2));
        thongTin.add(lblThoiLuong);
        the.add(thongTin, BorderLayout.CENTER);

        JButton btnChiTiet = Theme.nutPhu("Chi tiết phim");
        JButton btnChonSuat = Theme.nutChinh("Chọn suất chiếu");
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        nut.setOpaque(false);
        nut.add(btnChiTiet);
        nut.add(btnChonSuat);
        the.add(nut, BorderLayout.SOUTH);

        Runnable moChiTiet = () -> {
            ChiTietPhimDialog dlg = new ChiTietPhimDialog(dieuHuong.getCuaSo(), phim, posterIcon);
            dlg.setVisible(true);
            if (dlg.isChonSuat()) {
                dieuHuong.chuyenDen(ManHinh.CHON_SUAT_CHIEU, phim.getMaPhim());
            }
        };

        lblPoster.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                moChiTiet.run();
            }
        });
        btnChiTiet.addActionListener(e -> moChiTiet.run());
        btnChonSuat.addActionListener(e -> dieuHuong.chuyenDen(ManHinh.CHON_SUAT_CHIEU, phim.getMaPhim()));
        return the;
    }

    private static ImageIcon taoPosterIcon(Phim phim, Color mauChuDao, int w, int h) {
        if (phim.getPoster() != null && !phim.getPoster().isBlank()) {
            File f = new File(phim.getPoster());
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                if (icon.getIconWidth() > 0) {
                    return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
                }
            }
            URL res = DanhSachPhimPanel.class.getClassLoader().getResource(phim.getPoster());
            if (res != null) {
                ImageIcon icon = new ImageIcon(res);
                if (icon.getIconWidth() > 0) {
                    return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
                }
            }
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(0, 0, mauChuDao, w, h, mauChuDao.darker()));
        g2.fillRoundRect(0, 0, w, h, 12, 12);

        String doTuoi = phim.getDoTuoi() != null ? phim.getDoTuoi() : "P";
        g2.setColor(Theme.MAU_CHU);
        g2.fillRoundRect(w - 48, 10, 38, 24, 6, 6);
        g2.setColor(Color.WHITE);
        g2.setFont(Theme.FONT_DAM);
        g2.drawString(doTuoi, w - 41, 27);

        g2.setFont(Theme.FONT_TIEU_DE_NHO);
        String ten = phim.getTenPhim() == null ? "Phim" : phim.getTenPhim();
        if (ten.length() > 22) {
            ten = ten.substring(0, 20) + "...";
        }
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(ten, Math.max(12, (w - fm.stringWidth(ten)) / 2), h / 2);

        g2.setFont(Theme.FONT);
        String phu = phim.getThoiLuong() + " phút";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(phu, Math.max(12, (w - fm2.stringWidth(phu)) / 2), h / 2 + 24);

        g2.dispose();
        return new ImageIcon(img);
    }
}


// ==========================================================================================
// ChiTietPhimDialog   (truoc day: src/main/java/com/nhom/datve/ui/khachhang/ChiTietPhimDialog.java)
// ==========================================================================================

/**
 * Hop thoai xem chi tiet phim.
 * Hien poster lon (ImageIcon), ten phim, the loai, thoi luong, dao dien, do tuoi,
 * ngay khoi chieu, mo ta va nut "Chon suat chieu".
 */
class ChiTietPhimDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter NGAY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private boolean chonSuat;

    public ChiTietPhimDialog(Window owner, Phim phim, ImageIcon posterNho) {
        super(owner, "Chi tiết phim – " + phim.getTenPhim(), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel goc = new JPanel(new BorderLayout(20, 14));
        goc.setBorder(Theme.le(20));
        goc.setBackground(Theme.MAU_THE);

        // Poster lon ben trai
        JLabel lblPoster = new JLabel(taoPosterLon(phim, posterNho));
        lblPoster.setVerticalAlignment(SwingConstants.TOP);
        goc.add(lblPoster, BorderLayout.WEST);

        // Thong tin chi tiet o giua
        JPanel thongTin = new JPanel();
        thongTin.setLayout(new BoxLayout(thongTin, BoxLayout.Y_AXIS));
        thongTin.setOpaque(false);

        JLabel lblTen = Theme.tieuDe(phim.getTenPhim());
        lblTen.setAlignmentX(Component.LEFT_ALIGNMENT);
        thongTin.add(lblTen);
        thongTin.add(Box.createVerticalStrut(10));

        String dsTheLoai = phim.getTheLoais() == null || phim.getTheLoais().isEmpty()
                ? "Đang cập nhật"
                : phim.getTheLoais().stream().map(TheLoai::getTenTL).collect(Collectors.joining(", "));

        thongTin.add(dongThongTin("Thể loại:  ", dsTheLoai));
        thongTin.add(Box.createVerticalStrut(6));
        thongTin.add(dongThongTin("Thời lượng:  ", phim.getThoiLuong() + " phút"));
        thongTin.add(Box.createVerticalStrut(6));
        thongTin.add(dongThongTin("Đạo diễn:  ",
                phim.getDaoDien() != null && !phim.getDaoDien().isBlank() ? phim.getDaoDien() : "Đang cập nhật"));
        thongTin.add(Box.createVerticalStrut(6));
        thongTin.add(dongThongTin("Giới hạn tuổi:  ",
                phim.getDoTuoi() != null && !phim.getDoTuoi().isBlank() ? phim.getDoTuoi() : "P"));
        thongTin.add(Box.createVerticalStrut(6));
        thongTin.add(dongThongTin("Khởi chiếu:  ",
                phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(NGAY_FMT) : "Đang chiếu"));
        thongTin.add(Box.createVerticalStrut(12));

        JLabel lblMoTa = new JLabel("Nội dung phim");
        lblMoTa.setFont(Theme.FONT_DAM);
        lblMoTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        thongTin.add(lblMoTa);
        thongTin.add(Box.createVerticalStrut(4));

        JTextArea txtMoTa = new JTextArea(
                phim.getMoTa() != null && !phim.getMoTa().isBlank()
                        ? phim.getMoTa()
                        : "Chưa có mô tả chi tiết cho phim này.");
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        txtMoTa.setEditable(false);
        txtMoTa.setBackground(Theme.MAU_NEN);
        txtMoTa.setBorder(Theme.le(8));
        JScrollPane cuonMoTa = new JScrollPane(txtMoTa);
        cuonMoTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        thongTin.add(cuonMoTa);

        goc.add(thongTin, BorderLayout.CENTER);

        // Nut duoi
        JButton btnDong = Theme.nutPhu("Đóng");
        btnDong.addActionListener(e -> dispose());
        JButton btnDatVe = Theme.nutChinh("Chọn suất chiếu  →");
        btnDatVe.addActionListener(e -> {
            chonSuat = true;
            dispose();
        });
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.setOpaque(false);
        nut.add(btnDong);
        nut.add(btnDatVe);
        goc.add(nut, BorderLayout.SOUTH);

        setContentPane(goc);
        getRootPane().setDefaultButton(btnDatVe);
        setSize(new Dimension(640, 440));
        setLocationRelativeTo(owner);
    }

    public boolean isChonSuat() {
        return chonSuat;
    }

    private static JPanel dongThongTin(String nhan, String giaTri) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(Theme.chuPhu(nhan));
        JLabel v = new JLabel(giaTri);
        v.setFont(Theme.FONT_DAM);
        p.add(v);
        return p;
    }

    private static ImageIcon taoPosterLon(Phim phim, ImageIcon posterNho) {
        int w = 185;
        int h = 260;
        if (posterNho != null && posterNho.getIconWidth() > 0) {
            Image scaled = posterNho.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, Theme.MAU_CHINH, w, h, Theme.MAU_CHINH_DAM));
        g2.fillRoundRect(0, 0, w, h, 14, 14);
        g2.setColor(Color.WHITE);
        g2.setFont(Theme.FONT_TIEU_DE_NHO);
        g2.drawString("POSTER", 45, 115);
        g2.dispose();
        return new ImageIcon(img);
    }
}


// ==========================================================================================
// TimKiemPhimPanel   (truoc day: src/main/java/com/nhom/datve/ui/khachhang/TimKiemPhimPanel.java)
// ==========================================================================================

/**
 * Man hinh tim kiem phim (Khue).
 *  - Loc suat chieu sap toi theo ten phim, the loai, ngay chieu, gia toi da; ket qua tren JTable.
 *  - Khung "Demo chuong 10" doi giua PreparedStatement va noi chuoi de minh hoa SQL injection.
 *  - Nhap doi vao 1 dong (hoac nut "Chon suat nay") -> sang SO_DO_GHE voi maSuat.
 * Giao dien khong chua SQL: SQL tao o TimKiemSql (tang dao), man hinh chi hien cau lenh tra ve.
 */
class TimKiemPhimPanel extends JPanel implements ManHinhCon {
    private static final long serialVersionUID = 1L;
    private static final String TAT_CA = "Tất cả thể loại";

    private final transient DieuHuong dieuHuong;
    private final transient TimKiemService service = new TimKiemService();

    private final JTextField txtTen = new JTextField(22);
    private final JComboBox<Object> cboTheLoai = new JComboBox<>();
    private final JCheckBox chkNgay = new JCheckBox("Ngày chiếu");
    private final JSpinner spnNgay = new JSpinner(new SpinnerDateModel());
    private final JCheckBox chkGia = new JCheckBox("Giá tối đa (đ)");
    private final JSpinner spnGia = new JSpinner(new SpinnerNumberModel(100_000, 0, 1_000_000, 5_000));
    private final JRadioButton rdoAnToan = new JRadioButton("PreparedStatement (tham số ?)", true);
    private final JRadioButton rdoNoiChuoi = new JRadioButton("Nối chuỗi bằng Statement (không an toàn)");
    private final JButton btnTim = Theme.nutChinh("Tìm kiếm");
    private final JButton btnChon = Theme.nutChinh("Chọn suất này  →");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"Phim", "Thể loại", "Suất chiếu", "Phòng", "Giá cơ bản"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable bang = new JTable(model);
    private final JTextArea txtSql = new JTextArea(4, 60);
    private final JLabel lblThamSo = Theme.chuPhu(" ");
    private final JLabel lblTrangThai = Theme.chuPhu(" ");
    private final JLabel lblGhiChu = new JLabel(" ");

    private transient List<SuatChieu> ketQua = new ArrayList<>();
    private boolean ketQuaAnToan = true;
    private boolean daNapLanDau;

    public TimKiemPhimPanel(DieuHuong dieuHuong) {
        super(new BorderLayout(0, 12));
        this.dieuHuong = dieuHuong;
        setBackground(Theme.MAU_NEN);
        setBorder(Theme.le(16));

        JPanel tren = new JPanel();
        tren.setOpaque(false);
        tren.setLayout(new BoxLayout(tren, BoxLayout.Y_AXIS));
        JPanel hangLoc = new JPanel(new BorderLayout(12, 0));
        hangLoc.setOpaque(false);
        hangLoc.add(taoBoLoc(), BorderLayout.CENTER);
        hangLoc.add(taoKhungDemo(), BorderLayout.EAST);
        tren.add(hangLoc);
        add(tren, BorderLayout.NORTH);

        add(taoKetQua(), BorderLayout.CENTER);
        add(taoChanTrang(), BorderLayout.SOUTH);
        napTheLoai();
    }

    // ------------------------------------------------------------------ dung giao dien

    private JPanel taoBoLoc() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.MAU_THE);
        p.setBorder(Theme.khung("Bộ lọc"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        spnNgay.setEditor(new JSpinner.DateEditor(spnNgay, "dd/MM/yyyy"));
        spnNgay.setValue(Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        spnNgay.setEnabled(false);
        spnGia.setEnabled(false);
        ((JSpinner.DefaultEditor) spnGia.getEditor()).getTextField().setColumns(8);
        chkNgay.setOpaque(false);
        chkGia.setOpaque(false);
        chkNgay.addActionListener(e -> spnNgay.setEnabled(chkNgay.isSelected()));
        chkGia.addActionListener(e -> spnGia.setEnabled(chkGia.isSelected()));

        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("Tên phim"), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        p.add(txtTen, c);
        c.gridx = 2;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        p.add(new JLabel("Thể loại"), c);
        c.gridx = 3;
        p.add(cboTheLoai, c);

        c.gridy = 1;
        c.gridx = 0;
        p.add(chkNgay, c);
        c.gridx = 1;
        p.add(spnNgay, c);
        c.gridx = 2;
        p.add(chkGia, c);
        c.gridx = 3;
        p.add(spnGia, c);

        JButton btnXoa = Theme.nutPhu("Xóa lọc");
        btnXoa.addActionListener(e -> xoaLoc());
        btnTim.addActionListener(e -> timKiem());
        txtTen.addActionListener(e -> timKiem());          // Enter trong o ten = tim
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.setOpaque(false);
        nut.add(btnXoa);
        nut.add(btnTim);
        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 4;
        c.anchor = GridBagConstraints.EAST;
        p.add(nut, c);
        return p;
    }

    private JPanel taoKhungDemo() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(0xFFF8EC));
        p.setBorder(Theme.khung("Demo chương 10 – Dynamic SQL"));
        ButtonGroup g = new ButtonGroup();
        g.add(rdoAnToan);
        g.add(rdoNoiChuoi);
        rdoAnToan.setOpaque(false);
        rdoNoiChuoi.setOpaque(false);
        rdoNoiChuoi.setForeground(Theme.MAU_CHINH);
        JButton btnTanCong = Theme.nutPhu("Điền chuỗi tấn công vào ô Tên phim");
        btnTanCong.addActionListener(e -> {
            txtTen.setText(TimKiemSql.CHUOI_TAN_CONG);
            txtTen.requestFocusInWindow();
        });
        p.add(rdoAnToan);
        p.add(rdoNoiChuoi);
        p.add(javax.swing.Box.createVerticalStrut(6));
        p.add(btnTanCong);
        return p;
    }

    private JSplitPane taoKetQua() {
        Theme.dinhDangBang(bang);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getColumnModel().getColumn(0).setPreferredWidth(220);
        bang.getColumnModel().getColumn(1).setPreferredWidth(200);
        bang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    chonSuat();
                }
            }
        });
        bang.getSelectionModel().addListSelectionListener(e -> capNhatNutChon());

        txtSql.setEditable(false);
        txtSql.setLineWrap(true);
        txtSql.setFont(Theme.FONT_CODE);
        txtSql.setBackground(new Color(0x1F2328));
        txtSql.setForeground(new Color(0xE6EDF3));
        txtSql.setBorder(Theme.le(8));
        JPanel khungSql = new JPanel(new BorderLayout(0, 4));
        khungSql.setOpaque(false);
        JLabel lbl = new JLabel("Câu SQL gửi tới SQL Server");
        lbl.setFont(Theme.FONT_DAM);
        khungSql.add(lbl, BorderLayout.NORTH);
        khungSql.add(new JScrollPane(txtSql), BorderLayout.CENTER);
        JPanel duoi = new JPanel(new BorderLayout());
        duoi.setOpaque(false);
        duoi.add(lblThamSo, BorderLayout.NORTH);
        duoi.add(lblGhiChu, BorderLayout.SOUTH);
        khungSql.add(duoi, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(bang), khungSql);
        split.setResizeWeight(0.65);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        return split;
    }

    private JPanel taoChanTrang() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(lblTrangThai, BorderLayout.WEST);
        btnChon.setEnabled(false);
        btnChon.addActionListener(e -> chonSuat());
        p.add(btnChon, BorderLayout.EAST);
        return p;
    }

    // ------------------------------------------------------------------ xu ly

    @Override
    public void khiHienThi(Object thamSo) {
        if (!daNapLanDau) {
            daNapLanDau = true;
            timKiem();      // lan dau mo: hien tat ca suat sap chieu
        }
    }

    private void napTheLoai() {
        new SwingWorker<List<TheLoai>, Void>() {
            @Override
            protected List<TheLoai> doInBackground() {
                return service.dsTheLoai();
            }

            @Override
            protected void done() {
                try {
                    DefaultComboBoxModel<Object> m = new DefaultComboBoxModel<>();
                    m.addElement(TAT_CA);
                    get().forEach(m::addElement);
                    cboTheLoai.setModel(m);
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi(ex);
                }
            }
        }.execute();
    }

    private BoLocTimKiem docBoLoc() {
        Object tl = cboTheLoai.getSelectedItem();
        LocalDate ngay = chkNgay.isSelected()
                ? ((Date) spnNgay.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : null;
        BigDecimal gia = chkGia.isSelected() ? new BigDecimal(((Number) spnGia.getValue()).longValue()) : null;
        return new BoLocTimKiem(txtTen.getText(), tl instanceof TheLoai t ? t.getMaTL() : null, ngay, gia);
    }

    private void timKiem() {
        BoLocTimKiem loc = docBoLoc();
        boolean anToan = rdoAnToan.isSelected();
        btnTim.setEnabled(false);
        lblTrangThai.setText("Đang tìm...");
        new SwingWorker<KetQuaTimKiem, Void>() {
            @Override
            protected KetQuaTimKiem doInBackground() throws NghiepVuException {
                return service.tim(loc, anToan);
            }

            @Override
            protected void done() {
                btnTim.setEnabled(true);
                try {
                    hienKetQua(get());
                } catch (ExecutionException ex) {
                    lblTrangThai.setText(" ");
                    baoLoi(ex.getCause());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void hienKetQua(KetQuaTimKiem kq) {
        ketQua = kq.dsSuat();
        ketQuaAnToan = kq.anToan();
        model.setRowCount(0);
        for (SuatChieu s : ketQua) {
            model.addRow(new Object[] {s.getTenPhim(), s.getTheLoai(), Theme.ngayGio(s.getGioBatDau()),
                    s.getTenPhong(), Theme.tien(s.getGiaCoBan())});
        }
        txtSql.setText(kq.sql());
        txtSql.setCaretPosition(0);
        lblThamSo.setText(kq.anToan()
                ? "Tham số (gửi riêng, không ghép vào SQL): " + kq.thamSo()
                : "Không có tham số: mọi giá trị đã bị ghép thẳng vào câu SQL.");
        lblGhiChu.setText(kq.ghiChu() == null ? " " : kq.ghiChu());
        lblGhiChu.setForeground(kq.ghiChu() != null && kq.ghiChu().startsWith("SQL Server") ? Theme.MAU_CHINH : Theme.MAU_CANH_BAO);
        lblTrangThai.setText("Tìm thấy " + ketQua.size() + " suất chiếu"
                + (kq.anToan() ? "" : "  ·  CHẾ ĐỘ KHÔNG AN TOÀN (demo)"));
        lblTrangThai.setForeground(kq.anToan() ? Theme.MAU_CHU_PHU : Theme.MAU_CHINH);
        capNhatNutChon();
    }

    private void capNhatNutChon() {
        // Ket qua che do noi chuoi co the la du lieu bi lo (vd bang TaiKhoan) -> khong cho dat ve tu do
        btnChon.setEnabled(ketQuaAnToan && bang.getSelectedRow() >= 0);
    }

    private void chonSuat() {
        int dong = bang.getSelectedRow();
        if (dong < 0 || !ketQuaAnToan) {
            return;
        }
        SuatChieu s = ketQua.get(bang.convertRowIndexToModel(dong));
        dieuHuong.chuyenDen(ManHinh.SO_DO_GHE, s.getMaSuat());
    }

    private void xoaLoc() {
        txtTen.setText("");
        if (cboTheLoai.getItemCount() > 0) {
            cboTheLoai.setSelectedIndex(0);
        }
        chkNgay.setSelected(false);
        spnNgay.setEnabled(false);
        chkGia.setSelected(false);
        spnGia.setEnabled(false);
        rdoAnToan.setSelected(true);
        timKiem();
    }

    private void baoLoi(Throwable ex) {
        Throwable goc = ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
        if (goc instanceof NghiepVuException) {
            JOptionPane.showMessageDialog(this, goc.getMessage(), "Kiểm tra lại bộ lọc", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm được: " + goc.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}


// ==========================================================================================
// ChonSuatChieuPanel   (truoc day: src/main/java/com/nhom/datve/ui/khachhang/ChonSuatChieuPanel.java)
// ==========================================================================================

/**
 * Man hinh chon suat chieu.
 * - Chi goi tang Service (PhimService), khong goi DAO truc tiep.
 * - Nhan tham so Integer maPhim tai khiHienThi(Object thamSo).
 * - Cho phep chon Ngay chieu, Phong chieu va Gio chieu cua phim da chon.
 * - Bam "Chon ghe" -> dieuHuong.chuyenDen(ManHinh.SO_DO_GHE, suat.getMaSuat()).
 */
class ChonSuatChieuPanel extends JPanel implements ManHinhCon {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter NGAY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String TAT_CA_NGAY = "Tất cả ngày chiếu";
    private static final String TAT_CA_PHONG = "Tất cả phòng chiếu";

    private final transient DieuHuong dieuHuong;
    private final transient PhimService phimService = new PhimService();

    private final JLabel lblTieuDePhim = Theme.tieuDe("Chọn suất chiếu");
    private final JLabel lblThongTinPhim = Theme.chuPhu(" ");
    private final JComboBox<String> cboNgay = new JComboBox<>();
    private final JComboBox<String> cboPhong = new JComboBox<>();
    private final JPanel khuNutGioChieu = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
    private final JButton btnChonGhe = Theme.nutChinh("Chọn ghế cho suất này  →");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"Mã suất", "Ngày chiếu", "Giờ bắt đầu", "Giờ kết thúc", "Phòng chiếu", "Giá cơ bản"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable bangSuat = new JTable(model);

    private Integer maPhimHienTai;
    private transient List<SuatChieu> tatCaSuat = new ArrayList<>();
    private transient List<SuatChieu> suatDangLoc = new ArrayList<>();
    private boolean dangCapNhatBoLoc;

    public ChonSuatChieuPanel(DieuHuong dieuHuong) {
        super(new BorderLayout(0, 12));
        this.dieuHuong = dieuHuong;
        setBackground(Theme.MAU_NEN);
        setBorder(Theme.le(16));

        add(taoPhanDau(), BorderLayout.NORTH);
        add(taoPhanGiua(), BorderLayout.CENTER);
        add(taoPhanCuoi(), BorderLayout.SOUTH);
    }

    private JPanel taoPhanDau() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        JPanel hangTieuDe = new JPanel(new BorderLayout(12, 0));
        hangTieuDe.setOpaque(false);
        JButton btnQuayLai = Theme.nutPhu("← Quay lại");
        btnQuayLai.addActionListener(e -> dieuHuong.quayLai());
        hangTieuDe.add(btnQuayLai, BorderLayout.WEST);

        JPanel thongTinPhim = new JPanel(new BorderLayout(0, 2));
        thongTinPhim.setOpaque(false);
        thongTinPhim.add(lblTieuDePhim, BorderLayout.NORTH);
        thongTinPhim.add(lblThongTinPhim, BorderLayout.SOUTH);
        hangTieuDe.add(thongTinPhim, BorderLayout.CENTER);
        p.add(hangTieuDe, BorderLayout.NORTH);

        JPanel boLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        boLoc.setBackground(Theme.MAU_THE);
        boLoc.setBorder(Theme.khung("Chọn ngày chiếu và phòng chiếu"));

        cboNgay.addActionListener(e -> {
            if (!dangCapNhatBoLoc) {
                locSuatChieu();
            }
        });
        cboPhong.addActionListener(e -> {
            if (!dangCapNhatBoLoc) {
                locSuatChieu();
            }
        });

        boLoc.add(new JLabel("Ngày chiếu:"));
        boLoc.add(cboNgay);
        boLoc.add(new JLabel("Phòng chiếu:"));
        boLoc.add(cboPhong);
        p.add(boLoc, BorderLayout.SOUTH);
        return p;
    }

    private JPanel taoPhanGiua() {
        JPanel giua = new JPanel(new BorderLayout(0, 10));
        giua.setOpaque(false);

        khuNutGioChieu.setBackground(Theme.MAU_THE);
        khuNutGioChieu.setBorder(Theme.khung("Chọn nhanh khung giờ chiếu"));
        giua.add(khuNutGioChieu, BorderLayout.NORTH);

        Theme.dinhDangBang(bangSuat);
        bangSuat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bangSuat.getSelectionModel().addListSelectionListener(e -> btnChonGhe.setEnabled(bangSuat.getSelectedRow() >= 0));
        bangSuat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    moSoDoGheDaChon();
                }
            }
        });
        giua.add(new JScrollPane(bangSuat), BorderLayout.CENTER);
        return giua;
    }

    private JPanel taoPhanCuoi() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);
        btnChonGhe.setEnabled(false);
        btnChonGhe.addActionListener(e -> moSoDoGheDaChon());
        p.add(btnChonGhe);
        return p;
    }

    @Override
    public void khiHienThi(Object thamSo) {
        if (thamSo instanceof Integer id) {
            maPhimHienTai = id;
        }
        if (maPhimHienTai == null) {
            lblTieuDePhim.setText("Vui lòng chọn một phim từ danh sách");
            lblThongTinPhim.setText(" ");
            tatCaSuat = List.of();
            locSuatChieu();
            return;
        }
        napDuLieu(maPhimHienTai);
    }

    private void napDuLieu(int maPhim) {
        new SwingWorker<Void, Void>() {
            private Phim phim;
            private List<SuatChieu> dsSuat;

            @Override
            protected Void doInBackground() throws NghiepVuException {
                phim = phimService.layPhimTheoMa(maPhim);
                dsSuat = phimService.laySuatSapChieu(maPhim);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (phim != null) {
                        lblTieuDePhim.setText(phim.getTenPhim());
                        String tl = phim.getTheLoais() == null || phim.getTheLoais().isEmpty()
                                ? ""
                                : phim.getTheLoais().stream().map(TheLoai::getTenTL).collect(Collectors.joining(", ")) + "  ·  ";
                        lblThongTinPhim.setText(tl + "Thời lượng: " + phim.getThoiLuong() + " phút  ·  Đạo diễn: "
                                + (phim.getDaoDien() != null ? phim.getDaoDien() : "")
                                + "  ·  Độ tuổi: " + (phim.getDoTuoi() != null ? phim.getDoTuoi() : "P"));
                    }
                    tatCaSuat = dsSuat != null ? dsSuat : List.of();
                    capNhatBoLoc();
                    locSuatChieu();
                } catch (ExecutionException ex) {
                    Throwable goc = ex.getCause() != null ? ex.getCause() : ex;
                    int loai = goc instanceof NghiepVuException ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE;
                    JOptionPane.showMessageDialog(ChonSuatChieuPanel.this,
                            goc.getMessage(), "Không tải được suất chiếu", loai);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void capNhatBoLoc() {
        dangCapNhatBoLoc = true;
        DefaultComboBoxModel<String> mNgay = new DefaultComboBoxModel<>();
        mNgay.addElement(TAT_CA_NGAY);
        Set<LocalDate> dsNgay = new LinkedHashSet<>();
        for (SuatChieu s : tatCaSuat) {
            if (s.getGioBatDau() != null) {
                dsNgay.add(s.getGioBatDau().toLocalDate());
            }
        }
        dsNgay.forEach(d -> mNgay.addElement(d.format(NGAY_FMT)));
        cboNgay.setModel(mNgay);

        DefaultComboBoxModel<String> mPhong = new DefaultComboBoxModel<>();
        mPhong.addElement(TAT_CA_PHONG);
        Set<String> dsPhong = new LinkedHashSet<>();
        for (SuatChieu s : tatCaSuat) {
            String tenPhong = s.getTenPhong() != null ? s.getTenPhong() : "Phòng #" + s.getMaPhong();
            dsPhong.add(tenPhong);
        }
        dsPhong.forEach(mPhong::addElement);
        cboPhong.setModel(mPhong);
        dangCapNhatBoLoc = false;
    }

    private void locSuatChieu() {
        String ngayChon = (String) cboNgay.getSelectedItem();
        String phongChon = (String) cboPhong.getSelectedItem();

        suatDangLoc = tatCaSuat.stream()
                .filter(s -> {
                    if (ngayChon != null && !TAT_CA_NGAY.equals(ngayChon) && s.getGioBatDau() != null) {
                        if (!s.getGioBatDau().toLocalDate().format(NGAY_FMT).equals(ngayChon)) {
                            return false;
                        }
                    }
                    if (phongChon != null && !TAT_CA_PHONG.equals(phongChon)) {
                        String tenPhong = s.getTenPhong() != null ? s.getTenPhong() : "Phòng #" + s.getMaPhong();
                        if (!tenPhong.equals(phongChon)) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();

        model.setRowCount(0);
        khuNutGioChieu.removeAll();
        if (suatDangLoc.isEmpty()) {
            khuNutGioChieu.add(Theme.chuPhu("Không có suất chiếu nào phù hợp với bộ lọc."));
        } else {
            for (SuatChieu s : suatDangLoc) {
                String tenPhong = s.getTenPhong() != null ? s.getTenPhong() : "Phòng #" + s.getMaPhong();
                model.addRow(new Object[] {
                    s.getMaSuat(),
                    s.getGioBatDau() != null ? s.getGioBatDau().toLocalDate().format(NGAY_FMT) : "",
                    Theme.gio(s.getGioBatDau()),
                    Theme.gio(s.getGioKetThuc()),
                    tenPhong,
                    Theme.tien(s.getGiaCoBan())
                });

                JButton btnGio = Theme.nutPhu(Theme.gio(s.getGioBatDau()) + " (" + tenPhong + ")");
                btnGio.addActionListener(e -> dieuHuong.chuyenDen(ManHinh.SO_DO_GHE, s.getMaSuat()));
                khuNutGioChieu.add(btnGio);
            }
        }
        khuNutGioChieu.revalidate();
        khuNutGioChieu.repaint();
        btnChonGhe.setEnabled(false);
    }

    private void moSoDoGheDaChon() {
        int dong = bangSuat.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một suất chiếu.",
                    "Chưa chọn suất chiếu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SuatChieu s = suatDangLoc.get(bangSuat.convertRowIndexToModel(dong));
        dieuHuong.chuyenDen(ManHinh.SO_DO_GHE, s.getMaSuat());
    }
}


// ==========================================================================================
// SoDoGhePanel   (truoc day: src/main/java/com/nhom/datve/ui/khachhang/SoDoGhePanel.java)
// ==========================================================================================

/**
 * Man hinh so do chon ghe.
 * - Nhan tham so Integer maSuat tai khiHienThi(Object thamSo).
 * - Dung luoi JToggleButton (GridLayout) voi mau Theme.GHE_TRONG, GHE_DANG_CHON, GHE_DA_DAT, GHE_VIP, GHE_DOI.
 * - Nut "Dat ve" goi XacNhanDatVeDialog.hienThi(...).
 */
class SoDoGhePanel extends JPanel implements ManHinhCon {
    private static final long serialVersionUID = 1L;

    private final transient DieuHuong dieuHuong;
    private final transient DatVeService service = new DatVeService();

    private final JLabel lblSuat = Theme.tieuDe("Chọn ghế");
    private final JLabel lblChiTietSuat = Theme.chuPhu(" ");
    private final JPanel luoiGhe = new JPanel();
    private final JLabel lblGheChon = Theme.chuPhu("Chưa chọn ghế nào");
    private final JLabel lblTongTien = new JLabel("0 đ");
    private final JButton btnDatVe = Theme.nutChinh("Đặt vé");

    private Integer maSuatHienTai;
    private transient SoDoGhe soDo;
    private final transient List<Ghe> gheDangChon = new ArrayList<>();

    public SoDoGhePanel(DieuHuong dieuHuong) {
        super(new BorderLayout(0, 12));
        this.dieuHuong = dieuHuong;
        setBackground(Theme.MAU_NEN);
        setBorder(Theme.le(16));

        add(taoPhanDau(), BorderLayout.NORTH);
        add(taoPhanSoDo(), BorderLayout.CENTER);
        add(taoPhanCuoi(), BorderLayout.SOUTH);
    }

    private JPanel taoPhanDau() {
        JPanel p = new JPanel(new BorderLayout(12, 8));
        p.setOpaque(false);

        JButton btnQuayLai = Theme.nutPhu("← Quay lại");
        btnQuayLai.addActionListener(e -> dieuHuong.quayLai());
        p.add(btnQuayLai, BorderLayout.WEST);

        JPanel giua = new JPanel(new BorderLayout(0, 2));
        giua.setOpaque(false);
        giua.add(lblSuat, BorderLayout.NORTH);
        giua.add(lblChiTietSuat, BorderLayout.SOUTH);
        p.add(giua, BorderLayout.CENTER);

        // Thanh MAN HINH CHIEU
        JLabel manHinh = new JLabel("——————————  MÀN HÌNH CHIẾU  ——————————", SwingConstants.CENTER);
        manHinh.setFont(Theme.FONT_DAM);
        manHinh.setOpaque(true);
        manHinh.setBackground(Theme.MAU_CHU);
        manHinh.setForeground(Color.WHITE);
        manHinh.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        p.add(manHinh, BorderLayout.SOUTH);
        return p;
    }

    private JScrollPane taoPhanSoDo() {
        luoiGhe.setBackground(Theme.MAU_THE);
        luoiGhe.setBorder(Theme.le(24));
        JPanel boc = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        boc.setBackground(Theme.MAU_THE);
        boc.add(luoiGhe);
        JScrollPane cuon = new JScrollPane(boc);
        cuon.setBorder(BorderFactory.createLineBorder(Theme.MAU_VIEN));
        return cuon;
    }

    private JPanel taoPhanCuoi() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        // Chu thich mau ghe
        JPanel chuThich = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        chuThich.setOpaque(false);
        chuThich.add(oMau(Theme.GHE_TRONG, "Ghế thường (Trống)"));
        chuThich.add(oMau(Theme.GHE_VIP, "Ghế VIP (×1.3)"));
        chuThich.add(oMau(Theme.GHE_DOI, "Ghế đôi (×2.0)"));
        chuThich.add(oMau(Theme.GHE_DANG_CHON, "Đang chọn"));
        chuThich.add(oMau(Theme.GHE_DA_DAT, "Đã đặt"));
        p.add(chuThich, BorderLayout.NORTH);

        // Thanh tong tien + nut Dat ve
        JPanel thanhDat = new JPanel(new BorderLayout(12, 0));
        thanhDat.setBackground(Theme.MAU_THE);
        thanhDat.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.MAU_VIEN),
                Theme.le(12)));

        thanhDat.add(lblGheChon, BorderLayout.CENTER);

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        phai.setOpaque(false);
        phai.add(new JLabel("Tạm tính:"));
        lblTongTien.setFont(Theme.FONT_TIEU_DE);
        lblTongTien.setForeground(Theme.MAU_CHINH);
        phai.add(lblTongTien);

        btnDatVe.setEnabled(false);
        btnDatVe.addActionListener(e -> datVe());
        phai.add(btnDatVe);
        thanhDat.add(phai, BorderLayout.EAST);

        p.add(thanhDat, BorderLayout.SOUTH);
        return p;
    }

    @Override
    public void khiHienThi(Object thamSo) {
        if (thamSo instanceof Integer id) {
            maSuatHienTai = id;
        }
        napLaiSoDo();
    }

    public void napLaiSoDo() {
        gheDangChon.clear();
        capNhatThongTinChon();
        if (maSuatHienTai == null) {
            lblSuat.setText("Vui lòng chọn suất chiếu trước");
            lblChiTietSuat.setText(" ");
            luoiGhe.removeAll();
            luoiGhe.revalidate();
            luoiGhe.repaint();
            return;
        }

        int maSuat = maSuatHienTai;
        new SwingWorker<SoDoGhe, Void>() {
            @Override
            protected SoDoGhe doInBackground() throws NghiepVuException {
                return service.laySoDoGhe(maSuat);
            }

            @Override
            protected void done() {
                try {
                    soDo = get();
                    hienThiSoDo(soDo);
                } catch (ExecutionException ex) {
                    Throwable goc = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(SoDoGhePanel.this, goc.getMessage(),
                            "Không tải được sơ đồ ghế", JOptionPane.WARNING_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void hienThiSoDo(SoDoGhe sd) {
        SuatChieu s = sd.getSuatChieu();
        PhongChieu phong = sd.getPhong();
        String tenPhim = s.getTenPhim() != null ? s.getTenPhim() : "Suất #" + s.getMaSuat();
        lblSuat.setText(tenPhim + "  ·  " + phong.getTenPhong());
        lblChiTietSuat.setText("Giờ chiếu: " + Theme.ngayGio(s.getGioBatDau())
                + "  ·  Giá cơ bản: " + Theme.tien(s.getGiaCoBan())
                + "  ·  Còn trống: " + sd.soGheTrong() + "/" + sd.getDsGhe().size() + " ghế"
                + "  (Tối đa " + DatVeService.SO_GHE_TOI_DA + " ghế/lần đặt)");

        int soHang = Math.max(1, phong.getSoHang());
        int soCot = Math.max(1, phong.getSoCot());
        luoiGhe.removeAll();
        luoiGhe.setLayout(new GridLayout(soHang, soCot, 8, 8));

        for (int h = 0; h < soHang; h++) {
            char kyTuHang = (char) ('A' + h);
            for (int c = 1; c <= soCot; c++) {
                Optional<Ghe> optGhe = sd.timGhe(kyTuHang, c);
                if (optGhe.isPresent()) {
                    luoiGhe.add(taoNutGhe(optGhe.get(), sd));
                } else {
                    // O trong (vd hang ghe doi co so ghe = soCot / 2)
                    JPanel oTrong = new JPanel();
                    oTrong.setOpaque(false);
                    oTrong.setPreferredSize(new Dimension(64, 44));
                    luoiGhe.add(oTrong);
                }
            }
        }
        luoiGhe.revalidate();
        luoiGhe.repaint();
    }

    private JToggleButton taoNutGhe(Ghe ghe, SoDoGhe sd) {
        JToggleButton btn = new JToggleButton(ghe.getTenGhe());
        btn.setFont(Theme.FONT_DAM);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(64, 44));

        BigDecimal gia = ghe.tinhGiaVe(sd.getSuatChieu().getGiaCoBan());
        String tenLoai = ghe.getLoaiGhe() != null ? ghe.getLoaiGhe().getTenLoai() : "Thường";
        btn.setToolTipText("Ghế " + ghe.getTenGhe() + " (" + tenLoai + ") – " + Theme.tien(gia));

        if (sd.isDaDat(ghe)) {
            btn.setBackground(Theme.GHE_DA_DAT);
            btn.setForeground(Color.WHITE);
            btn.putClientProperty("FlatLaf.style",
                    "disabledBackground: " + hex(Theme.GHE_DA_DAT) + "; disabledText: #FFFFFF");
            btn.setEnabled(false);
        } else {
            datMauNutGhe(btn, ghe, false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> xuLyChonGhe(btn, ghe));
        }
        return btn;
    }

    private void xuLyChonGhe(JToggleButton btn, Ghe ghe) {
        if (btn.isSelected()) {
            if (gheDangChon.size() >= DatVeService.SO_GHE_TOI_DA) {
                btn.setSelected(false);
                JOptionPane.showMessageDialog(this,
                        "Mỗi lần đặt tối đa " + DatVeService.SO_GHE_TOI_DA + " ghế.",
                        "Vượt số ghế tối đa", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!gheDangChon.contains(ghe)) {
                gheDangChon.add(ghe);
            }
            datMauNutGhe(btn, ghe, true);
        } else {
            gheDangChon.remove(ghe);
            datMauNutGhe(btn, ghe, false);
        }
        capNhatThongTinChon();
    }

    private static void datMauNutGhe(JToggleButton btn, Ghe ghe, boolean dangChon) {
        Color mauTrong = Theme.GHE_TRONG;
        if (ghe.getLoaiGhe() != null && ghe.getLoaiGhe().getTenLoai() != null) {
            String ten = ghe.getLoaiGhe().getTenLoai().toLowerCase();
            if (ten.contains("vip")) {
                mauTrong = Theme.GHE_VIP;
            } else if (ten.contains("đôi") || ten.contains("doi")) {
                mauTrong = Theme.GHE_DOI;
            }
        }
        // Dam bao hien dung mau tren ca FlatLaf lan System LookAndFeel
        btn.putClientProperty("FlatLaf.style",
                "background: " + hex(mauTrong)
                        + "; foreground: " + hex(Theme.MAU_CHU)
                        + "; selectedBackground: " + hex(Theme.GHE_DANG_CHON)
                        + "; selectedForeground: #FFFFFF");
        if (dangChon) {
            btn.setBackground(Theme.GHE_DANG_CHON);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(mauTrong);
            btn.setForeground(Theme.MAU_CHU);
        }
    }

    private static String hex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    private void capNhatThongTinChon() {
        if (gheDangChon.isEmpty() || soDo == null) {
            lblGheChon.setText("Chưa chọn ghế nào");
            lblTongTien.setText(Theme.tien(BigDecimal.ZERO));
            btnDatVe.setEnabled(false);
            return;
        }
        List<String> dsTen = gheDangChon.stream()
                .sorted(Comparator.comparing(Ghe::getHang).thenComparing(Ghe::getSoGhe))
                .map(Ghe::getTenGhe)
                .toList();
        lblGheChon.setText("Đang chọn " + dsTen.size() + " ghế: " + String.join(", ", dsTen));
        BigDecimal tong = service.tinhTongTien(soDo.getSuatChieu(), gheDangChon);
        lblTongTien.setText(Theme.tien(tong));
        btnDatVe.setEnabled(true);
    }

    /**
     * goi XacNhanDatVeDialog.hienThi(...).
     */
    private void datVe() {
        if (soDo == null || gheDangChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 ghế.",
                    "Chưa chọn ghế", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Optional<DatVe> kq = XacNhanDatVeDialog.hienThi(dieuHuong.getCuaSo(),
                dieuHuong.getPhien().getMaKH(), soDo.getSuatChieu(), gheDangChon);
        if (kq.isPresent()) {
            dieuHuong.chuyenDen(ManHinh.LICH_SU_VE);
        } else {
            napLaiSoDo();   // co the ghe vua bi nguoi khac dat
        }
    }

    private static JPanel oMau(Color mau, String nhan) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JPanel o = new JPanel();
        o.setBackground(mau);
        o.setPreferredSize(new Dimension(20, 20));
        o.setBorder(BorderFactory.createLineBorder(Theme.MAU_VIEN));
        p.add(o);
        p.add(new JLabel(nhan));
        return p;
    }
}


// ==========================================================================================
// XacNhanDatVeDialog   (truoc day: src/main/java/com/nhom/datve/ui/khachhang/XacNhanDatVeDialog.java)
// ==========================================================================================

/**
 * Hop thoai xac nhan dat ve va thanh toan gia lap (Khue).
 * Goi tu SoDoGhePanel (Trung) khi khach bam "Dat ve":
 * <pre>
 * Optional&lt;DatVe&gt; kq = XacNhanDatVeDialog.hienThi(dieuHuong.getCuaSo(),
 *         dieuHuong.getPhien().getMaKH(), soDo.getSuatChieu(), gheDangChon);
 * if (kq.isPresent()) dieuHuong.chuyenDen(ManHinh.LICH_SU_VE);
 * else napLaiSoDoGhe();   // co the ghe vua bi nguoi khac dat
 * </pre>
 */
class XacNhanDatVeDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    /** Thoi gian gia lap "dang ket noi cong thanh toan". */
    private static final long GIA_LAP_MS = 800;

    private final transient DatVeService service;
    private final int maKH;
    private final transient SuatChieu suat;
    private final transient List<Ghe> gheChon;

    private final JComboBox<PhuongThucThanhToan> cboPhuongThuc = new JComboBox<>(PhuongThucThanhToan.values());
    private final JCheckBox chkDongY = new JCheckBox("Tôi đã kiểm tra thông tin và đồng ý thanh toán");
    private final JButton btnThanhToan = Theme.nutChinh("Thanh toán");
    private final JButton btnHuy = Theme.nutPhu("Hủy");
    private final CardLayout cardNut = new CardLayout();
    private final JPanel khuNut = new JPanel(cardNut);
    private transient DatVe ketQua;

    public XacNhanDatVeDialog(Window owner, DatVeService service, int maKH, SuatChieu suat, List<Ghe> gheChon) {
        super(owner, "Xác nhận đặt vé", ModalityType.APPLICATION_MODAL);
        this.service = service;
        this.maKH = maKH;
        this.suat = suat;
        this.gheChon = gheChon.stream()
                .sorted(Comparator.comparing(Ghe::getHang).thenComparing(Ghe::getSoGhe)).toList();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel goc = new JPanel(new BorderLayout(0, 14));
        goc.setBorder(Theme.le(20));
        goc.add(taoThongTin(), BorderLayout.NORTH);
        goc.add(taoBangGhe(), BorderLayout.CENTER);
        goc.add(taoThanhToan(), BorderLayout.SOUTH);
        setContentPane(goc);
        getRootPane().setDefaultButton(btnThanhToan);
        setSize(new Dimension(560, 600));
        setMinimumSize(new Dimension(480, 520));
        setLocationRelativeTo(owner);
    }

    /** Mo hop thoai va cho den khi dong. Tra ve lan dat neu thanh toan thanh cong. */
    public static Optional<DatVe> hienThi(Component cha, int maKH, SuatChieu suat, List<Ghe> gheChon) {
        Window owner = cha instanceof Window w ? w : (cha == null ? null : javax.swing.SwingUtilities.getWindowAncestor(cha));
        if (gheChon == null || gheChon.isEmpty()) {
            JOptionPane.showMessageDialog(cha, "Vui lòng chọn ít nhất 1 ghế.", "Chưa chọn ghế",
                    JOptionPane.WARNING_MESSAGE);
            return Optional.empty();
        }
        XacNhanDatVeDialog d = new XacNhanDatVeDialog(owner, new DatVeService(), maKH, suat, gheChon);
        d.setVisible(true);
        return Optional.ofNullable(d.ketQua);
    }

    public Optional<DatVe> getKetQua() { return Optional.ofNullable(ketQua); }

    // ------------------------------------------------------------------ giao dien

    private JPanel taoThongTin() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 0, 3, 14);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        String tenPhim = suat.getTenPhim() != null ? suat.getTenPhim() : "Suất chiếu #" + suat.getMaSuat();
        p.add(Theme.tieuDe(tenPhim), c);
        c.gridwidth = 1;
        dong(p, c, "Suất chiếu", Theme.ngayGio(suat.getGioBatDau()));
        dong(p, c, "Phòng", suat.getTenPhong() != null ? suat.getTenPhong() : "Phòng #" + suat.getMaPhong());
        dong(p, c, "Số ghế", gheChon.size() + " ghế: "
                + String.join(", ", gheChon.stream().map(Ghe::getTenGhe).toList()));
        JPanel canhTrai = new JPanel(new BorderLayout());
        canhTrai.add(p, BorderLayout.WEST);
        return canhTrai;
    }

    private static void dong(JPanel p, GridBagConstraints c, String nhan, String giaTri) {
        c.gridy++;
        c.gridx = 0;
        p.add(Theme.chuPhu(nhan), c);
        c.gridx = 1;
        JLabel v = new JLabel(giaTri);
        v.setFont(Theme.FONT_DAM);
        p.add(v, c);
    }

    private JScrollPane taoBangGhe() {
        DefaultTableModel m = new DefaultTableModel(new Object[] {"Ghế", "Loại ghế", "Giá vé"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Ghe g : gheChon) {
            m.addRow(new Object[] {g.getTenGhe(), g.getLoaiGhe() == null ? "" : g.getLoaiGhe().getTenLoai(),
                    Theme.tien(g.tinhGiaVe(suat.getGiaCoBan()))});
        }
        JTable t = new JTable(m);
        Theme.dinhDangBang(t);
        t.setAutoCreateRowSorter(false);
        t.setRowSelectionAllowed(false);
        DefaultTableCellRenderer phai = new DefaultTableCellRenderer();
        phai.setHorizontalAlignment(SwingConstants.RIGHT);
        t.getColumnModel().getColumn(2).setCellRenderer(phai);
        return new JScrollPane(t);
    }

    private JPanel taoThanhToan() {
        BigDecimal tong = service.tinhTongTien(suat, gheChon);
        JPanel p = new JPanel(new BorderLayout(0, 10));

        JPanel dongTong = new JPanel(new BorderLayout());
        dongTong.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.MAU_VIEN));
        JLabel lblTong = new JLabel("Tổng tiền");
        lblTong.setFont(Theme.FONT_TIEU_DE_NHO);
        JLabel soTien = new JLabel(Theme.tien(tong));
        soTien.setFont(Theme.FONT_TIEU_DE);
        soTien.setForeground(Theme.MAU_CHINH);
        dongTong.add(lblTong, BorderLayout.WEST);
        dongTong.add(soTien, BorderLayout.EAST);

        JPanel chon = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chon.add(new JLabel("Phương thức thanh toán:  "));
        chon.add(cboPhuongThuc);
        JPanel giua = new JPanel(new BorderLayout(0, 6));
        giua.add(chon, BorderLayout.NORTH);
        giua.add(chkDongY, BorderLayout.CENTER);
        giua.add(Theme.chuPhu("Thanh toán giả lập: không trừ tiền thật."), BorderLayout.SOUTH);

        btnThanhToan.setEnabled(false);
        chkDongY.addActionListener(e -> btnThanhToan.setEnabled(chkDongY.isSelected()));
        btnThanhToan.addActionListener(e -> thanhToan());
        btnHuy.addActionListener(e -> dispose());
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.add(btnHuy);
        nut.add(btnThanhToan);
        JProgressBar dangXuLy = new JProgressBar();
        dangXuLy.setIndeterminate(true);
        dangXuLy.setStringPainted(true);
        dangXuLy.setString("Đang xử lý thanh toán...");
        khuNut.add(nut, "nut");
        khuNut.add(dangXuLy, "cho");

        p.add(dongTong, BorderLayout.NORTH);
        p.add(giua, BorderLayout.CENTER);
        p.add(khuNut, BorderLayout.SOUTH);
        return p;
    }

    // ------------------------------------------------------------------ xu ly

    private void thanhToan() {
        PhuongThucThanhToan pt = (PhuongThucThanhToan) cboPhuongThuc.getSelectedItem();
        cardNut.show(khuNut, "cho");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);      // khong cho dong giua chung
        new SwingWorker<DatVe, Void>() {
            @Override
            protected DatVe doInBackground() throws Exception {
                Thread.sleep(GIA_LAP_MS);                    // gia lap cho cong thanh toan
                return service.datVe(maKH, suat.getMaSuat(), gheChon, pt);
            }

            @Override
            protected void done() {
                setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                try {
                    ketQua = get();
                    JOptionPane.showMessageDialog(XacNhanDatVeDialog.this,
                            "Đặt vé thành công!\nMã đặt vé: #" + ketQua.getMaDatVe()
                                    + "\nĐã thanh toán " + Theme.tien(ketQua.getTongTien()) + " bằng " + pt.getMoTa() + ".",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (ExecutionException ex) {
                    cardNut.show(khuNut, "nut");
                    Throwable goc = ex.getCause();
                    if (goc instanceof NghiepVuException) {
                        // vd ghe vua bi nguoi khac dat: dong hop thoai de khach chon lai tren so do ghe
                        JOptionPane.showMessageDialog(XacNhanDatVeDialog.this, goc.getMessage(),
                                "Không đặt được vé", JOptionPane.WARNING_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(XacNhanDatVeDialog.this,
                                "Có lỗi khi kết nối CSDL, vui lòng thử lại.\n(" + goc.getMessage() + ")",
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
// LichSuVePanel   (truoc day: src/main/java/com/nhom/datve/ui/khachhang/LichSuVePanel.java)
// ==========================================================================================

/**
 * Man hinh lich su ve va huy ve.
 * - JTable hien danh sach cac lan dat ve cua khach hang dang dang nhap.
 * - Nut "Huy ve" goi DatVeService.huyVe(maKH, maDatVe) trong SwingWorker va bao loi bang JOptionPane.
 */
class LichSuVePanel extends JPanel implements ManHinhCon {
    private static final long serialVersionUID = 1L;

    private final transient DieuHuong dieuHuong;
    private final transient DatVeService service = new DatVeService();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"Mã đặt vé", "Phim", "Phòng", "Suất chiếu", "Ghế đã đặt", "Tổng tiền", "Ngày đặt", "Trạng thái"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable bang = new JTable(model);
    private final JLabel lblTrangThai = Theme.chuPhu(" ");
    private final JButton btnHuyVe = Theme.nutChinh("Hủy vé đã chọn");

    private transient List<DatVe> danhSach = new ArrayList<>();

    public LichSuVePanel(DieuHuong dieuHuong) {
        super(new BorderLayout(0, 12));
        this.dieuHuong = dieuHuong;
        setBackground(Theme.MAU_NEN);
        setBorder(Theme.le(16));

        add(taoPhanDau(), BorderLayout.NORTH);
        add(taoBangVe(), BorderLayout.CENTER);
        add(taoPhanCuoi(), BorderLayout.SOUTH);
    }

    private JPanel taoPhanDau() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JPanel trai = new JPanel(new BorderLayout(0, 2));
        trai.setOpaque(false);
        trai.add(Theme.tieuDe("Vé của tôi"), BorderLayout.NORTH);
        trai.add(Theme.chuPhu("Chỉ được hủy vé ở trạng thái \"Đã thanh toán\" và trước giờ chiếu ít nhất 2 tiếng."),
                BorderLayout.SOUTH);
        p.add(trai, BorderLayout.WEST);

        JButton btnLamMoi = Theme.nutPhu("Làm mới");
        btnLamMoi.addActionListener(e -> napLichSu());
        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);
        phai.add(btnLamMoi);
        p.add(phai, BorderLayout.EAST);
        return p;
    }

    private JScrollPane taoBangVe() {
        Theme.dinhDangBang(bang);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getColumnModel().getColumn(0).setPreferredWidth(80);
        bang.getColumnModel().getColumn(1).setPreferredWidth(200);
        bang.getColumnModel().getColumn(3).setPreferredWidth(135);
        bang.getColumnModel().getColumn(4).setPreferredWidth(140);
        bang.getColumnModel().getColumn(7).setCellRenderer(new TrangThaiCellRenderer());
        bang.getSelectionModel().addListSelectionListener(e -> capNhatNutHuy());
        return new JScrollPane(bang);
    }

    private JPanel taoPhanCuoi() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(lblTrangThai, BorderLayout.WEST);

        JButton btnDatThem = Theme.nutPhu("Đặt vé mới");
        btnDatThem.addActionListener(e -> dieuHuong.chuyenDen(ManHinh.DANH_SACH_PHIM));
        btnHuyVe.setEnabled(false);
        btnHuyVe.addActionListener(e -> xuLyHuyVe());

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.setOpaque(false);
        nut.add(btnDatThem);
        nut.add(btnHuyVe);
        p.add(nut, BorderLayout.EAST);
        return p;
    }

    @Override
    public void khiHienThi(Object thamSo) {
        napLichSu();
    }

    private void napLichSu() {
        if (dieuHuong.getPhien() == null || dieuHuong.getPhien().isAdmin()) {
            return;
        }
        int maKH = dieuHuong.getPhien().getMaKH();
        lblTrangThai.setText("Đang tải lịch sử đặt vé...");
        new SwingWorker<List<DatVe>, Void>() {
            @Override
            protected List<DatVe> doInBackground() {
                return service.lichSuDatVe(maKH);
            }

            @Override
            protected void done() {
                try {
                    danhSach = get();
                    doDuLieuVaoBang();
                    lblTrangThai.setText("Tổng cộng: " + danhSach.size() + " lần đặt vé.");
                } catch (ExecutionException ex) {
                    Throwable goc = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(LichSuVePanel.this,
                            "Không tải được lịch sử vé: " + goc.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void doDuLieuVaoBang() {
        model.setRowCount(0);
        for (DatVe dv : danhSach) {
            List<Ve> ves = dv.getVes();
            String tenPhim = ves.stream().map(Ve::getTenPhim).filter(Objects::nonNull).findFirst().orElse("—");
            String tenPhong = ves.stream().map(Ve::getTenPhong).filter(Objects::nonNull).findFirst().orElse("—");
            LocalDateTime gioChieu = ves.stream().map(Ve::getGioBatDau).filter(Objects::nonNull).findFirst().orElse(null);
            String dsGhe = ves.stream().map(Ve::getTenGhe).filter(Objects::nonNull).collect(Collectors.joining(", "));
            String trangThai = dv.getTrangThai() != null ? dv.getTrangThai().getMoTa() : "";

            model.addRow(new Object[] {
                "#" + dv.getMaDatVe(),
                tenPhim,
                tenPhong,
                Theme.ngayGio(gioChieu),
                dsGhe,
                Theme.tien(dv.getTongTien()),
                Theme.ngayGio(dv.getNgayDat()),
                trangThai
            });
        }
        capNhatNutHuy();
    }

    private void capNhatNutHuy() {
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            btnHuyVe.setEnabled(false);
            return;
        }
        DatVe dv = danhSach.get(bang.convertRowIndexToModel(dong));
        btnHuyVe.setEnabled(dv.getTrangThai() == TrangThaiDatVe.DA_THANH_TOAN);
    }

    private void xuLyHuyVe() {
        int dong = bang.getSelectedRow();
        if (dong < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một vé trong bảng để hủy.",
                    "Chưa chọn vé", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DatVe dv = danhSach.get(bang.convertRowIndexToModel(dong));
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn hủy mã đặt vé #" + dv.getMaDatVe() + " không?",
                "Xác nhận hủy vé", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }

        int maKH = dieuHuong.getPhien().getMaKH();
        btnHuyVe.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws NghiepVuException {
                service.huyVe(maKH, dv.getMaDatVe());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(LichSuVePanel.this,
                            "Đã hủy mã đặt vé #" + dv.getMaDatVe() + " thành công. Ghế đã được giải phóng.",
                            "Hủy vé thành công", JOptionPane.INFORMATION_MESSAGE);
                    napLichSu();
                } catch (ExecutionException ex) {
                    capNhatNutHuy();
                    Throwable goc = ex.getCause();
                    if (goc instanceof NghiepVuException) {
                        JOptionPane.showMessageDialog(LichSuVePanel.this, goc.getMessage(),
                                "Không thể hủy vé", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(LichSuVePanel.this,
                                "Lỗi khi hủy vé: " + goc.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private static class TrangThaiCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(Theme.FONT_DAM);
            if (!isSelected && value != null) {
                String s = value.toString();
                if (s.equals(TrangThaiDatVe.DA_THANH_TOAN.getMoTa())) {
                    setForeground(Theme.MAU_THANH_CONG);
                } else if (s.equals(TrangThaiDatVe.DA_HUY.getMoTa())) {
                    setForeground(Theme.MAU_CHINH);
                } else {
                    setForeground(Theme.MAU_CANH_BAO);
                }
            }
            return this;
        }
    }
}
