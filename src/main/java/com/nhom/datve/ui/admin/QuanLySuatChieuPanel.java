package com.nhom.datve.ui.admin;

import com.nhom.datve.dao.DAOFactory;
import com.nhom.datve.dao.PhimDAO;
import com.nhom.datve.dao.PhongChieuDAO;
import com.nhom.datve.dao.SuatChieuDAO;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.Phim;
import com.nhom.datve.model.PhongChieu;
import com.nhom.datve.model.SuatChieu;
import com.nhom.datve.ui.DieuHuong;
import com.nhom.datve.ui.ManHinhCon;
import com.nhom.datve.ui.Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình quản lý lịch suất chiếu cho Quản trị viên (Khánh).
 */
public class QuanLySuatChieuPanel extends JPanel implements ManHinhCon {
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