package com.nhom.datve.service;

import com.nhom.datve.dao.DAOFactory;
import com.nhom.datve.dao.PhimDAO;
import com.nhom.datve.dao.SuatChieuDAO;
import com.nhom.datve.dao.TheLoaiDAO;
import com.nhom.datve.exception.NghiepVuException;
import com.nhom.datve.model.Phim;
import com.nhom.datve.model.SuatChieu;
import com.nhom.datve.model.TheLoai;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Nghiep vu tra cuu phim, the loai va suat chieu cho man hinh khach hang.
 */
public class PhimService {
    private final PhimDAO phimDAO;
    private final TheLoaiDAO theLoaiDAO;
    private final SuatChieuDAO suatChieuDAO;

    public PhimService() {
        this(DAOFactory.phim(), DAOFactory.theLoai(), DAOFactory.suatChieu());
    }

    public PhimService(PhimDAO phimDAO, TheLoaiDAO theLoaiDAO, SuatChieuDAO suatChieuDAO) {
        this.phimDAO = phimDAO;
        this.theLoaiDAO = theLoaiDAO;
        this.suatChieuDAO = suatChieuDAO;
    }

    public List<TheLoai> layTatCaTheLoai() {
        return theLoaiDAO.findAll().stream()
                .sorted(Comparator.comparing(TheLoai::getTenTL))
                .toList();
    }

    public List<Phim> layPhimDangChieu(String tuKhoa, Integer maTL) {
        String kw = (tuKhoa == null || tuKhoa.isBlank()) ? null : tuKhoa.trim();
        if (kw == null && maTL == null) {
            return phimDAO.findDangChieu();
        }
        return phimDAO.search(kw, maTL).stream()
                .filter(p -> Phim.DANG_CHIEU.equals(p.getTrangThai()))
                .toList();
    }

    public Phim layPhimTheoMa(int maPhim) throws NghiepVuException {
        return phimDAO.findById(maPhim)
                .orElseThrow(() -> new NghiepVuException("Không tìm thấy phim mã #" + maPhim + "."));
    }

    public List<SuatChieu> laySuatSapChieu(int maPhim) {
        return suatChieuDAO.findSapChieu(maPhim, LocalDateTime.now());
    }

    public List<SuatChieu> laySuatTheoNgay(int maPhim, LocalDate ngay) {
        return suatChieuDAO.findByPhimVaNgay(maPhim, ngay);
    }
}
