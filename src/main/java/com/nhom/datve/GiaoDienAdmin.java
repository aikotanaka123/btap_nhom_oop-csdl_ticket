package com.nhom.datve;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/*
 * GiaoDienAdmin.java: Cac man hinh cua NHAN VIEN (admin): quan ly phim, phong/ghe, suat chieu, thong ke.
 * Gom cac lop: QuanLyPhimPanel, QuanLyPhongPanel, QuanLySuatChieuPanel, ThongKePanel
 */

// ==========================================================================================
// QuanLyPhimPanel   (truoc day: src/main/java/com/nhom/datve/ui/admin/QuanLyPhimPanel.java)
// ==========================================================================================

/**
 * Màn hình quản lý phim cho Quản trị viên (Khánh).
 */
class QuanLyPhimPanel extends JPanel implements ManHinhCon {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final transient DieuHuong dieuHuong;
    private final transient PhimDAO phimDAO = DAOFactory.phim();
    private final transient TheLoaiDAO theLoaiDAO = DAOFactory.theLoai();

    private final JTextField txtTimKiem = new JTextField(18);
    private final DefaultTableModel modelBang = new DefaultTableModel(
            new Object[] {"Mã", "Tên phim", "Thời lượng", "Đạo diễn", "Khởi chiếu", "Độ tuổi", "Thể loại", "Trạng thái"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable bangPhim = new JTable(modelBang);

    // Form nhập liệu bên phải
    private final JTextField txtTenPhim = new JTextField(20);
    private final JSpinner spnThoiLuong = new JSpinner(new SpinnerNumberModel(110, 1, 500, 5));
    private final JTextField txtDaoDien = new JTextField(20);
    private final JSpinner spnNgayKhoiChieu = new JSpinner(new SpinnerDateModel());
    private final JComboBox<String> cboDoTuoi = new JComboBox<>(new String[] {"P", "T13", "T16", "T18"});
    private final JComboBox<TrangThaiItem> cboTrangThai = new JComboBox<>(new TrangThaiItem[] {
            new TrangThaiItem(Phim.DANG_CHIEU, "Đang chiếu"),
            new TrangThaiItem(Phim.SAP_CHIEU, "Sắp chiếu"),
            new TrangThaiItem(Phim.NGUNG_CHIEU, "Ngừng chiếu")
    });
    private final JTextField txtPoster = new JTextField(20);
    private final JTextArea txtMoTa = new JTextArea(4, 20);
    private final JPanel pnlTheLoai = new JPanel(new GridLayout(0, 2, 6, 4));
    private final Map<Integer, JCheckBox> chkTheLoaiMap = new LinkedHashMap<>();

    private final JButton btnThem = Theme.nutChinh("Thêm mới");
    private final JButton btnCapNhat = Theme.nutChinh("Cập nhật");
    private final JButton btnXoa = Theme.nutPhu("Xóa phim");
    private final JButton btnLamMoiForm = Theme.nutPhu("Nhập mới");

    private transient List<Phim> dsPhimHienTai = new ArrayList<>();
    private Integer maPhimDangChon;
    private boolean daNapLanDau;

    private record TrangThaiItem(String ma, String ten) {
        @Override
        public String toString() { return ten; }
    }

    public QuanLyPhimPanel(DieuHuong dieuHuong) {
        super(new BorderLayout(0, 12));
        this.dieuHuong = dieuHuong;
        setBackground(Theme.MAU_NEN);
        setBorder(Theme.le(16));

        add(taoThanhTren(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoKhuDanhSach(), taoKhuForm());
        split.setResizeWeight(0.62);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);
    }

    private JPanel taoThanhTren() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(Theme.tieuDe("Quản lý danh mục phim"), BorderLayout.WEST);

        JPanel tim = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tim.setOpaque(false);
        tim.add(new JLabel("Tìm tên phim:"));
        txtTimKiem.addActionListener(e -> taiDuLieu());
        tim.add(txtTimKiem);
        JButton btnTim = Theme.nutPhu("Tìm");
        btnTim.addActionListener(e -> taiDuLieu());
        JButton btnTaiLai = Theme.nutPhu("Tải lại");
        btnTaiLai.addActionListener(e -> {
            txtTimKiem.setText("");
            taiDuLieu();
        });
        tim.add(btnTim);
        tim.add(btnTaiLai);
        p.add(tim, BorderLayout.EAST);
        return p;
    }

    private JPanel taoKhuDanhSach() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.MAU_THE);
        p.setBorder(Theme.khung("Danh sách phim"));

        Theme.dinhDangBang(bangPhim);
        bangPhim.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bangPhim.getColumnModel().getColumn(0).setPreferredWidth(45);
        bangPhim.getColumnModel().getColumn(1).setPreferredWidth(190);
        bangPhim.getColumnModel().getColumn(2).setPreferredWidth(80);
        bangPhim.getColumnModel().getColumn(6).setPreferredWidth(140);

        bangPhim.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienChiTietDongChon();
            }
        });

        p.add(new JScrollPane(bangPhim), BorderLayout.CENTER);
        return p;
    }

    private JPanel taoKhuForm() {
        JPanel boc = new JPanel(new BorderLayout(0, 10));
        boc.setBackground(Theme.MAU_THE);
        boc.setBorder(Theme.khung("Thông tin chi tiết phim"));
        boc.setPreferredSize(new Dimension(410, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        spnNgayKhoiChieu.setEditor(new JSpinner.DateEditor(spnNgayKhoiChieu, "dd/MM/yyyy"));
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        pnlTheLoai.setOpaque(false);

        int row = 0;
        themDong(form, c, row++, "Tên phim (*)", txtTenPhim);
        themDong(form, c, row++, "Thời lượng (phút)", spnThoiLuong);
        themDong(form, c, row++, "Đạo diễn (*)", txtDaoDien);
        themDong(form, c, row++, "Ngày khởi chiếu", spnNgayKhoiChieu);
        themDong(form, c, row++, "Độ tuổi", cboDoTuoi);
        themDong(form, c, row++, "Trạng thái", cboTrangThai);
        themDong(form, c, row++, "Ảnh poster", txtPoster);

        c.gridy = row++;
        c.gridx = 0;
        c.weightx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Thể loại (*)"), c);
        c.gridx = 1;
        c.weightx = 1;
        JScrollPane spTheLoai = new JScrollPane(pnlTheLoai);
        spTheLoai.setPreferredSize(new Dimension(220, 88));
        form.add(spTheLoai, c);

        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        form.add(new JLabel("Mô tả"), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(new JScrollPane(txtMoTa), c);

        JPanel khuNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        khuNut.setOpaque(false);
        btnLamMoiForm.addActionListener(e -> xoaTrangForm());
        btnThem.addActionListener(e -> luuPhim(true));
        btnCapNhat.addActionListener(e -> luuPhim(false));
        btnXoa.addActionListener(e -> xoaPhim());
        khuNut.add(btnLamMoiForm);
        khuNut.add(btnXoa);
        khuNut.add(btnCapNhat);
        khuNut.add(btnThem);

        boc.add(new JScrollPane(form), BorderLayout.CENTER);
        boc.add(khuNut, BorderLayout.SOUTH);
        capNhatTrangThaiNut();
        return boc;
    }

    private static void themDong(JPanel p, GridBagConstraints c, int row, String nhan, java.awt.Component comp) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        p.add(new JLabel(nhan), c);
        c.gridx = 1;
        c.weightx = 1;
        p.add(comp, c);
    }

    @Override
    public void khiHienThi(Object thamSo) {
        if (!daNapLanDau) {
            daNapLanDau = true;
            napDanhSachTheLoaiVaPhim();
        } else {
            taiDuLieu();
        }
    }

    private void napDanhSachTheLoaiVaPhim() {
        new SwingWorker<List<TheLoai>, Void>() {
            @Override
            protected List<TheLoai> doInBackground() {
                return theLoaiDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<TheLoai> dsTL = get();
                    pnlTheLoai.removeAll();
                    chkTheLoaiMap.clear();
                    for (TheLoai tl : dsTL) {
                        JCheckBox chk = new JCheckBox(tl.getTenTL());
                        chk.setOpaque(false);
                        chkTheLoaiMap.put(tl.getMaTL(), chk);
                        pnlTheLoai.add(chk);
                    }
                    pnlTheLoai.revalidate();
                    pnlTheLoai.repaint();
                    taiDuLieu();
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi("Không tải được danh sách thể loại", ex);
                }
            }
        }.execute();
    }

    private void taiDuLieu() {
        String tuKhoa = txtTimKiem.getText().trim();
        new SwingWorker<List<Phim>, Void>() {
            @Override
            protected List<Phim> doInBackground() {
                return tuKhoa.isEmpty() ? phimDAO.findAll() : phimDAO.search(tuKhoa, null);
            }

            @Override
            protected void done() {
                try {
                    dsPhimHienTai = get();
                    modelBang.setRowCount(0);
                    for (Phim p : dsPhimHienTai) {
                        String tenTL = p.getTheLoais().stream()
                                .map(TheLoai::getTenTL)
                                .collect(Collectors.joining(", "));
                        String ngayKC = p.getNgayKhoiChieu() == null ? "" : DINH_DANG_NGAY.format(p.getNgayKhoiChieu());
                        modelBang.addRow(new Object[] {
                                p.getMaPhim(),
                                p.getTenPhim(),
                                p.getThoiLuong() + " phút",
                                p.getDaoDien(),
                                ngayKC,
                                p.getDoTuoi(),
                                tenTL,
                                tenTrangThai(p.getTrangThai())
                        });
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi("Không tải được danh sách phim", ex);
                }
            }
        }.execute();
    }

    private void hienChiTietDongChon() {
        int row = bangPhim.getSelectedRow();
        if (row < 0) {
            return;
        }
        Phim p = dsPhimHienTai.get(bangPhim.convertRowIndexToModel(row));
        maPhimDangChon = p.getMaPhim();
        txtTenPhim.setText(p.getTenPhim());
        spnThoiLuong.setValue(p.getThoiLuong() > 0 ? p.getThoiLuong() : 90);
        txtDaoDien.setText(p.getDaoDien() == null ? "" : p.getDaoDien());
        LocalDate ngay = p.getNgayKhoiChieu() != null ? p.getNgayKhoiChieu() : LocalDate.now();
        spnNgayKhoiChieu.setValue(Date.from(ngay.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        cboDoTuoi.setSelectedItem(p.getDoTuoi() != null ? p.getDoTuoi() : "P");
        for (int i = 0; i < cboTrangThai.getItemCount(); i++) {
            if (cboTrangThai.getItemAt(i).ma().equals(p.getTrangThai())) {
                cboTrangThai.setSelectedIndex(i);
                break;
            }
        }
        txtPoster.setText(p.getPoster() == null ? "" : p.getPoster());
        txtMoTa.setText(p.getMoTa() == null ? "" : p.getMoTa());

        Set<Integer> maDaChon = p.getTheLoais().stream().map(TheLoai::getMaTL).collect(Collectors.toSet());
        chkTheLoaiMap.forEach((maTL, chk) -> chk.setSelected(maDaChon.contains(maTL)));
        capNhatTrangThaiNut();
    }

    private Phim docVaKiemTraForm(boolean laThemMoi) throws NghiepVuException {
        String ten = txtTenPhim.getText().trim();
        if (ten.isEmpty()) {
            throw new NghiepVuException("Vui lòng nhập tên phim.");
        }
        String daoDien = txtDaoDien.getText().trim();
        if (daoDien.isEmpty()) {
            throw new NghiepVuException("Vui lòng nhập tên đạo diễn.");
        }
        List<TheLoai> dsTheLoaiChon = new ArrayList<>();
        chkTheLoaiMap.forEach((maTL, chk) -> {
            if (chk.isSelected()) {
                dsTheLoaiChon.add(new TheLoai(maTL, chk.getText()));
            }
        });
        if (dsTheLoaiChon.isEmpty()) {
            throw new NghiepVuException("Vui lòng chọn ít nhất 1 thể loại cho phim.");
        }

        Phim p = new Phim();
        if (!laThemMoi) {
            if (maPhimDangChon == null) {
                throw new NghiepVuException("Vui lòng chọn phim cần cập nhật trên bảng.");
            }
            p.setMaPhim(maPhimDangChon);
        }
        p.setTenPhim(ten);
        p.setThoiLuong(((Number) spnThoiLuong.getValue()).intValue());
        p.setDaoDien(daoDien);
        Date d = (Date) spnNgayKhoiChieu.getValue();
        p.setNgayKhoiChieu(d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        p.setDoTuoi((String) cboDoTuoi.getSelectedItem());
        TrangThaiItem tt = (TrangThaiItem) cboTrangThai.getSelectedItem();
        p.setTrangThai(tt == null ? Phim.DANG_CHIEU : tt.ma());
        String poster = txtPoster.getText().trim();
        p.setPoster(poster.isEmpty() ? null : poster);
        String moTa = txtMoTa.getText().trim();
        p.setMoTa(moTa.isEmpty() ? null : moTa);
        p.setTheLoais(dsTheLoaiChon);
        return p;
    }

    private void luuPhim(boolean laThemMoi) {
        final Phim phim;
        try {
            phim = docVaKiemTraForm(laThemMoi);
        } catch (NghiepVuException ex) {
            JOptionPane.showMessageDialog(dieuHuong.getCuaSo(), ex.getMessage(),
                    "Dữ liệu chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Integer> dsMaTL = phim.getTheLoais().stream().map(TheLoai::getMaTL).toList();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                if (laThemMoi) {
                    int maMoi = phimDAO.insert(phim);
                    phimDAO.capNhatTheLoai(maMoi, dsMaTL);
                } else {
                    phimDAO.update(phim);
                    phimDAO.capNhatTheLoai(phim.getMaPhim(), dsMaTL);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(dieuHuong.getCuaSo(),
                            laThemMoi ? "Thêm phim mới thành công!" : "Cập nhật thông tin phim thành công!",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    xoaTrangForm();
                    taiDuLieu();
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi("Lưu thông tin phim thất bại", ex);
                }
            }
        }.execute();
    }

    private void xoaPhim() {
        if (maPhimDangChon == null) {
            return;
        }
        int xacNhan = JOptionPane.showConfirmDialog(dieuHuong.getCuaSo(),
                "Bạn có chắc muốn xóa phim \"" + txtTenPhim.getText().trim() + "\"?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }

        int idXoa = maPhimDangChon;
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return phimDAO.delete(idXoa);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(dieuHuong.getCuaSo(), "Đã xóa phim.",
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        xoaTrangForm();
                        taiDuLieu();
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi("Không thể xóa phim (phim có thể đã có suất chiếu)", ex);
                }
            }
        }.execute();
    }

    private void xoaTrangForm() {
        maPhimDangChon = null;
        bangPhim.clearSelection();
        txtTenPhim.setText("");
        spnThoiLuong.setValue(110);
        txtDaoDien.setText("");
        spnNgayKhoiChieu.setValue(new Date());
        cboDoTuoi.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        txtPoster.setText("");
        txtMoTa.setText("");
        chkTheLoaiMap.values().forEach(chk -> chk.setSelected(false));
        capNhatTrangThaiNut();
    }

    private void capNhatTrangThaiNut() {
        boolean dangChon = maPhimDangChon != null;
        btnCapNhat.setEnabled(dangChon);
        btnXoa.setEnabled(dangChon);
    }

    private static String tenTrangThai(String ma) {
        if (Phim.SAP_CHIEU.equals(ma)) {
            return "Sắp chiếu";
        }
        if (Phim.NGUNG_CHIEU.equals(ma)) {
            return "Ngừng chiếu";
        }
        return "Đang chiếu";
    }

    private void baoLoi(String tieuDe, Throwable ex) {
        Throwable goc = ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
        String chiTiet = goc instanceof DataAccessException ? goc.getMessage() : String.valueOf(goc.getMessage());
        JOptionPane.showMessageDialog(dieuHuong.getCuaSo(), tieuDe + ":\n" + chiTiet,
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}


// ==========================================================================================
// QuanLyPhongPanel   (truoc day: src/main/java/com/nhom/datve/ui/admin/QuanLyPhongPanel.java)
// ==========================================================================================

/**
 * Màn hình quản lý phòng chiếu và sơ đồ ghế cho Quản trị viên (Khánh).
 */
class QuanLyPhongPanel extends JPanel implements ManHinhCon {
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
                    // Le mac dinh cua FlatLaf (~14px moi ben) lam "A10", "D1"... bi cat thanh "..."
                    btnGhe.setMargin(new Insets(2, 2, 2, 2));
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


// ==========================================================================================
// QuanLySuatChieuPanel   (truoc day: src/main/java/com/nhom/datve/ui/admin/QuanLySuatChieuPanel.java)
// ==========================================================================================

/**
 * Màn hình quản lý lịch suất chiếu cho Quản trị viên (Khánh).
 */
class QuanLySuatChieuPanel extends JPanel implements ManHinhCon {
    private static final long serialVersionUID = 1L;
    private static final int PHUT_DON_PHONG = 15;

    private final transient DieuHuong dieuHuong;
    private final transient SuatChieuDAO suatChieuDAO = DAOFactory.suatChieu();
    private final transient PhimDAO phimDAO = DAOFactory.phim();
    private final transient PhongChieuDAO phongDAO = DAOFactory.phongChieu();

    private final DefaultTableModel modelSuat = new DefaultTableModel(
            new Object[] { "Mã suất", "Phim", "Phòng", "Giờ bắt đầu", "Giờ kết thúc", "Giá cơ bản" }, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable bangSuat = new JTable(modelSuat);

    private final JComboBox<Phim> cboPhim = new JComboBox<>();
    private final JComboBox<PhongChieu> cboPhong = new JComboBox<>();
    private final JSpinner spnBatDau = new JSpinner(new SpinnerDateModel());
    private final JSpinner spnKetThuc = new JSpinner(new SpinnerDateModel());
    private final JSpinner spnGiaCoBan = new JSpinner(new SpinnerNumberModel(75_000L, 10_000L, 2_000_000L, 5_000L));

    private final JButton btnTinhGioKT = Theme.nutPhu("Tự tính giờ kết thúc (+15p)");
    private final JButton btnThem = Theme.nutChinh("Thêm suất");
    private final JButton btnCapNhat = Theme.nutChinh("Cập nhật");
    private final JButton btnXoa = Theme.nutPhu("Xóa suất");
    private final JButton btnLamMoi = Theme.nutPhu("Nhập mới");

    private transient List<SuatChieu> dsSuat = new ArrayList<>();
    private transient Map<Integer, Phim> banDoPhim = Map.of();
    private transient Map<Integer, PhongChieu> banDoPhong = Map.of();
    private Integer maSuatDangChon;
    private boolean daNapLanDau;

    public QuanLySuatChieuPanel(DieuHuong dieuHuong) {
        super(new BorderLayout(0, 12));
        this.dieuHuong = dieuHuong;
        setBackground(Theme.MAU_NEN);
        setBorder(Theme.le(16));

        JPanel tren = new JPanel(new BorderLayout());
        tren.setOpaque(false);
        tren.add(Theme.tieuDe("Quản lý lịch suất chiếu"), BorderLayout.WEST);
        JButton btnTaiLai = Theme.nutPhu("Tải lại dữ liệu");
        btnTaiLai.addActionListener(e -> napDanhMucVaSuatChieu());
        tren.add(btnTaiLai, BorderLayout.EAST);
        add(tren, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taoBangDanhSach(), taoFormNhap());
        split.setResizeWeight(0.62);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);
    }

    private JPanel taoBangDanhSach() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.MAU_THE);
        p.setBorder(Theme.khung("Danh sách suất chiếu"));

        Theme.dinhDangBang(bangSuat);
        bangSuat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bangSuat.getColumnModel().getColumn(0).setPreferredWidth(65);
        bangSuat.getColumnModel().getColumn(1).setPreferredWidth(200);
        bangSuat.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienChiTietDongChon();
            }
        });

        p.add(new JScrollPane(bangSuat), BorderLayout.CENTER);
        return p;
    }

    private JPanel taoFormNhap() {
        JPanel boc = new JPanel(new BorderLayout(0, 10));
        boc.setBackground(Theme.MAU_THE);
        boc.setBorder(Theme.khung("Thông tin suất chiếu"));
        boc.setPreferredSize(new Dimension(410, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        spnBatDau.setEditor(new JSpinner.DateEditor(spnBatDau, "dd/MM/yyyy HH:mm"));
        spnKetThuc.setEditor(new JSpinner.DateEditor(spnKetThuc, "dd/MM/yyyy HH:mm"));
        datGioMacDinh();

        cboPhim.addActionListener(e -> tuDongTinhGioKetThuc());
        spnBatDau.addChangeListener(e -> tuDongTinhGioKetThuc());
        btnTinhGioKT.addActionListener(e -> tuDongTinhGioKetThuc());

        int r = 0;
        themDong(form, c, r++, "Phim (*)", cboPhim);
        themDong(form, c, r++, "Phòng chiếu (*)", cboPhong);
        themDong(form, c, r++, "Giờ bắt đầu (*)", spnBatDau);
        themDong(form, c, r++, "Giờ kết thúc (*)", spnKetThuc);

        c.gridy = r++;
        c.gridx = 1;
        c.weightx = 1;
        form.add(btnTinhGioKT, c);

        themDong(form, c, r, "Giá cơ bản (đ) (*)", spnGiaCoBan);

        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        nut.setOpaque(false);
        btnLamMoi.addActionListener(e -> xoaTrangForm());
        btnThem.addActionListener(e -> luuSuatChieu(true));
        btnCapNhat.addActionListener(e -> luuSuatChieu(false));
        btnXoa.addActionListener(e -> xoaSuatChieu());

        nut.add(btnLamMoi);
        nut.add(btnXoa);
        nut.add(btnCapNhat);
        nut.add(btnThem);

        boc.add(form, BorderLayout.NORTH);
        boc.add(nut, BorderLayout.SOUTH);
        capNhatTrangThaiNut();
        return boc;
    }

    private static void themDong(JPanel p, GridBagConstraints c, int row, String nhan, java.awt.Component comp) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        p.add(new JLabel(nhan), c);
        c.gridx = 1;
        c.weightx = 1;
        p.add(comp, c);
    }

    @Override
    public void khiHienThi(Object thamSo) {
        if (!daNapLanDau) {
            daNapLanDau = true;
        }
        napDanhMucVaSuatChieu();
    }

    private record DuLieuSuatChieu(List<Phim> phims, List<PhongChieu> phongs, List<SuatChieu> suats) {
    }

    private void napDanhMucVaSuatChieu() {
        new SwingWorker<DuLieuSuatChieu, Void>() {
            @Override
            protected DuLieuSuatChieu doInBackground() {
                List<Phim> phims = phimDAO.findAll().stream()
                        .filter(p -> !Phim.NGUNG_CHIEU.equals(p.getTrangThai()))
                        .toList();
                List<PhongChieu> phongs = phongDAO.findDangHoatDong();
                List<SuatChieu> suats = suatChieuDAO.findAll();
                return new DuLieuSuatChieu(phims, phongs, suats);
            }

            @Override
            protected void done() {
                try {
                    DuLieuSuatChieu dl = get();
                    banDoPhim = dl.phims().stream()
                            .collect(Collectors.toMap(Phim::getMaPhim, Function.identity(), (a, b) -> a));
                    banDoPhong = dl.phongs().stream()
                            .collect(Collectors.toMap(PhongChieu::getMaPhong, Function.identity(), (a, b) -> a));

                    DefaultComboBoxModel<Phim> mPhim = new DefaultComboBoxModel<>();
                    dl.phims().forEach(mPhim::addElement);
                    cboPhim.setModel(mPhim);

                    DefaultComboBoxModel<PhongChieu> mPhong = new DefaultComboBoxModel<>();
                    dl.phongs().forEach(mPhong::addElement);
                    cboPhong.setModel(mPhong);

                    tuDongTinhGioKetThuc();

                    dsSuat = dl.suats();
                    modelSuat.setRowCount(0);
                    for (SuatChieu sc : dsSuat) {
                        String tenPhim = sc.getTenPhim() != null ? sc.getTenPhim()
                                : (banDoPhim.containsKey(sc.getMaPhim()) ? banDoPhim.get(sc.getMaPhim()).getTenPhim()
                                        : "#" + sc.getMaPhim());
                        String tenPhong = sc.getTenPhong() != null ? sc.getTenPhong()
                                : (banDoPhong.containsKey(sc.getMaPhong())
                                        ? banDoPhong.get(sc.getMaPhong()).getTenPhong()
                                        : "#" + sc.getMaPhong());
                        modelSuat.addRow(new Object[] {
                                sc.getMaSuat(),
                                tenPhim,
                                tenPhong,
                                Theme.ngayGio(sc.getGioBatDau()),
                                Theme.ngayGio(sc.getGioKetThuc()),
                                Theme.tien(sc.getGiaCoBan())
                        });
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi("Không tải được dữ liệu suất chiếu", ex);
                }
            }
        }.execute();
    }

    private void hienChiTietDongChon() {
        int row = bangSuat.getSelectedRow();
        if (row < 0) {
            return;
        }
        SuatChieu sc = dsSuat.get(bangSuat.convertRowIndexToModel(row));
        maSuatDangChon = sc.getMaSuat();

        for (int i = 0; i < cboPhim.getItemCount(); i++) {
            if (cboPhim.getItemAt(i).getMaPhim() == sc.getMaPhim()) {
                cboPhim.setSelectedIndex(i);
                break;
            }
        }
        for (int i = 0; i < cboPhong.getItemCount(); i++) {
            if (cboPhong.getItemAt(i).getMaPhong() == sc.getMaPhong()) {
                cboPhong.setSelectedIndex(i);
                break;
            }
        }
        spnBatDau.setValue(Date.from(sc.getGioBatDau().atZone(ZoneId.systemDefault()).toInstant()));
        spnKetThuc.setValue(Date.from(sc.getGioKetThuc().atZone(ZoneId.systemDefault()).toInstant()));
        spnGiaCoBan.setValue(sc.getGiaCoBan().longValue());
        capNhatTrangThaiNut();
    }

    private void tuDongTinhGioKetThuc() {
        Phim phim = (Phim) cboPhim.getSelectedItem();
        if (phim == null) {
            return;
        }
        LocalDateTime bd = docNgayGio(spnBatDau);
        LocalDateTime kt = bd.plusMinutes(phim.getThoiLuong() + PHUT_DON_PHONG);
        spnKetThuc.setValue(Date.from(kt.atZone(ZoneId.systemDefault()).toInstant()));
    }

    private SuatChieu docForm(boolean laThemMoi) throws NghiepVuException {
        Phim phim = (Phim) cboPhim.getSelectedItem();
        PhongChieu phong = (PhongChieu) cboPhong.getSelectedItem();
        if (phim == null || phong == null) {
            throw new NghiepVuException("Vui lòng chọn đầy đủ phim và phòng chiếu.");
        }
        LocalDateTime bd = docNgayGio(spnBatDau);
        LocalDateTime kt = docNgayGio(spnKetThuc);
        if (!kt.isAfter(bd)) {
            throw new NghiepVuException("Giờ kết thúc phải sau giờ bắt đầu.");
        }
        long gia = ((Number) spnGiaCoBan.getValue()).longValue();
        if (gia <= 0) {
            throw new NghiepVuException("Giá cơ bản phải lớn hơn 0.");
        }

        SuatChieu sc = new SuatChieu();
        if (!laThemMoi) {
            if (maSuatDangChon == null) {
                throw new NghiepVuException("Vui lòng chọn suất chiếu cần cập nhật.");
            }
            sc.setMaSuat(maSuatDangChon);
        }
        sc.setMaPhim(phim.getMaPhim());
        sc.setMaPhong(phong.getMaPhong());
        sc.setTenPhim(phim.getTenPhim());
        sc.setTenPhong(phong.getTenPhong());
        sc.setGioBatDau(bd);
        sc.setGioKetThuc(kt);
        sc.setGiaCoBan(BigDecimal.valueOf(gia));
        return sc;
    }

    private void luuSuatChieu(boolean laThemMoi) {
        final SuatChieu sc;
        try {
            sc = docForm(laThemMoi);
        } catch (NghiepVuException ex) {
            JOptionPane.showMessageDialog(dieuHuong.getCuaSo(), ex.getMessage(),
                    "Dữ liệu chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer boQua = laThemMoi ? null : sc.getMaSuat();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws NghiepVuException {
                if (suatChieuDAO.biTrungLich(sc.getMaPhong(), sc.getGioBatDau(), sc.getGioKetThuc(), boQua)) {
                    throw new NghiepVuException("Phòng " + sc.getTenPhong()
                            + " đã có suất chiếu khác trùng trong khung giờ này.");
                }
                if (laThemMoi) {
                    suatChieuDAO.insert(sc);
                } else {
                    suatChieuDAO.update(sc);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(dieuHuong.getCuaSo(),
                            laThemMoi ? "Thêm suất chiếu thành công!" : "Cập nhật suất chiếu thành công!",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    xoaTrangForm();
                    napDanhMucVaSuatChieu();
                } catch (ExecutionException ex) {
                    if (ex.getCause() instanceof NghiepVuException nv) {
                        JOptionPane.showMessageDialog(dieuHuong.getCuaSo(), nv.getMessage(),
                                "Trùng lịch chiếu", JOptionPane.WARNING_MESSAGE);
                    } else {
                        baoLoi("Không lưu được suất chiếu", ex);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void xoaSuatChieu() {
        if (maSuatDangChon == null) {
            return;
        }
        int xacNhan = JOptionPane.showConfirmDialog(dieuHuong.getCuaSo(),
                "Bạn có chắc muốn xóa suất chiếu #" + maSuatDangChon + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) {
            return;
        }
        int idXoa = maSuatDangChon;
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return suatChieuDAO.delete(idXoa);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        xoaTrangForm();
                        napDanhMucVaSuatChieu();
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    baoLoi("Không thể xóa suất chiếu (suất chiếu có thể đã có khách đặt vé)", ex);
                }
            }
        }.execute();
    }

    private void xoaTrangForm() {
        maSuatDangChon = null;
        bangSuat.clearSelection();
        datGioMacDinh();
        tuDongTinhGioKetThuc();
        spnGiaCoBan.setValue(75_000L);
        capNhatTrangThaiNut();
    }

    private void datGioMacDinh() {
        LocalDateTime macDinh = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(19, 0));
        spnBatDau.setValue(Date.from(macDinh.atZone(ZoneId.systemDefault()).toInstant()));
        spnKetThuc.setValue(Date.from(macDinh.plusHours(2).atZone(ZoneId.systemDefault()).toInstant()));
    }

    private static LocalDateTime docNgayGio(JSpinner spinner) {
        Date d = (Date) spinner.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().withSecond(0).withNano(0);
    }

    private void capNhatTrangThaiNut() {
        boolean coChon = maSuatDangChon != null;
        btnCapNhat.setEnabled(coChon);
        btnXoa.setEnabled(coChon);
    }

    private void baoLoi(String tieuDe, Throwable ex) {
        Throwable goc = ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
        JOptionPane.showMessageDialog(dieuHuong.getCuaSo(), tieuDe + ":\n" + goc.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}


// ==========================================================================================
// ThongKePanel   (truoc day: src/main/java/com/nhom/datve/ui/admin/ThongKePanel.java)
// ==========================================================================================

/**
 * Màn hình thống kê doanh thu cho Quản trị viên (Khánh), sử dụng ThongKeDAO
 * (fn_DoanhThu).
 */
class ThongKePanel extends JPanel implements ManHinhCon {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final transient DieuHuong dieuHuong;
    private final transient ThongKeDAO thongKeDAO = DAOFactory.thongKe();

    private final JSpinner spnTuNgay = new JSpinner(new SpinnerDateModel());
    private final JSpinner spnDenNgay = new JSpinner(new SpinnerDateModel());
    private final JButton btnThongKe = Theme.nutChinh("Thống kê");

    private final JLabel lblTongDoanhThu = new JLabel("0 đ");
    private final JLabel lblSoPhim = new JLabel("0 phim");
    private final JLabel lblSoNgay = new JLabel("0 ngày");

    private final DefaultTableModel modelTheoPhim = new DefaultTableModel(
            new Object[] { "Hạng", "Tên phim", "Doanh thu", "Tỷ trọng (%)" }, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel modelTheoNgay = new DefaultTableModel(
            new Object[] { "Ngày giao dịch", "Doanh thu" }, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private boolean daNapLanDau;

    public ThongKePanel(DieuHuong dieuHuong) {
        super(new BorderLayout(0, 14));
        this.dieuHuong = dieuHuong;
        setBackground(Theme.MAU_NEN);
        setBorder(Theme.le(16));

        JPanel tren = new JPanel(new BorderLayout(0, 12));
        tren.setOpaque(false);
        tren.add(taoThanhBoLoc(), BorderLayout.NORTH);
        tren.add(taoTheTongHop(), BorderLayout.CENTER);
        add(tren, BorderLayout.NORTH);

        add(taoHaiBangChiTiet(), BorderLayout.CENTER);
    }

    private JPanel taoThanhBoLoc() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(Theme.tieuDe("Thống kê doanh thu bán vé"), BorderLayout.WEST);

        spnTuNgay.setEditor(new JSpinner.DateEditor(spnTuNgay, "dd/MM/yyyy"));
        spnDenNgay.setEditor(new JSpinner.DateEditor(spnDenNgay, "dd/MM/yyyy"));
        datKhoangNgay(LocalDate.now().minusDays(30), LocalDate.now());

        JButton btnHomNay = Theme.nutPhu("Hôm nay");
        btnHomNay.addActionListener(e -> {
            LocalDate nay = LocalDate.now();
            datKhoangNgay(nay, nay);
            chayThongKe();
        });
        JButton btn7Ngay = Theme.nutPhu("7 ngày qua");
        btn7Ngay.addActionListener(e -> {
            datKhoangNgay(LocalDate.now().minusDays(6), LocalDate.now());
            chayThongKe();
        });
        JButton btnThangNay = Theme.nutPhu("Tháng này");
        btnThangNay.addActionListener(e -> {
            LocalDate nay = LocalDate.now();
            datKhoangNgay(nay.withDayOfMonth(1), nay);
            chayThongKe();
        });
        btnThongKe.addActionListener(e -> chayThongKe());

        JPanel boLoc = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        boLoc.setOpaque(false);
        boLoc.add(new JLabel("Từ ngày:"));
        boLoc.add(spnTuNgay);
        boLoc.add(new JLabel("Đến ngày:"));
        boLoc.add(spnDenNgay);
        boLoc.add(btnHomNay);
        boLoc.add(btn7Ngay);
        boLoc.add(btnThangNay);
        boLoc.add(btnThongKe);

        p.add(boLoc, BorderLayout.EAST);
        return p;
    }

    private JPanel taoTheTongHop() {
        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 0));
        grid.setOpaque(false);

        lblTongDoanhThu.setFont(Theme.FONT_TIEU_DE);
        lblTongDoanhThu.setForeground(Theme.MAU_CHINH);

        lblSoPhim.setFont(Theme.FONT_TIEU_DE);
        lblSoPhim.setForeground(Theme.MAU_THANH_CONG);

        lblSoNgay.setFont(Theme.FONT_TIEU_DE);
        lblSoNgay.setForeground(Theme.MAU_CHU);

        grid.add(taoCard("Tổng doanh thu thực thu", lblTongDoanhThu, "Tính trên các giao dịch thanh toán thành công"));
        grid.add(taoCard("Số phim có doanh thu", lblSoPhim, "Số lượng phim phát sinh vé trong kỳ"));
        grid.add(taoCard("Số ngày có giao dịch", lblSoNgay, "Số ngày ghi nhận thanh toán thành công"));
        return grid;
    }

    private static JPanel taoCard(String tieuDe, JLabel giaTri, String ghiChu) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Theme.MAU_THE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.MAU_VIEN),
                Theme.le(14)));
        card.add(Theme.chuPhu(tieuDe), BorderLayout.NORTH);
        card.add(giaTri, BorderLayout.CENTER);
        card.add(Theme.chuPhu(ghiChu), BorderLayout.SOUTH);
        return card;
    }

    private JSplitPane taoHaiBangChiTiet() {
        JTable bangPhim = new JTable(modelTheoPhim);
        Theme.dinhDangBang(bangPhim);
        bangPhim.getColumnModel().getColumn(0).setPreferredWidth(55);
        bangPhim.getColumnModel().getColumn(1).setPreferredWidth(230);

        DefaultTableCellRenderer canPhai = new DefaultTableCellRenderer();
        canPhai.setHorizontalAlignment(SwingConstants.RIGHT);
        bangPhim.getColumnModel().getColumn(2).setCellRenderer(canPhai);
        bangPhim.getColumnModel().getColumn(3).setCellRenderer(canPhai);

        JTable bangNgay = new JTable(modelTheoNgay);
        Theme.dinhDangBang(bangNgay);
        bangNgay.getColumnModel().getColumn(1).setCellRenderer(canPhai);

        JPanel pnlPhim = new JPanel(new BorderLayout());
        pnlPhim.setBackground(Theme.MAU_THE);
        pnlPhim.setBorder(Theme.khung("Doanh thu theo phim"));
        pnlPhim.add(new JScrollPane(bangPhim), BorderLayout.CENTER);

        JPanel pnlNgay = new JPanel(new BorderLayout());
        pnlNgay.setBackground(Theme.MAU_THE);
        pnlNgay.setBorder(Theme.khung("Doanh thu theo ngày"));
        pnlNgay.add(new JScrollPane(bangNgay), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlPhim, pnlNgay);
        split.setResizeWeight(0.58);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        return split;
    }

    @Override
    public void khiHienThi(Object thamSo) {
        if (!daNapLanDau) {
            daNapLanDau = true;
        }
        chayThongKe();
    }

    private record KetQuaThongKe(
            BigDecimal tongDoanhThu,
            Map<String, BigDecimal> theoPhim,
            Map<LocalDate, BigDecimal> theoNgay) {
    }

    private void chayThongKe() {
        LocalDate tu = docNgay(spnTuNgay);
        LocalDate den = docNgay(spnDenNgay);
        if (tu.isAfter(den)) {
            JOptionPane.showMessageDialog(dieuHuong.getCuaSo(),
                    "\"Từ ngày\" không được lớn hơn \"Đến ngày\".",
                    "Khoảng ngày chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnThongKe.setEnabled(false);
        new SwingWorker<KetQuaThongKe, Void>() {
            @Override
            protected KetQuaThongKe doInBackground() throws NghiepVuException {
                BigDecimal tong = thongKeDAO.tongDoanhThu(tu, den);
                Map<String, BigDecimal> theoPhim = thongKeDAO.doanhThuTheoPhim(tu, den);
                Map<LocalDate, BigDecimal> theoNgay = thongKeDAO.doanhThuTheoNgay(tu, den);
                return new KetQuaThongKe(tong, theoPhim, theoNgay);
            }

            @Override
            protected void done() {
                btnThongKe.setEnabled(true);
                try {
                    KetQuaThongKe kq = get();
                    BigDecimal tong = kq.tongDoanhThu() == null ? BigDecimal.ZERO : kq.tongDoanhThu();
                    lblTongDoanhThu.setText(Theme.tien(tong));
                    lblSoPhim.setText(kq.theoPhim().size() + " phim");
                    lblSoNgay.setText(kq.theoNgay().size() + " ngày");

                    modelTheoPhim.setRowCount(0);
                    int hang = 1;
                    for (Map.Entry<String, BigDecimal> e : kq.theoPhim().entrySet()) {
                        BigDecimal dt = e.getValue();
                        String tyTrong = tong.compareTo(BigDecimal.ZERO) > 0
                                ? dt.multiply(BigDecimal.valueOf(100))
                                        .divide(tong, 1, RoundingMode.HALF_UP) + "%"
                                : "0.0%";
                        modelTheoPhim.addRow(new Object[] { hang++, e.getKey(), Theme.tien(dt), tyTrong });
                    }

                    modelTheoNgay.setRowCount(0);
                    for (Map.Entry<LocalDate, BigDecimal> e : kq.theoNgay().entrySet()) {
                        modelTheoNgay.addRow(new Object[] {
                                DINH_DANG_NGAY.format(e.getKey()),
                                Theme.tien(e.getValue())
                        });
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    Throwable goc = ex instanceof ExecutionException && ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(dieuHuong.getCuaSo(),
                            "Không tải được dữ liệu thống kê:\n" + goc.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void datKhoangNgay(LocalDate tu, LocalDate den) {
        spnTuNgay.setValue(Date.from(tu.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        spnDenNgay.setValue(Date.from(den.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private static LocalDate docNgay(JSpinner spinner) {
        Date d = (Date) spinner.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
