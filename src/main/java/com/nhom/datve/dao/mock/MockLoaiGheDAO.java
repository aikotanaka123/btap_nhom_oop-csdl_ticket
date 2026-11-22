package com.nhom.datve.dao.mock;

import com.nhom.datve.dao.LoaiGheDAO;
import com.nhom.datve.model.LoaiGhe;

/** DAO gia cho LoaiGhe (du lieu trong MockData). */
public class MockLoaiGheDAO extends InMemoryDAO<LoaiGhe> implements LoaiGheDAO {

    public MockLoaiGheDAO() {
        super(MockData.LOAI_GHE, LoaiGhe::getMaLoaiGhe, LoaiGhe::setMaLoaiGhe);
    }
}
