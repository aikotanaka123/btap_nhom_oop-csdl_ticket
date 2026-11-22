package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.GheDAO;
import com.nhom.datve.model.Ghe;
import java.util.Comparator;
import java.util.List;

/** DAO gia cho Ghe (du lieu trong MockData). */
public class MockGheDAO extends InMemoryDAO<Ghe> implements GheDAO {

    public MockGheDAO() {
        super(MockData.GHE, Ghe::getMaGhe, Ghe::setMaGhe);
    }

    @Override
    public List<Ghe> findByPhong(int maPhong) {
        return store.values().stream().filter(g -> g.getMaPhong() == maPhong)
                .sorted(Comparator.comparing(Ghe::getHang).thenComparing(Ghe::getSoGhe)).toList();
    }

    @Override
    public int insertBatch(List<Ghe> dsGhe) {
        dsGhe.forEach(this::insert);
        return dsGhe.size();
    }
}
