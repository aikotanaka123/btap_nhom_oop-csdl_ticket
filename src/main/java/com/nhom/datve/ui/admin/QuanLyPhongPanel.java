package com.nhom.datve.ui.admin;

import com.nhom.datve.dao.DAOFactory;
import com.nhom.datve.dao.GheDAO;
import com.nhom.datve.dao.LoaiGheDAO;
import com.nhom.datve.dao.PhongChieuDAO;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.Ghe;
import com.nhom.datve.model.LoaiGhe;
import com.nhom.datve.model.PhongChieu;
import com.nhom.datve.ui.DieuHuong;
import com.nhom.datve.ui.ManHinhCon;
import com.nhom.datve.ui.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình quản lý phòng chiếu và sơ đồ ghế cho Quản trị viên (Khánh).
 */
public class QuanLyPhongPanel extends JPanel implements ManHinhCon {
    private static final long serialVersionUID = 1L;

    private final transient DieuHuong dieuHuong;
    private final transient PhongChieuDAO phongDAO = DAOFactory.phongChieu();
    private final transient GheDAO gheDAO = DAOFactory.ghe();
    private final transient LoaiGheDAO loaiGheDAO = DAOFactory.loaiGhe();

    private final DefaultTableModel modelPhong = new DefaultTableModel(
            new Object[] { "Mã", "Tên phòng", "Số hàng", "Số cột", "Trạng thái" }, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable bangPhong = new JTable(modelPhong);

    private final JTextField txtTenPhong = new JTextField(16);
    private final JSpinner spnSoHang = new JSpinner(new SpinnerNumberModel(6, 1, 20, 1));
    private final JSpinner spnSoCot = new JSpinner(new SpinnerNumberModel(10, 2, 24, 2));
    private final JCheckBox chkHoatDong = new JCheckBox("Đang hoạt động", true);

    private final JButton btnThemPhong = Theme.nutChinh("Thêm phòng");
    private final JButton btnSuaPhong = Theme.nutChinh("Lưu phòng");
    private final JButton btnXoaPhong = Theme.nutPhu("Xóa phòng");
    private final JButton btnNhapMoi = Theme.nutPhu("Nhập mới");

    // Khu vực sơ đồ ghế bên phải
    private final JLabel lblTieuDeSoDo = Theme.chuPhu("Chọn một phòng chiếu ở bảng bên trái để xem sơ đồ ghế.");
    private final JPanel pnlLuoiGhe = new JPanel();
    private final JLabel lblGheDangChon = new JLabel("Chưa chọn ghế");
    private final JComboBox<LoaiGhe> cboLoaiGhe = new JComboBox<>();
    private final JButton btnCapNhatGhe = Theme.nutChinh("Đổi loại ghế");
    private final JButton btnSinhGhe = Theme.nutPhu("Sinh sơ đồ ghế tự động");

    private transient List<PhongChieu> dsPhong = new ArrayList<>();
    private transient List<LoaiGhe> dsLoaiGhe = new ArrayList<>();
    private transient PhongChieu phongDangChon;
    private transient Ghe gheDangChon;
    private boolean daNapLanDau;

    public QuanLyPhongPanel(DieuHuong dieuHuong) {
        super(new BorderLayout(0, 12));
        this.dieuHuong = dieuHuong;
        setBackground(Theme.MAU_NEN);
        setBorder(Theme.le(16));

        add(Theme.tieuDe("Quản lý phòng chiếu và sơ đồ ghế"), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoKhuPhong(), taoKhuSoDoGhe());
        split.setResizeWeight(0.42);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);
    }

    private JPanel taoKhuPhong() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Theme.MAU_THE);
        p.setBorder(Theme.khung("Danh sách phòng chiếu"));

        Theme.dinhDangBang(bangPhong);
        bangPhong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bangPhong.getColumnModel().getColumn(0).setPreferredWidth(45);
        bangPhong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                chonPhongTrenBang();
            }
        });

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.MAU_VIEN));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;
        c.gridx = 0;
        form.add(new JLabel("Tên phòng (*)"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        form.add(txtTenPhong, c);

        c.gridy = 1;
        c.gridwidth = 1;
        c.gridx = 0;
        form.add(new JLabel("Số hàng"), c);
        c.gridx = 1;
        form.add(spnSoHang, c);
        c.gridx = 2;
        form.add(new JLabel("Số cột"), c);
        c.gridx = 3;
        form.add(spnSoCot, c);

        c.gridy = 2;
        c.gridx = 1;
        c.gridwidth = 3;
        chkHoatDong.setOpaque(false);
        form.add(chkHoatDong, c);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        nut.setOpaque(false);
        btnNhapMoi.addActionListener(e -> lamMoiFormPhong());
        btnThemPhong.addActionListener(e -> luuPhong(true));
        btnSuaPhong.addActionListener(e -> luuPhong(false));
        btnXoaPhong.addActionListener(e -> xoaPhong());
        nut.add(btnNhapMoi);
        nut.add(btnXoaPhong);
        nut.add(btnSuaPhong);
        nut.add(btnThemPhong);

        c.gridy = 3;
        c.gridx = 0;
        c.gridwidth = 4;
        form.add(nut, c);

        p.add(new JScrollPane(bangPhong), BorderLayout.CENTER);
        p.add(form, BorderLayout.SOUTH);
        capNhatTrangThaiNutPhong();
        return p;
    }

    private JPanel taoKhuSoDoGhe() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Theme.MAU_THE);
        p.setBorder(Theme.khung("Sơ đồ ghế trong phòng"));

        JPanel tren = new JPanel(new BorderLayout(0, 6));
        tren.setOpaque(false);
        tren.add(lblTieuDeSoDo, BorderLayout.NORTH);

        JLabel manHinh = new JLabel("MÀN HÌNH CHIẾU", SwingConstants.CENTER);
        manHinh.setOpaque(true);
        manHinh.setBackground(new Color(0xE2E8F0));
        manHinh.setForeground(Theme.MAU_CHU);
        manHinh.setFont(Theme.FONT_DAM);
        manHinh.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        tren.add(manHinh, BorderLayout.CENTER);

        pnlLuoiGhe.setOpaque(false);
        pnlLuoiGhe.setLayout(new BoxLayout(pnlLuoiGhe, BoxLayout.Y_AXIS));
        JPanel bocLuoi = new JPanel(new GridBagLayout());
        bocLuoi.setOpaque(false);
        bocLuoi.add(pnlLuoiGhe);

        JPanel duoi = new JPanel(new BorderLayout(0, 8));
        duoi.setOpaque(false);
        duoi.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.MAU_VIEN));

        JPanel chuThich = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        chuThich.setOpaque(false);
        chuThich.add(taoMauChuThich(Theme.GHE_TRONG, "Ghế Thường (x1.0)"));
        chuThich.add(taoMauChuThich(Theme.GHE_VIP, "Ghế VIP (x1.3)"));
        chuThich.add(taoMauChuThich(Theme.GHE_DOI, "Ghế Đôi (x2.0)"));

        JPanel thaoTacGhe = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        thaoTacGhe.setOpaque(false);
        lblGheDangChon.setFont(Theme.FONT_DAM);
        btnCapNhatGhe.setEnabled(false);
        btnSinhGhe.setEnabled(false);
        btnCapNhatGhe.addActionListener(e -> doiLoaiGheDangChon());
        btnSinhGhe.addActionListener(e -> sinhSoDoGheTuDong());

        thaoTacGhe.add(lblGheDangChon);
        thaoTacGhe.add(cboLoaiGhe);
        thaoTacGhe.add(btnCapNhatGhe);
        thaoTacGhe.add(btnSinhGhe);

        duoi.add(chuThich, BorderLayout.NORTH);
        duoi.add(thaoTacGhe, BorderLayout.SOUTH);

        p.add(tren, BorderLayout.NORTH);
        p.add(new JScrollPane(bocLuoi), BorderLayout.CENTER);
        p.add(duoi, BorderLayout.SOUTH);
        return p;
    }

    private static JPanel taoMauChuThich(Color mau, String nhan) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        item.setOpaque(false);
        JLabel o = new JLabel("   ");
        o.setOpaque(true);
        o.setBackground(mau);
        o.setBorder(BorderFactory.createLineBorder(Theme.MAU_VIEN));
        item.add(o);
        item.add(new JLabel(nhan));
        return item;
    }

    @Override
    public void khiHienThi(Object thamSo) {
        if (!daNapLanDau) {
            daNapLanDau = true;
            napLoaiGheVaPhong();
        } else {
            taiDanhSachPhong();
        }
    }

    private void napLoaiGheVaPhong() {
        new SwingWorker<List<LoaiGhe>, Void>() {
            @Override
            protected List<LoaiGhe> doInBackground() {
                return loaiGheDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    dsLoaiGhe = get();
                    DefaultComboBoxModel<LoaiGhe> m = new DefaultComboBoxModel<>();
                    dsLoaiGhe.forEach(m::addElement);
                    cboLoaiGhe.setModel(m);
                    taiDanhSachPhong();
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi("Không tải được danh sách loại ghế", ex);
                }
            }
        }.execute();
    }

    private void taiDanhSachPhong() {
        new SwingWorker<List<PhongChieu>, Void>() {
            @Override
            protected List<PhongChieu> doInBackground() {
                return phongDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    dsPhong = get();
                    modelPhong.setRowCount(0);
                    for (PhongChieu pc : dsPhong) {
                        modelPhong.addRow(new Object[] {
                                pc.getMaPhong(),
                                pc.getTenPhong(),
                                pc.getSoHang(),
                                pc.getSoCot(),
                                pc.isHoatDong() ? "Hoạt động" : "Ngừng hoạt động"
                        });
                    }
                    if (phongDangChon != null) {
                        for (int i = 0; i < dsPhong.size(); i++) {
                            if (dsPhong.get(i).getMaPhong() == phongDangChon.getMaPhong()) {
                                int viewRow = bangPhong.convertRowIndexToView(i);
                                bangPhong.setRowSelectionInterval(viewRow, viewRow);
                                break;
                            }
                        }
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi("Không tải được danh sách phòng chiếu", ex);
                }
            }
        }.execute();
    }

    private void chonPhongTrenBang() {
        int row = bangPhong.getSelectedRow();
        if (row < 0) {
            return;
        }
        phongDangChon = dsPhong.get(bangPhong.convertRowIndexToModel(row));
        txtTenPhong.setText(phongDangChon.getTenPhong());
        spnSoHang.setValue(phongDangChon.getSoHang());
        spnSoCot.setValue(phongDangChon.getSoCot());
        chkHoatDong.setSelected(phongDangChon.isHoatDong());
        capNhatTrangThaiNutPhong();
        taiSoDoGhe(phongDangChon);
    }

    private void taiSoDoGhe(PhongChieu pc) {
        gheDangChon = null;
        lblGheDangChon.setText("Chưa chọn ghế");
        btnCapNhatGhe.setEnabled(false);

        new SwingWorker<List<Ghe>, Void>() {
            @Override
            protected List<Ghe> doInBackground() {
                return gheDAO.findByPhong(pc.getMaPhong());
            }

            @Override
            protected void done() {
                try {
                    List<Ghe> dsGhe = get();
                    veSoDoGhe(pc, dsGhe);
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi("Không tải được sơ đồ ghế", ex);
                }
            }
        }.execute();
    }

    private void veSoDoGhe(PhongChieu pc, List<Ghe> dsGhe) {
        pnlLuoiGhe.removeAll();
        btnSinhGhe.setEnabled(dsGhe.isEmpty());
        lblTieuDeSoDo.setText(pc.getTenPhong() + " · Sức chứa hiện tại: " + dsGhe.size() + " ghế ("
                + pc.getSoHang() + " hàng × " + pc.getSoCot() + " cột)");

        if (dsGhe.isEmpty()) {
            JLabel trong = Theme.chuPhu("Phòng này chưa có ghế. Bấm \"Sinh sơ đồ ghế tự động\" để tạo.");
            pnlLuoiGhe.add(trong);
        } else {
            Map<Character, List<Ghe>> theoHang = dsGhe.stream()
                    .sorted(Comparator.comparing(Ghe::getHang).thenComparing(Ghe::getSoGhe))
                    .collect(Collectors.groupingBy(Ghe::getHang, LinkedHashMap::new, Collectors.toList()));

            for (Map.Entry<Character, List<Ghe>> entry : theoHang.entrySet()) {
                JPanel hangPanel = new JPanel(new GridLayout(1, 0, 6, 0));
                hangPanel.setOpaque(false);
                hangPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));

                for (Ghe g : entry.getValue()) {
                    JButton btnGhe = new JButton(g.getTenGhe());
                    btnGhe.setFont(Theme.FONT_DAM);
                    btnGhe.setFocusPainted(false);
                    boolean laGheDoi = g.getLoaiGhe() != null && g.getLoaiGhe().getMaLoaiGhe() == 3;
                    btnGhe.setPreferredSize(new Dimension(laGheDoi ? 116 : 55, 36));
                    btnGhe.setBackground(mauTheoLoaiGhe(g));
                    btnGhe.setForeground(Theme.MAU_CHU);
                    btnGhe.addActionListener(e -> {
                        gheDangChon = g;
                        lblGheDangChon.setText("Ghế " + g.getTenGhe() + " (" + g.getLoaiGhe().getTenLoai() + ")");
                        for (int i = 0; i < cboLoaiGhe.getItemCount(); i++) {
                            if (cboLoaiGhe.getItemAt(i).getMaLoaiGhe() == g.getLoaiGhe().getMaLoaiGhe()) {
                                cboLoaiGhe.setSelectedIndex(i);
                                break;
                            }
                        }
                        btnCapNhatGhe.setEnabled(true);
                    });
                    hangPanel.add(btnGhe);
                }
                pnlLuoiGhe.add(hangPanel);
            }
        }
        pnlLuoiGhe.revalidate();
        pnlLuoiGhe.repaint();
    }

    private static Color mauTheoLoaiGhe(Ghe g) {
        if (g.getLoaiGhe() == null) {
            return Theme.GHE_TRONG;
        }
        return switch (g.getLoaiGhe().getMaLoaiGhe()) {
            case 2 -> Theme.GHE_VIP;
            case 3 -> Theme.GHE_DOI;
            default -> Theme.GHE_TRONG;
        };
    }

    private void doiLoaiGheDangChon() {
        if (gheDangChon == null || phongDangChon == null) {
            return;
        }
        LoaiGhe loaiMoi = (LoaiGhe) cboLoaiGhe.getSelectedItem();
        if (loaiMoi == null) {
            return;
        }
        gheDangChon.setLoaiGhe(loaiMoi);
        Ghe gheCanLuu = gheDangChon;
        PhongChieu phongHienTai = phongDangChon;

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return gheDAO.update(gheCanLuu);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        taiSoDoGhe(phongHienTai);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi("Không cập nhật được loại ghế", ex);
                }
            }
        }.execute();
    }

    private void sinhSoDoGheTuDong() {
        if (phongDangChon == null || dsLoaiGhe.isEmpty()) {
            return;
        }
        PhongChieu pc = phongDangChon;
        LoaiGhe thuong = timLoaiGhe(1);
        LoaiGhe vip = timLoaiGhe(2);
        LoaiGhe doi = timLoaiGhe(3);

        List<Ghe> dsMoi = new ArrayList<>();
        int soHang = pc.getSoHang();
        int soCot = pc.getSoCot();
        for (int h = 0; h < soHang; h++) {
            LoaiGhe loai = (h == soHang - 1 && soHang >= 3) ? doi : (h >= soHang - 3 && soHang >= 3 ? vip : thuong);
            int soGheHang = loai.getMaLoaiGhe() == 3 ? Math.max(1, soCot / 2) : soCot;
            for (int c = 1; c <= soGheHang; c++) {
                dsMoi.add(new Ghe(0, pc.getMaPhong(), loai, (char) ('A' + h), c));
            }
        }

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                return gheDAO.insertBatch(dsMoi);
            }

            @Override
            protected void done() {
                try {
                    int soLuong = get();
                    JOptionPane.showMessageDialog(dieuHuong.getCuaSo(),
                            "Đã sinh tự động " + soLuong + " ghế cho " + pc.getTenPhong() + ".",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    taiSoDoGhe(pc);
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi("Sinh danh sách ghế thất bại", ex);
                }
            }
        }.execute();
    }

    private LoaiGhe timLoaiGhe(int ma) {
        return dsLoaiGhe.stream()
                .filter(l -> l.getMaLoaiGhe() == ma)
                .findFirst()
                .orElse(dsLoaiGhe.get(0));
    }

    private PhongChieu docFormPhong(boolean laThemMoi) throws NghiepVuException {
        String ten = txtTenPhong.getText().trim();
        if (ten.isEmpty()) {
            throw new NghiepVuException("Vui lòng nhập tên phòng chiếu.");
        }
        PhongChieu pc = new PhongChieu();
        if (!laThemMoi) {
            if (phongDangChon == null) {
                throw new NghiepVuException("Vui lòng chọn phòng cần cập nhật.");
            }
            pc.setMaPhong(phongDangChon.getMaPhong());
        }
        pc.setTenPhong(ten);
        pc.setSoHang(((Number) spnSoHang.getValue()).intValue());
        pc.setSoCot(((Number) spnSoCot.getValue()).intValue());
        pc.setHoatDong(chkHoatDong.isSelected());
        return pc;
    }

    private void luuPhong(boolean laThemMoi) {
        final PhongChieu pc;
        try {
            pc = docFormPhong(laThemMoi);
        } catch (NghiepVuException ex) {
            JOptionPane.showMessageDialog(dieuHuong.getCuaSo(), ex.getMessage(),
                    "Dữ liệu chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                if (laThemMoi) {
                    phongDAO.insert(pc);
                } else {
                    phongDAO.update(pc);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(dieuHuong.getCuaSo(),
                            laThemMoi ? "Thêm phòng chiếu thành công!" : "Cập nhật phòng chiếu thành công!",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    taiDanhSachPhong();
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi("Không lưu được phòng chiếu", ex);
                }
            }
        }.execute();
    }

    private void xoaPhong() {
        if (phongDangChon == null) {
            return;
        }
        int xacNhan = JOptionPane.showConfirmDialog(dieuHuong.getCuaSo(),
                "Xóa phòng \"" + phongDangChon.getTenPhong() + "\"?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }
        int idXoa = phongDangChon.getMaPhong();
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return phongDAO.delete(idXoa);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        lamMoiFormPhong();
                        taiDanhSachPhong();
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi("Không thể xóa phòng chiếu (phòng đang có ghế hoặc suất chiếu)", ex);
                }
            }
        }.execute();
    }

    private void lamMoiFormPhong() {
        phongDangChon = null;
        gheDangChon = null;
        bangPhong.clearSelection();
        txtTenPhong.setText("");
        spnSoHang.setValue(6);
        spnSoCot.setValue(10);
        chkHoatDong.setSelected(true);
        pnlLuoiGhe.removeAll();
        pnlLuoiGhe.revalidate();
        pnlLuoiGhe.repaint();
        lblTieuDeSoDo.setText("Chọn một phòng chiếu ở bảng bên trái để xem sơ đồ ghế.");
        lblGheDangChon.setText("Chưa chọn ghế");
        btnCapNhatGhe.setEnabled(false);
        btnSinhGhe.setEnabled(false);
        capNhatTrangThaiNutPhong();
    }

    private void capNhatTrangThaiNutPhong() {
        boolean coChon = phongDangChon != null;
        btnSuaPhong.setEnabled(coChon);
        btnXoaPhong.setEnabled(coChon);
    }

    private void baoLoi(String tieuDe, Throwable ex) {
        Throwable goc = ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
        JOptionPane.showMessageDialog(dieuHuong.getCuaSo(), tieuDe + ":\n" + goc.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}