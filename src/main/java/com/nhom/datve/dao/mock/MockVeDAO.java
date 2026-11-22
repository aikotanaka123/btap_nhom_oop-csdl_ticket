package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.VeDAO;
import com.nhom.datve.model.Ve;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** DAO gia cho Ve. */
public class MockVeDAO implements VeDAO {

    @Override
    public Set<Integer> findMaGheDaDat(int maSuat) {
        synchronized (MockData.class) {
            return MockData.VE.values().stream()
                    .filter(v -> v.getMaSuat() == maSuat && Ve.HIEU_LUC.equals(v.getTrangThai()))
                    .map(Ve::getMaGhe).collect(Collectors.toSet());
        }
    }

    @Override
    public List<Ve> findByDatVe(int maDatVe) {
        return MockData.VE.values().stream().filter(v -> v.getMaDatVe() == maDatVe).toList();
    }
}
