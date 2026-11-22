package com.nhom.datve.service;

import com.nhom.datve.model.Ghe;
import com.nhom.datve.model.PhongChieu;
import com.nhom.datve.model.SuatChieu;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Du lieu de ve so do chon ghe cho 1 suat chieu: phong (so hang, so cot), danh sach ghe va
 * cac ghe da co nguoi dat. Man hinh so do ghe chi can goi DatVeService.laySoDoGhe(maSuat).
 */
public class SoDoGhe {
    private final SuatChieu suatChieu;
    private final PhongChieu phong;
    private final List<Ghe> dsGhe;
    private final Set<Integer> maGheDaDat;

    public SoDoGhe(SuatChieu suatChieu, PhongChieu phong, List<Ghe> dsGhe, Set<Integer> maGheDaDat) {
        this.suatChieu = suatChieu;
        this.phong = phong;
        this.dsGhe = List.copyOf(dsGhe);
        this.maGheDaDat = Set.copyOf(maGheDaDat);
    }

    public boolean isDaDat(Ghe ghe) { return maGheDaDat.contains(ghe.getMaGhe()); }

    /** Tim ghe theo vi tri, vd timGhe('C', 7). Rong neu o do la loi di. */
    public Optional<Ghe> timGhe(char hang, int soGhe) {
        return dsGhe.stream().filter(g -> g.getHang() == hang && g.getSoGhe() == soGhe).findFirst();
    }

    public int soGheTrong() { return dsGhe.size() - maGheDaDat.size(); }

    public SuatChieu getSuatChieu() { return suatChieu; }
    public PhongChieu getPhong() { return phong; }
    public List<Ghe> getDsGhe() { return dsGhe; }
    public Set<Integer> getMaGheDaDat() { return maGheDaDat; }
}
