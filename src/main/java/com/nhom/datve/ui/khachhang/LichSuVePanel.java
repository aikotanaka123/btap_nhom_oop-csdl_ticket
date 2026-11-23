package com.nhom.datve.ui.khachhang;

import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.DatVe;
import com.nhom.datve.model.TrangThaiDatVe;
import com.nhom.datve.model.Ve;
import com.nhom.datve.service.DatVeService;
import com.nhom.datve.ui.DieuHuong;
import com.nhom.datve.ui.ManHinh;
import com.nhom.datve.ui.ManHinhCon;
import com.nhom.datve.ui.Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Man hinh lich su ve va huy ve.
 * - JTable hien danh sach cac lan dat ve cua khach hang dang dang nhap.
 * - Nut "Huy ve" goi DatVeService.huyVe(maKH, maDatVe) trong SwingWorker va bao loi bang JOptionPane.
 */
public class LichSuVePanel extends JPanel implements ManHinhCon {
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
