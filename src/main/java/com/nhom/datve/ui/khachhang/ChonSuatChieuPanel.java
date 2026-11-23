package com.nhom.datve.ui.khachhang;

import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.Phim;
import com.nhom.datve.model.SuatChieu;
import com.nhom.datve.model.TheLoai;
import com.nhom.datve.service.PhimService;
import com.nhom.datve.ui.DieuHuong;
import com.nhom.datve.ui.ManHinh;
import com.nhom.datve.ui.ManHinhCon;
import com.nhom.datve.ui.Theme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Man hinh chon suat chieu.
 * - Chi goi tang Service (PhimService), khong goi DAO truc tiep.
 * - Nhan tham so Integer maPhim tai khiHienThi(Object thamSo).
 * - Cho phep chon Ngay chieu, Phong chieu va Gio chieu cua phim da chon.
 * - Bam "Chon ghe" -> dieuHuong.chuyenDen(ManHinh.SO_DO_GHE, suat.getMaSuat()).
 */
public class ChonSuatChieuPanel extends JPanel implements ManHinhCon {
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
