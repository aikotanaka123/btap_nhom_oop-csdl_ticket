package com.nhom.datve.dao;

import com.nhom.datve.util.AppConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Noi duy nhat tao doi tuong DAO. Service va UI chi lam viec voi INTERFACE, khong new ...Impl truc tiep.
 *
 * Quy uoc ten lop:
 *  - Ban JDBC: com.nhom.datve.dao.impl.[TenInterface]Impl   (vd PhimDAOImpl)
 *  - Ban gia : com.nhom.datve.dao.mock.Mock[TenInterface]   (vd MockPhimDAO)
 *
 * Che do jdbc ma lop Impl chua ton tai (ban chua code xong) -> tu dong dung ban mock va in canh bao,
 * nho vay ca nhom chay app duoc trong khi tung DAO dang lam dan.
 */
public final class DAOFactory {
    private static final Map<Class<?>, Object> CACHE = new ConcurrentHashMap<>();

    private DAOFactory() { }

    public static TaiKhoanDAO taiKhoan() { return get(TaiKhoanDAO.class); }
    public static KhachHangDAO khachHang() { return get(KhachHangDAO.class); }
    public static NhanVienDAO nhanVien() { return get(NhanVienDAO.class); }
    public static TheLoaiDAO theLoai() { return get(TheLoaiDAO.class); }
    public static PhimDAO phim() { return get(PhimDAO.class); }
    public static PhongChieuDAO phongChieu() { return get(PhongChieuDAO.class); }
    public static LoaiGheDAO loaiGhe() { return get(LoaiGheDAO.class); }
    public static GheDAO ghe() { return get(GheDAO.class); }
    public static SuatChieuDAO suatChieu() { return get(SuatChieuDAO.class); }
    public static VeDAO ve() { return get(VeDAO.class); }
    public static DatVeDAO datVe() { return get(DatVeDAO.class); }
    public static ThongKeDAO thongKe() { return get(ThongKeDAO.class); }
    public static TimKiemDAO timKiem() { return get(TimKiemDAO.class); }

    /** Xoa cache (dung khi doi che do mock/jdbc trong luc chay hoac trong test). */
    public static void reset() { CACHE.clear(); }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> type) {
        return (T) CACHE.computeIfAbsent(type, DAOFactory::create);
    }

    private static Object create(Class<?> type) {
        String ten = type.getSimpleName();
        String lopMock = "com.nhom.datve.dao.mock.Mock" + ten;
        if (!AppConfig.isMockMode()) {
            String lopImpl = "com.nhom.datve.dao.impl." + ten + "Impl";
            try {
                return newInstance(lopImpl);
            } catch (ClassNotFoundException e) {
                System.err.println("[DAOFactory] Chua co " + lopImpl + " -> tam dung du lieu gia.");
            }
        }
        try {
            return newInstance(lopMock);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Khong tim thay ca ban JDBC lan ban mock cua " + ten, e);
        }
    }

    private static Object newInstance(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Khong tao duoc " + className, e);
        }
    }
}
