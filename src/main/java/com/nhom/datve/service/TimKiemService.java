package com.nhom.datve.service;

import com.nhom.datve.dao.DAOFactory;
import com.nhom.datve.dao.TheLoaiDAO;
import com.nhom.datve.dao.TimKiemDAO;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.BoLocTimKiem;
import com.nhom.datve.model.KetQuaTimKiem;
import com.nhom.datve.model.TheLoai;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/** Nghiep vu man hinh tim kiem phim. */
public class TimKiemService {
    public static final int DO_DAI_TU_KHOA_TOI_DA = 100;

    private final TimKiemDAO timKiemDAO;
    private final TheLoaiDAO theLoaiDAO;

    public TimKiemService() {
        this(DAOFactory.timKiem(), DAOFactory.theLoai());
    }

    public TimKiemService(TimKiemDAO timKiemDAO, TheLoaiDAO theLoaiDAO) {
        this.timKiemDAO = timKiemDAO;
        this.theLoaiDAO = theLoaiDAO;
    }

    public List<TheLoai> dsTheLoai() {
        return theLoaiDAO.findAll().stream().sorted(Comparator.comparing(TheLoai::getTenTL)).toList();
    }

    /**
     * @param anToan false chi dung khi demo chuong 10 (noi chuoi, bi SQL injection)
     */
    public KetQuaTimKiem tim(BoLocTimKiem loc, boolean anToan) throws NghiepVuException {
        if (loc.coTuKhoa() && loc.tuKhoa().length() > DO_DAI_TU_KHOA_TOI_DA) {
            throw new NghiepVuException("Từ khóa tối đa " + DO_DAI_TU_KHOA_TOI_DA + " ký tự.");
        }
        if (loc.giaToiDa() != null && loc.giaToiDa().compareTo(BigDecimal.ZERO) < 0) {
            throw new NghiepVuException("Giá tối đa không được âm.");
        }
        return timKiemDAO.timSuatChieu(loc, anToan);
    }
}
