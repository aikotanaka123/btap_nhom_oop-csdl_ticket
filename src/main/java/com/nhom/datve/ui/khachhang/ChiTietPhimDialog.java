package com.nhom.datve.ui.khachhang;

import com.nhom.datve.model.Phim;
import com.nhom.datve.model.TheLoai;
import com.nhom.datve.ui.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * Hop thoai xem chi tiet phim.
 * Hien poster lon (ImageIcon), ten phim, the loai, thoi luong, dao dien, do tuoi,
 * ngay khoi chieu, mo ta va nut "Chon suat chieu".
 */
public class ChiTietPhimDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter NGAY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private boolean chonSuat;

    public ChiTietPhimDialog(Window owner, Phim phim, ImageIcon posterNho) {
        super(owner, "Chi tiết phim – " + phim.getTenPhim(), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel goc = new JPanel(new BorderLayout(20, 14));
        goc.setBorder(Theme.le(20));
        goc.setBackground(Theme.MAU_THE);

        // Poster lon ben trai
        JLabel lblPoster = new JLabel(taoPosterLon(phim, posterNho));
        lblPoster.setVerticalAlignment(SwingConstants.TOP);
        goc.add(lblPoster, BorderLayout.WEST);

        // Thong tin chi tiet o giua
        JPanel thongTin = new JPanel();
        thongTin.setLayout(new BoxLayout(thongTin, BoxLayout.Y_AXIS));
        thongTin.setOpaque(false);

        JLabel lblTen = Theme.tieuDe(phim.getTenPhim());
        lblTen.setAlignmentX(Component.LEFT_ALIGNMENT);
        thongTin.add(lblTen);
        thongTin.add(Box.createVerticalStrut(10));

        String dsTheLoai = phim.getTheLoais() == null || phim.getTheLoais().isEmpty()
                ? "Đang cập nhật"
                : phim.getTheLoais().stream().map(TheLoai::getTenTL).collect(Collectors.joining(", "));

        thongTin.add(dongThongTin("Thể loại:  ", dsTheLoai));
        thongTin.add(Box.createVerticalStrut(6));
        thongTin.add(dongThongTin("Thời lượng:  ", phim.getThoiLuong() + " phút"));
        thongTin.add(Box.createVerticalStrut(6));
        thongTin.add(dongThongTin("Đạo diễn:  ",
                phim.getDaoDien() != null && !phim.getDaoDien().isBlank() ? phim.getDaoDien() : "Đang cập nhật"));
        thongTin.add(Box.createVerticalStrut(6));
        thongTin.add(dongThongTin("Giới hạn tuổi:  ",
                phim.getDoTuoi() != null && !phim.getDoTuoi().isBlank() ? phim.getDoTuoi() : "P"));
        thongTin.add(Box.createVerticalStrut(6));
        thongTin.add(dongThongTin("Khởi chiếu:  ",
                phim.getNgayKhoiChieu() != null ? phim.getNgayKhoiChieu().format(NGAY_FMT) : "Đang chiếu"));
        thongTin.add(Box.createVerticalStrut(12));

        JLabel lblMoTa = new JLabel("Nội dung phim");
        lblMoTa.setFont(Theme.FONT_DAM);
        lblMoTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        thongTin.add(lblMoTa);
        thongTin.add(Box.createVerticalStrut(4));

        JTextArea txtMoTa = new JTextArea(
                phim.getMoTa() != null && !phim.getMoTa().isBlank()
                        ? phim.getMoTa()
                        : "Chưa có mô tả chi tiết cho phim này.");
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        txtMoTa.setEditable(false);
        txtMoTa.setBackground(Theme.MAU_NEN);
        txtMoTa.setBorder(Theme.le(8));
        JScrollPane cuonMoTa = new JScrollPane(txtMoTa);
        cuonMoTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        thongTin.add(cuonMoTa);

        goc.add(thongTin, BorderLayout.CENTER);

        // Nut duoi
        JButton btnDong = Theme.nutPhu("Đóng");
        btnDong.addActionListener(e -> dispose());
        JButton btnDatVe = Theme.nutChinh("Chọn suất chiếu  →");
        btnDatVe.addActionListener(e -> {
            chonSuat = true;
            dispose();
        });
        JPanel nut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nut.setOpaque(false);
        nut.add(btnDong);
        nut.add(btnDatVe);
        goc.add(nut, BorderLayout.SOUTH);

        setContentPane(goc);
        getRootPane().setDefaultButton(btnDatVe);
        setSize(new Dimension(640, 440));
        setLocationRelativeTo(owner);
    }

    public boolean isChonSuat() {
        return chonSuat;
    }

    private static JPanel dongThongTin(String nhan, String giaTri) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(Theme.chuPhu(nhan));
        JLabel v = new JLabel(giaTri);
        v.setFont(Theme.FONT_DAM);
        p.add(v);
        return p;
    }

    private static ImageIcon taoPosterLon(Phim phim, ImageIcon posterNho) {
        int w = 185;
        int h = 260;
        if (posterNho != null && posterNho.getIconWidth() > 0) {
            Image scaled = posterNho.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, Theme.MAU_CHINH, w, h, Theme.MAU_CHINH_DAM));
        g2.fillRoundRect(0, 0, w, h, 14, 14);
        g2.setColor(Color.WHITE);
        g2.setFont(Theme.FONT_TIEU_DE_NHO);
        g2.drawString("POSTER", 45, 115);
        g2.dispose();
        return new ImageIcon(img);
    }
}
