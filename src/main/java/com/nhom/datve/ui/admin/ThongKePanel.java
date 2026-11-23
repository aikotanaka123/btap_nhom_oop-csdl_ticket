package com.nhom.datve.ui.admin;

import com.nhom.datve.dao.DAOFactory;
import com.nhom.datve.dao.ThongKeDAO;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.ui.DieuHuong;
import com.nhom.datve.ui.ManHinhCon;
import com.nhom.datve.ui.Theme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình thống kê doanh thu cho Quản trị viên (Khánh), sử dụng ThongKeDAO
 * (fn_DoanhThu).
 */
public class ThongKePanel extends JPanel implements ManHinhCon {
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