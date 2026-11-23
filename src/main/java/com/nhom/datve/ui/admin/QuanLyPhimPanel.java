package com.nhom.datve.ui.admin;

import com.nhom.datve.dao.DAOFactory;
import com.nhom.datve.dao.PhimDAO;
import com.nhom.datve.dao.TheLoaiDAO;
import com.nhom.datve.exception.DataAccessException;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.Phim;
import com.nhom.datve.model.TheLoai;
import com.nhom.datve.ui.DieuHuong;
import com.nhom.datve.ui.ManHinhCon;
import com.nhom.datve.ui.Theme;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
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
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình quản lý phim cho Quản trị viên (Khánh).
 */
public class QuanLyPhimPanel extends JPanel implements ManHinhCon {
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