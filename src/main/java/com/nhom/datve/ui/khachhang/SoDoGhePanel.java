package com.nhom.datve.ui.khachhang;

import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.DatVe;
import com.nhom.datve.model.Ghe;
import com.nhom.datve.model.PhongChieu;
import com.nhom.datve.model.SuatChieu;
import com.nhom.datve.service.DatVeService;
import com.nhom.datve.service.SoDoGhe;
import com.nhom.datve.ui.DieuHuong;
import com.nhom.datve.ui.ManHinh;
import com.nhom.datve.ui.ManHinhCon;
import com.nhom.datve.ui.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

/**
 * Man hinh so do chon ghe.
 * - Nhan tham so Integer maSuat tai khiHienThi(Object thamSo).
 * - Dung luoi JToggleButton (GridLayout) voi mau Theme.GHE_TRONG, GHE_DANG_CHON, GHE_DA_DAT, GHE_VIP, GHE_DOI.
 * - Nut "Dat ve" goi XacNhanDatVeDialog.hienThi(...).
 */
public class SoDoGhePanel extends JPanel implements ManHinhCon {
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
        JLabel manHinh = new JLabel("──────────  MÀN HÌNH CHIẾU  ──────────", SwingConstants.CENTER);
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
