package com.nhom.datve.ui.khachhang;

import com.nhom.datve.dao.TimKiemSql;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.BoLocTimKiem;
import com.nhom.datve.model.KetQuaTimKiem;
import com.nhom.datve.model.SuatChieu;
import com.nhom.datve.model.TheLoai;
import com.nhom.datve.service.TimKiemService;
import com.nhom.datve.ui.DieuHuong;
import com.nhom.datve.ui.ManHinh;
import com.nhom.datve.ui.ManHinhCon;
import com.nhom.datve.ui.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Man hinh tim kiem phim (Khue).
 *  - Loc suat chieu sap toi theo ten phim, the loai, ngay chieu, gia toi da; ket qua tren JTable.
 *  - Khung "Demo chuong 10" doi giua PreparedStatement va noi chuoi de minh hoa SQL injection.
 *  - Nhap doi vao 1 dong (hoac nut "Chon suat nay") -> sang SO_DO_GHE voi maSuat.
 * Giao dien khong chua SQL: SQL tao o TimKiemSql (tang dao), man hinh chi hien cau lenh tra ve.
 */
public class TimKiemPhimPanel extends JPanel implements ManHinhCon {
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
