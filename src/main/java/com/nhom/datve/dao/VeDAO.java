package com.nhom.datve.dao;

import com.nhom.datve.model.Ve;
import java.util.List;
import java.util.Set;

public interface VeDAO {
    /** MaGhe cua cac ve con hieu luc trong suat chieu (de to mau ghe da dat tren so do). */
    Set<Integer> findMaGheDaDat(int maSuat);

    List<Ve> findByDatVe(int maDatVe);
}
