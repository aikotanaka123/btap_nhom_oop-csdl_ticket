package com.nhom.datve.ui.khachhang;

import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.DatVe;
import com.nhom.datve.model.Ghe;
import com.nhom.datve.model.PhuongThucThanhToan;
import com.nhom.datve.model.SuatChieu;
import com.nhom.datve.service.DatVeService;
import com.nhom.datve.ui.Theme;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

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
public class XacNhanDatVeDialog extends JDialog {
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
