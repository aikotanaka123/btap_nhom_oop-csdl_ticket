package com.nhom.datve.dao;

import com.nhom.datve.model.BoLocTimKiem;
import com.nhom.datve.model.KetQuaTimKiem;

/** Tim suat chieu sap toi theo bo loc. */
public interface TimKiemDAO {
    /**
     * @param anToan true = PreparedStatement; false = noi chuoi (demo SQL injection)
     */
    KetQuaTimKiem timSuatChieu(BoLocTimKiem boLoc, boolean anToan);
}
