package com.nhom.datve.service;

import com.nhom.datve.dao.DAOFactory;
import com.nhom.datve.dao.DatVeDAO;
import com.nhom.datve.dao.GheDAO;
import com.nhom.datve.dao.PhongChieuDAO;
import com.nhom.datve.dao.SuatChieuDAO;
import com.nhom.datve.dao.VeDAO;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.DatVe;
import com.nhom.datve.model.Ghe;
import com.nhom.datve.model.PhongChieu;
import com.nhom.datve.model.PhuongThucThanhToan;
import com.nhom.datve.model.SuatChieu;
import com.nhom.datve.model.TrangThaiDatVe;
import com.nhom.datve.model.Ve;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Nghiep vu dat ve / huy ve. UI chi goi lop nay, khong goi DAO truc tiep.
 *
 * Kiem tra 2 lop:
 *  1. Tai day (Java): bao loi som, ro rang cho nguoi dung truoc khi gui xuong CSDL.
 *  2. Trong CSDL (sp_DatVe co transaction + chi muc UNIQUE + trigger): chan truong hop 2 nguoi
 *     cung dat 1 ghe gan nhu cung luc - Java khong tu dam bao duoc dieu nay.
 */
public class DatVeService {
    public static final int SO_GHE_TOI_DA = 8;
    /** Chi duoc huy truoc gio chieu it nhat 2 tieng. */
    public static final Duration HAN_HUY_TRUOC = Duration.ofHours(2);

    private final SuatChieuDAO suatChieuDAO;
    private final PhongChieuDAO phongChieuDAO;
    private final GheDAO gheDAO;
    private final VeDAO veDAO;
    private final DatVeDAO datVeDAO;
    private final Clock clock;

    /** Dung trong ung dung: lay DAO tu DAOFactory (JDBC hoac mock tuy cau hinh). */
    public DatVeService() {
        this(DAOFactory.suatChieu(), DAOFactory.phongChieu(), DAOFactory.ghe(),
                DAOFactory.ve(), DAOFactory.datVe(), Clock.systemDefaultZone());
    }

    /** Dung trong test: truyen DAO va dong ho tu ngoai vao (dependency injection). */
    public DatVeService(SuatChieuDAO suatChieuDAO, PhongChieuDAO phongChieuDAO, GheDAO gheDAO,
                        VeDAO veDAO, DatVeDAO datVeDAO, Clock clock) {
        this.suatChieuDAO = suatChieuDAO;
        this.phongChieuDAO = phongChieuDAO;
        this.gheDAO = gheDAO;
        this.veDAO = veDAO;
        this.datVeDAO = datVeDAO;
        this.clock = clock;
    }

    /** Lay du lieu cho man hinh so do ghe. */
    public SoDoGhe laySoDoGhe(int maSuat) throws NghiepVuException {
        SuatChieu suat = laySuat(maSuat);
        PhongChieu phong = phongChieuDAO.findById(suat.getMaPhong())
                .orElseThrow(() -> new NghiepVuException("Không tìm thấy phòng chiếu của suất này."));
        return new SoDoGhe(suat, phong, gheDAO.findByPhong(phong.getMaPhong()), veDAO.findMaGheDaDat(maSuat));
    }

    /** Tong tien tam tinh de hien truoc khi khach bam Thanh toan. */
    public BigDecimal tinhTongTien(SuatChieu suat, List<Ghe> gheChon) {
        return gheChon.stream().map(g -> g.tinhGiaVe(suat.getGiaCoBan()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Dat ve va thanh toan (gia lap).
     * @return lan dat vua tao, kem danh sach ve
     */
    public DatVe datVe(int maKH, int maSuat, List<Ghe> gheChon, PhuongThucThanhToan phuongThuc)
            throws NghiepVuException {
        if (gheChon == null || gheChon.isEmpty()) {
            throw new NghiepVuException("Vui lòng chọn ít nhất 1 ghế.");
        }
        if (gheChon.size() > SO_GHE_TOI_DA) {
            throw new NghiepVuException("Mỗi lần đặt tối đa " + SO_GHE_TOI_DA + " ghế.");
        }
        if (phuongThuc == null) {
            throw new NghiepVuException("Vui lòng chọn phương thức thanh toán.");
        }
        SuatChieu suat = laySuat(maSuat);
        if (!suat.getGioBatDau().isAfter(LocalDateTime.now(clock))) {
            throw new NghiepVuException("Suất chiếu đã bắt đầu, không thể đặt vé.");
        }
        Set<Integer> daChon = new HashSet<>();
        Set<Integer> daDat = veDAO.findMaGheDaDat(maSuat);
        for (Ghe g : gheChon) {
            if (g.getMaPhong() != suat.getMaPhong()) {
                throw new NghiepVuException("Ghế " + g.getTenGhe() + " không thuộc phòng của suất chiếu này.");
            }
            if (!daChon.add(g.getMaGhe())) {
                throw new NghiepVuException("Ghế " + g.getTenGhe() + " bị chọn trùng.");
            }
            if (daDat.contains(g.getMaGhe())) {
                throw new NghiepVuException("Ghế " + g.getTenGhe() + " đã có người đặt.");
            }
        }
        List<Integer> dsMaGhe = gheChon.stream().map(Ghe::getMaGhe).toList();
        int maDatVe = datVeDAO.datVe(maKH, maSuat, dsMaGhe, phuongThuc);
        return datVeDAO.findById(maDatVe)
                .orElseThrow(() -> new NghiepVuException("Đặt vé xong nhưng không đọc lại được thông tin vé."));
    }

    /** Huy 1 lan dat ve cua chinh khach hang do. */
    public void huyVe(int maKH, int maDatVe) throws NghiepVuException {
        DatVe dv = datVeDAO.findById(maDatVe)
                .orElseThrow(() -> new NghiepVuException("Không tìm thấy vé cần hủy."));
        if (dv.getMaKH() != maKH) {
            throw new NghiepVuException("Bạn không thể hủy vé của người khác.");
        }
        if (dv.getTrangThai() != TrangThaiDatVe.DA_THANH_TOAN) {
            throw new NghiepVuException("Vé đang ở trạng thái \"" + dv.getTrangThai().getMoTa() + "\", không thể hủy.");
        }
        LocalDateTime gioChieuSomNhat = dv.getVes().stream().map(Ve::getGioBatDau)
                .filter(Objects::nonNull).min(LocalDateTime::compareTo)
                .orElseThrow(() -> new NghiepVuException("Lần đặt này không có vé."));
        if (LocalDateTime.now(clock).plus(HAN_HUY_TRUOC).isAfter(gioChieuSomNhat)) {
            throw new NghiepVuException("Chỉ được hủy vé trước giờ chiếu ít nhất "
                    + HAN_HUY_TRUOC.toHours() + " tiếng.");
        }
        if (!datVeDAO.huy(maDatVe)) {
            throw new NghiepVuException("Hủy vé không thành công, vui lòng thử lại.");
        }
    }

    public List<DatVe> lichSuDatVe(int maKH) {
        return datVeDAO.findByKhachHang(maKH);
    }

    private SuatChieu laySuat(int maSuat) throws NghiepVuException {
        return suatChieuDAO.findById(maSuat)
                .orElseThrow(() -> new NghiepVuException("Suất chiếu không tồn tại."));
    }
}
