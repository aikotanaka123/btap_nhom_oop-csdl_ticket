package com.nhom.datve.ui;

import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.DatVe;
import com.nhom.datve.model.Ghe;
import com.nhom.datve.service.DatVeService;
import com.nhom.datve.service.SoDoGhe;
import com.nhom.datve.ui.khachhang.XacNhanDatVeDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Hien thay cho man hinh chua co lop (dang duoc thanh vien khac lam) hoac bi loi khi tao.
 * Rieng SO_DO_GHE co nut thu de chay duoc luong dat ve truoc khi SoDoGhePanel xong.
 */
class ManHinhChoPanel extends JPanel implements ManHinhCon {
    private static final long serialVersionUID = 1L;

    private final ManHinh manHinh;
    private final transient DieuHuong dieuHuong;
    private final JLabel lblThamSo = Theme.chuPhu(" ");
    private final JButton btnThu = Theme.nutChinh("Thử đặt 2 ghế trống đầu tiên");
    private Integer maSuat;

    ManHinhChoPanel(ManHinh manHinh, DieuHuong dieuHuong, Throwable loi) {
        super(new GridBagLayout());
        this.manHinh = manHinh;
        this.dieuHuong = dieuHuong;
        setBackground(Theme.MAU_NEN);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(6, 6, 6, 6);

        add(Theme.tieuDe(manHinh.getTieuDe()), c);
        String moTa = loi == null
                ? "Màn hình này do " + manHinh.getNguoiLam() + " phụ trách, chưa có lớp " + manHinh.getTenLop() + "."
                : "Lỗi khi tạo " + manHinh.getTenLop() + ": " + loi;
        JLabel lbl = new JLabel(moTa, SwingConstants.CENTER);
        lbl.setForeground(loi == null ? Theme.MAU_CHU_PHU : Theme.MAU_CHINH);
        add(lbl, c);
        add(lblThamSo, c);
        if (manHinh == ManHinh.SO_DO_GHE) {
            btnThu.addActionListener(e -> thuDatVe());
            btnThu.setEnabled(false);
            add(btnThu, c);
        }
        JButton quayLai = Theme.nutPhu("Quay lại");
        quayLai.addActionListener(e -> dieuHuong.quayLai());
        add(quayLai, c);
    }

    @Override
    public void khiHienThi(Object thamSo) {
        lblThamSo.setText(thamSo == null ? " " : "Tham số nhận được: " + thamSo);
        maSuat = thamSo instanceof Integer i ? i : null;
        btnThu.setEnabled(maSuat != null);
    }

    private void thuDatVe() {
        try {
            DatVeService service = new DatVeService();
            SoDoGhe soDo = service.laySoDoGhe(maSuat);
            List<Ghe> trong = soDo.getDsGhe().stream().filter(g -> !soDo.isDaDat(g)).limit(2).toList();
            Optional<DatVe> kq = XacNhanDatVeDialog.hienThi(dieuHuong.getCuaSo(), dieuHuong.getPhien().getMaKH(),
                    soDo.getSuatChieu(), trong);
            if (kq.isPresent()) {
                dieuHuong.chuyenDen(ManHinh.LICH_SU_VE);
            }
        } catch (NghiepVuException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể đặt vé", JOptionPane.WARNING_MESSAGE);
        }
    }

    ManHinh getManHinh() { return manHinh; }
}
