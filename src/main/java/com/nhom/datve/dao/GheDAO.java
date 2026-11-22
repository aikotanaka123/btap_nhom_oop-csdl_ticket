package com.nhom.datve.dao;

import com.nhom.datve.model.Ghe;
import java.util.List;

public interface GheDAO extends GenericDAO<Ghe, Integer> {
    /** Toan bo ghe cua 1 phong (kem LoaiGhe), sap xep theo Hang, SoGhe de ve so do. */
    List<Ghe> findByPhong(int maPhong);

    /** Sinh hang loat ghe khi tao phong moi (dung batch insert). */
    int insertBatch(List<Ghe> dsGhe);
}
