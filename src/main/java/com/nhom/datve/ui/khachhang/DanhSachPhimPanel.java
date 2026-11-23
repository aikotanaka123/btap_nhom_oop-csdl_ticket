package com.nhom.datve.ui.khachhang;

import com.nhom.datve.model.Phim;
import com.nhom.datve.model.TheLoai;
import com.nhom.datve.service.PhimService;
import com.nhom.datve.ui.DieuHuong;
import com.nhom.datve.ui.ManHinh;
import com.nhom.datve.ui.ManHinhCon;
import com.nhom.datve.ui.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

/**
 * Man hinh danh sach phim va chi tiet phim.
 * - Chi goi tang Service (PhimService), khong goi DAO truc tiep.
 * - Dung mau/font chuan tu Theme.
 * - Hien thi luoi the phim: moi the co poster (ImageIcon), ten phim, the loai, thoi luong, do tuoi.
 * - Bam vao the phim hoac nut "Chi tiet phim" -> mo ChiTietPhimDialog.
 * - Bam "Chon suat chieu" -> dieuHuong.chuyenDen(ManHinh.CHON_SUAT_CHIEU, phim.getMaPhim()).
 */
public class DanhSachPhimPanel extends JPanel implements ManHinhCon {
    private static final long serialVersionUID = 1L;
    private static final String TAT_CA_THE_LOAI = "Tất cả thể loại";
    private static final Color[] BANG_MAU_POSTER = {
        Theme.MAU_CHINH,
        Theme.MAU_CHINH_DAM,
        Theme.MAU_THANH_CONG,
        Theme.MAU_CANH_BAO,
        Theme.MAU_CHU
    };

    private final transient DieuHuong dieuHuong;
    private final transient PhimService phimService = new PhimService();

    private final JTextField txtTuKhoa = new JTextField(18);
    private final JComboBox<Object> cboTheLoai = new JComboBox<>();
    private final JPanel luoiPhim = new JPanel(new GridLayout(0, 3, 16, 16));
    private final JLabel lblTrangThai = Theme.chuPhu(" ");

    public DanhSachPhimPanel(DieuHuong dieuHuong) {
        super(new BorderLayout(0, 12));
        this.dieuHuong = dieuHuong;
        setBackground(Theme.MAU_NEN);
        setBorder(Theme.le(16));

        add(taoThanhDau(), BorderLayout.NORTH);

        luoiPhim.setOpaque(false);
        JPanel boc = new JPanel(new BorderLayout());
        boc.setBackground(Theme.MAU_NEN);
        boc.add(luoiPhim, BorderLayout.NORTH);
        JScrollPane cuon = new JScrollPane(boc);
        cuon.setBorder(BorderFactory.createEmptyBorder());
        cuon.getVerticalScrollBar().setUnitIncrement(16);
        add(cuon, BorderLayout.CENTER);

        add(lblTrangThai, BorderLayout.SOUTH);
        napTheLoai();
    }

    private JPanel taoThanhDau() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setOpaque(false);

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        trai.setOpaque(false);
        trai.add(Theme.tieuDe("Phim đang chiếu"));
        p.add(trai, BorderLayout.WEST);

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);
        JButton btnLoc = Theme.nutChinh("Lọc phim");
        JButton btnTimNangCao = Theme.nutPhu("Tìm kiếm nâng cao & Demo SQL");
        btnLoc.addActionListener(e -> napPhim());
        txtTuKhoa.addActionListener(e -> napPhim());
        cboTheLoai.addActionListener(e -> napPhim());
        btnTimNangCao.addActionListener(e -> dieuHuong.chuyenDen(ManHinh.TIM_KIEM_PHIM));

        phai.add(new JLabel("Tên phim:"));
        phai.add(txtTuKhoa);
        phai.add(new JLabel("Thể loại:"));
        phai.add(cboTheLoai);
        phai.add(btnLoc);
        phai.add(btnTimNangCao);
        p.add(phai, BorderLayout.EAST);
        return p;
    }

    @Override
    public void khiHienThi(Object thamSo) {
        napPhim();
    }

    private void napTheLoai() {
        new SwingWorker<List<TheLoai>, Void>() {
            @Override
            protected List<TheLoai> doInBackground() {
                return phimService.layTatCaTheLoai();
            }

            @Override
            protected void done() {
                try {
                    DefaultComboBoxModel<Object> m = new DefaultComboBoxModel<>();
                    m.addElement(TAT_CA_THE_LOAI);
                    get().forEach(m::addElement);
                    cboTheLoai.setModel(m);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    Throwable goc = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(DanhSachPhimPanel.this,
                            "Không tải được thể loại: " + goc.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void napPhim() {
        String tuKhoa = txtTuKhoa.getText();
        Object chonTL = cboTheLoai.getSelectedItem();
        Integer maTL = chonTL instanceof TheLoai t ? t.getMaTL() : null;

        lblTrangThai.setText("Đang tải danh sách phim...");
        new SwingWorker<List<Phim>, Void>() {
            @Override
            protected List<Phim> doInBackground() {
                return phimService.layPhimDangChieu(tuKhoa, maTL);
            }

            @Override
            protected void done() {
                try {
                    List<Phim> ds = get();
                    veDanhSachThePhim(ds);
                    lblTrangThai.setText("Hiển thị " + ds.size() + " phim đang chiếu. Bấm vào thẻ phim để xem chi tiết.");
                } catch (ExecutionException ex) {
                    Throwable goc = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(DanhSachPhimPanel.this,
                            "Không tải được danh sách phim: " + goc.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void veDanhSachThePhim(List<Phim> dsPhim) {
        luoiPhim.removeAll();
        if (dsPhim.isEmpty()) {
            luoiPhim.add(Theme.chuPhu("Không tìm thấy phim phù hợp."));
        } else {
            for (int i = 0; i < dsPhim.size(); i++) {
                Phim phim = dsPhim.get(i);
                Color mau = BANG_MAU_POSTER[i % BANG_MAU_POSTER.length];
                luoiPhim.add(taoThePhim(phim, mau));
            }
        }
        luoiPhim.revalidate();
        luoiPhim.repaint();
    }

    private JPanel taoThePhim(Phim phim, Color mauPoster) {
        JPanel the = new JPanel(new BorderLayout(0, 10));
        the.setBackground(Theme.MAU_THE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.MAU_VIEN),
                Theme.le(12)));

        ImageIcon posterIcon = taoPosterIcon(phim, mauPoster, 260, 165);
        JLabel lblPoster = new JLabel(posterIcon, SwingConstants.CENTER);
        lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        the.add(lblPoster, BorderLayout.NORTH);

        JPanel thongTin = new JPanel();
        thongTin.setLayout(new BoxLayout(thongTin, BoxLayout.Y_AXIS));
        thongTin.setOpaque(false);

        JLabel lblTen = new JLabel(phim.getTenPhim());
        lblTen.setFont(Theme.FONT_TIEU_DE_NHO);
        lblTen.setForeground(Theme.MAU_CHU);

        String chuoiTheLoai = phim.getTheLoais() == null || phim.getTheLoais().isEmpty()
                ? "Chưa phân loại"
                : phim.getTheLoais().stream().map(TheLoai::getTenTL).collect(Collectors.joining(", "));
        JLabel lblTheLoai = Theme.chuPhu("Thể loại: " + chuoiTheLoai);

        String doTuoi = phim.getDoTuoi() != null ? "  ·  Độ tuổi: " + phim.getDoTuoi() : "";
        JLabel lblThoiLuong = Theme.chuPhu("Thời lượng: " + phim.getThoiLuong() + " phút" + doTuoi);

        thongTin.add(lblTen);
        thongTin.add(Box.createVerticalStrut(4));
        thongTin.add(lblTheLoai);
        thongTin.add(Box.createVerticalStrut(2));
        thongTin.add(lblThoiLuong);
        the.add(thongTin, BorderLayout.CENTER);

        JButton btnChiTiet = Theme.nutPhu("Chi tiết phim");
        JButton btnChonSuat = Theme.nutChinh("Chọn suất chiếu");
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        nut.setOpaque(false);
        nut.add(btnChiTiet);
        nut.add(btnChonSuat);
        the.add(nut, BorderLayout.SOUTH);

        Runnable moChiTiet = () -> {
            ChiTietPhimDialog dlg = new ChiTietPhimDialog(dieuHuong.getCuaSo(), phim, posterIcon);
            dlg.setVisible(true);
            if (dlg.isChonSuat()) {
                dieuHuong.chuyenDen(ManHinh.CHON_SUAT_CHIEU, phim.getMaPhim());
            }
        };

        lblPoster.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                moChiTiet.run();
            }
        });
        btnChiTiet.addActionListener(e -> moChiTiet.run());
        btnChonSuat.addActionListener(e -> dieuHuong.chuyenDen(ManHinh.CHON_SUAT_CHIEU, phim.getMaPhim()));
        return the;
    }

    private static ImageIcon taoPosterIcon(Phim phim, Color mauChuDao, int w, int h) {
        if (phim.getPoster() != null && !phim.getPoster().isBlank()) {
            File f = new File(phim.getPoster());
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                if (icon.getIconWidth() > 0) {
                    return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
                }
            }
            URL res = DanhSachPhimPanel.class.getClassLoader().getResource(phim.getPoster());
            if (res != null) {
                ImageIcon icon = new ImageIcon(res);
                if (icon.getIconWidth() > 0) {
                    return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
                }
            }
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(new GradientPaint(0, 0, mauChuDao, w, h, mauChuDao.darker()));
        g2.fillRoundRect(0, 0, w, h, 12, 12);

        String doTuoi = phim.getDoTuoi() != null ? phim.getDoTuoi() : "P";
        g2.setColor(Theme.MAU_CHU);
        g2.fillRoundRect(w - 48, 10, 38, 24, 6, 6);
        g2.setColor(Color.WHITE);
        g2.setFont(Theme.FONT_DAM);
        g2.drawString(doTuoi, w - 41, 27);

        g2.setFont(Theme.FONT_TIEU_DE_NHO);
        String ten = phim.getTenPhim() == null ? "Phim" : phim.getTenPhim();
        if (ten.length() > 22) {
            ten = ten.substring(0, 20) + "...";
        }
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(ten, Math.max(12, (w - fm.stringWidth(ten)) / 2), h / 2);

        g2.setFont(Theme.FONT);
        String phu = phim.getThoiLuong() + " phút";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(phu, Math.max(12, (w - fm2.stringWidth(phu)) / 2), h / 2 + 24);

        g2.dispose();
        return new ImageIcon(img);
    }
}
