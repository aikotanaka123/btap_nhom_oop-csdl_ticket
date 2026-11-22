package com.nhom.datve.ui;

import com.nhom.datve.model.VaiTro;

/**
 * Danh sach man hinh gan vao MainFrame. MainFrame tao man hinh theo TEN LOP o day
 * (constructor nhan DieuHuong, hoac khong tham so). Lop chua co -> hien man hinh "dang lam".
 * Nho vay moi nguoi them man hinh cua minh ma khong phai sua MainFrame.
 *
 * Tham so khi chuyen man hinh (DieuHuong.chuyenDen):
 *   CHON_SUAT_CHIEU -> Integer maPhim
 *   SO_DO_GHE       -> Integer maSuat
 *   cac man hinh khac -> null
 */
public enum ManHinh {
    DANH_SACH_PHIM("Phim đang chiếu", "khachhang.DanhSachPhimPanel", "Trung", VaiTro.KHACHHANG),
    TIM_KIEM_PHIM("Tìm kiếm phim", "khachhang.TimKiemPhimPanel", "Khuê", VaiTro.KHACHHANG),
    CHON_SUAT_CHIEU("Chọn suất chiếu", "khachhang.ChonSuatChieuPanel", "Trung", VaiTro.KHACHHANG),
    SO_DO_GHE("Chọn ghế", "khachhang.SoDoGhePanel", "Trung", VaiTro.KHACHHANG),
    LICH_SU_VE("Vé của tôi", "khachhang.LichSuVePanel", "Trung", VaiTro.KHACHHANG),
    QL_PHIM("Quản lý phim", "admin.QuanLyPhimPanel", "Khánh", VaiTro.NHANVIEN),
    QL_PHONG("Phòng chiếu và ghế", "admin.QuanLyPhongPanel", "Khánh", VaiTro.NHANVIEN),
    QL_SUAT_CHIEU("Lịch suất chiếu", "admin.QuanLySuatChieuPanel", "Khánh", VaiTro.NHANVIEN),
    THONG_KE("Thống kê doanh thu", "admin.ThongKePanel", "Khánh", VaiTro.NHANVIEN);

    private final String tieuDe;
    private final String tenLop;
    private final String nguoiLam;
    private final VaiTro vaiTro;

    ManHinh(String tieuDe, String lopTuongDoi, String nguoiLam, VaiTro vaiTro) {
        this.tieuDe = tieuDe;
        this.tenLop = "com.nhom.datve.ui." + lopTuongDoi;
        this.nguoiLam = nguoiLam;
        this.vaiTro = vaiTro;
    }

    public String getTieuDe() { return tieuDe; }
    public String getTenLop() { return tenLop; }
    public String getNguoiLam() { return nguoiLam; }
    public VaiTro getVaiTro() { return vaiTro; }
}
