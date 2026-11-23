USE DatVeXemPhim;
GO

-- =========================================================================
-- PHẦN A: CÁC VIEW (KHUNG NHÌN) TỔNG HỢP DỮ LIỆU
-- =========================================================================

-- 1. (VIEW): Danh sách phim kèm các thể loại (JOIN nhiều bảng + STRING_AGG)
CREATE OR ALTER VIEW dbo.vw_Phim_TheLoai
AS
SELECT
    p.MaPhim,
    p.TenPhim,
    p.ThoiLuong,
    p.DaoDien,
    p.NgayKhoiChieu,
    p.DoTuoi,
    p.TrangThai,
    ISNULL(STRING_AGG(tl.TenTL, N', '), N'Chưa phân loại') AS DanhSachTheLoai
FROM Phim p
LEFT JOIN Phim_TheLoai pt ON p.MaPhim = pt.MaPhim
LEFT JOIN TheLoai tl ON pt.MaTL = tl.MaTL
GROUP BY
    p.MaPhim, p.TenPhim, p.ThoiLuong, p.DaoDien,
    p.NgayKhoiChieu, p.DoTuoi, p.TrangThai;
GO

-- 2. (VIEW): Chi tiết suất chiếu, phòng chiếu, số vé bán & tỷ lệ lấp đầy
CREATE OR ALTER VIEW dbo.vw_SuatChieu_TongQuan
AS
SELECT
    sc.MaSuat,
    p.TenPhim,
    p.DoTuoi,
    pc.MaPhong,
    pc.TenPhong,
    sc.GioBatDau,
    sc.GioKetThuc,
    sc.GiaCoBan,
    COUNT(DISTINCT v.MaVe) AS SoVeDaBan,
    (SELECT COUNT(*) FROM Ghe g WHERE g.MaPhong = pc.MaPhong) AS TongSoGhe,
    CAST(
        100.0 * COUNT(DISTINCT v.MaVe) /
        NULLIF((SELECT COUNT(*) FROM Ghe g WHERE g.MaPhong = pc.MaPhong), 0)
        AS DECIMAL(5, 2)
    ) AS TyLeLapDayPhanTram
FROM SuatChieu sc
JOIN Phim p ON sc.MaPhim = p.MaPhim
JOIN PhongChieu pc ON sc.MaPhong = pc.MaPhong
LEFT JOIN Ve v ON sc.MaSuat = v.MaSuat AND v.TrangThai <> 'DA_HUY'
LEFT JOIN DatVe dv ON v.MaDatVe = dv.MaDatVe AND dv.TrangThai = 'DA_THANH_TOAN'
GROUP BY
    sc.MaSuat, p.TenPhim, p.DoTuoi, pc.MaPhong, pc.TenPhong,
    sc.GioBatDau, sc.GioKetThuc, sc.GiaCoBan;
GO

-- =========================================================================
-- PHẦN B: CÁC TRUY VẤN JOIN, GROUP BY / HAVING & HÀM/THỦ TỤC
-- =========================================================================

-- 3. (Hàm fn_DoanhThu): Tổng doanh thu thực thu 30 ngày gần nhất
SELECT
    dbo.fn_DoanhThu(DATEADD(DAY, -30, CAST(GETDATE() AS DATE)), CAST(GETDATE() AS DATE)) AS TongDoanhThu30Ngay;
GO

-- 4. (Truy vấn VIEW 1): Xem toàn bộ phim kèm thể loại
SELECT *
FROM dbo.vw_Phim_TheLoai
ORDER BY MaPhim;
GO

-- 5. (Truy vấn VIEW 2): Lịch suất chiếu và tỷ lệ lấp đầy ghế
SELECT *
FROM dbo.vw_SuatChieu_TongQuan
ORDER BY GioBatDau;
GO

-- 6. (LEFT JOIN + GROUP BY): Thống kê doanh thu & số vé bán theo từng phim
SELECT
    p.MaPhim,
    p.TenPhim,
    COUNT(v.MaVe) AS TongSoVeBan,
    ISNULL(SUM(CASE WHEN dv.TrangThai = 'DA_THANH_TOAN' AND v.TrangThai <> 'DA_HUY' THEN v.GiaVe ELSE 0 END), 0) AS DoanhThuPhim
FROM Phim p
LEFT JOIN SuatChieu sc ON p.MaPhim = sc.MaPhim
LEFT JOIN Ve v ON sc.MaSuat = v.MaSuat
LEFT JOIN DatVe dv ON v.MaDatVe = dv.MaDatVe
GROUP BY p.MaPhim, p.TenPhim
ORDER BY DoanhThuPhim DESC, p.MaPhim;
GO

-- 7. (GROUP BY theo ngày): Doanh thu thực thu theo từng ngày thanh toán
SELECT
    CAST(NgayThanhToan AS DATE) AS NgayThanhToan,
    COUNT(*) AS SoGiaoDichThanhCong,
    SUM(SoTien) AS DoanhThuNgay
FROM ThanhToan
WHERE TrangThai = 'THANH_CONG'
GROUP BY CAST(NgayThanhToan AS DATE)
ORDER BY NgayThanhToan;
GO

-- 8. (GROUP BY + HAVING): Các thể loại phim có từ 2 phim trở lên
SELECT
    tl.MaTL,
    tl.TenTL,
    COUNT(pt.MaPhim) AS SoLuongPhim
FROM TheLoai tl
JOIN Phim_TheLoai pt ON tl.MaTL = pt.MaTL
GROUP BY tl.MaTL, tl.TenTL
HAVING COUNT(pt.MaPhim) >= 2
ORDER BY SoLuongPhim DESC, tl.TenTL;
GO

-- 9. (JOIN 3 bảng + GROUP BY + HAVING): Khách hàng có tổng chi tiêu >= 150.000đ
SELECT
    kh.MaKH,
    kh.HoTen,
    kh.SoDienThoai,
    kh.DiemTichLuy,
    COUNT(DISTINCT dv.MaDatVe) AS SoDonDatVe,
    SUM(dv.TongTien) AS TongChiTieu
FROM KhachHang kh
JOIN DatVe dv ON kh.MaKH = dv.MaKH
WHERE dv.TrangThai = 'DA_THANH_TOAN'
GROUP BY kh.MaKH, kh.HoTen, kh.SoDienThoai, kh.DiemTichLuy
HAVING SUM(dv.TongTien) >= 150000
ORDER BY TongChiTieu DESC;
GO

-- 10. (INNER JOIN nhiều bảng): Chi tiết từng vé đã bán (phim, phòng, ghế, giá vé)
SELECT
    v.MaVe,
    dv.MaDatVe,
    p.TenPhim,
    pc.TenPhong,
    (g.Hang + CAST(g.SoGhe AS VARCHAR(5))) AS MaViTriGhe,
    lg.TenLoai AS LoaiGhe,
    v.GiaVe,
    dv.NgayDat,
    tt.PhuongThuc
FROM Ve v
JOIN DatVe dv ON v.MaDatVe = dv.MaDatVe
JOIN SuatChieu sc ON v.MaSuat = sc.MaSuat
JOIN Phim p ON sc.MaPhim = p.MaPhim
JOIN PhongChieu pc ON sc.MaPhong = pc.MaPhong
JOIN Ghe g ON v.MaGhe = g.MaGhe
JOIN LoaiGhe lg ON g.MaLoaiGhe = lg.MaLoaiGhe
LEFT JOIN ThanhToan tt ON dv.MaDatVe = tt.MaDatVe
WHERE v.TrangThai <> 'DA_HUY'
ORDER BY v.MaVe;
GO

-- 11. (GROUP BY theo loại ghế): Thống kê số lượng vé bán và doanh thu theo loại ghế
SELECT
    lg.TenLoai AS LoaiGhe,
    lg.HeSoGia,
    COUNT(v.MaVe) AS SoVeDaBan,
    ISNULL(SUM(v.GiaVe), 0) AS TongTienTheoLoaiGhe
FROM LoaiGhe lg
LEFT JOIN Ghe g ON lg.MaLoaiGhe = g.MaLoaiGhe
LEFT JOIN Ve v ON g.MaGhe = v.MaGhe AND v.TrangThai <> 'DA_HUY'
GROUP BY lg.MaLoaiGhe, lg.TenLoai, lg.HeSoGia
ORDER BY TongTienTheoLoaiGhe DESC;
GO

-- =========================================================================
-- PHẦN C: CÁC TRUY VẤN LỒNG (SUBQUERY) & KIỂM TRA DỮ LIỆU CHƯA ĐẶT
-- =========================================================================

-- 12. (Truy vấn lồng NOT IN): Các ghế còn trống của suất chiếu có MaSuat = 1
SELECT
    g.MaGhe,
    (g.Hang + CAST(g.SoGhe AS VARCHAR(5))) AS SoGheHienThi,
    lg.TenLoai AS LoaiGhe,
    ROUND(sc.GiaCoBan * lg.HeSoGia, 0) AS GiaDuKien
FROM Ghe g
JOIN SuatChieu sc ON g.MaPhong = sc.MaPhong
JOIN LoaiGhe lg ON g.MaLoaiGhe = lg.MaLoaiGhe
WHERE sc.MaSuat = 1
  AND g.MaGhe NOT IN (
      SELECT v.MaGhe
      FROM Ve v
      WHERE v.MaSuat = 1
        AND v.TrangThai <> 'DA_HUY'
  )
ORDER BY g.Hang, g.SoGhe;
GO

-- 13. (Truy vấn lồng NOT EXISTS): Danh sách phim chưa từng có suất chiếu nào
SELECT
    p.MaPhim,
    p.TenPhim,
    p.TrangThai
FROM Phim p
WHERE NOT EXISTS (
    SELECT 1
    FROM SuatChieu sc
    WHERE sc.MaPhim = p.MaPhim
);
GO

-- 14. (Truy vấn lồng vô hướng): Các đơn đặt vé có tổng tiền cao hơn mức trung bình
SELECT
    dv.MaDatVe,
    kh.HoTen AS TenKhachHang,
    dv.NgayDat,
    dv.TongTien,
    dv.TrangThai
FROM DatVe dv
LEFT JOIN KhachHang kh ON dv.MaKH = kh.MaKH
WHERE dv.TongTien > (
    SELECT AVG(TongTien)
    FROM DatVe
    WHERE TrangThai = 'DA_THANH_TOAN'
);
GO

-- 15. (Truy vấn lồng với IN & tổng hợp): Các suất chiếu thuộc các phim đang chiếu và có doanh thu
SELECT
    sc.MaSuat,
    p.TenPhim,
    pc.TenPhong,
    sc.GioBatDau,
    (
        SELECT ISNULL(SUM(v.GiaVe), 0)
        FROM Ve v
        WHERE v.MaSuat = sc.MaSuat
          AND v.TrangThai <> 'DA_HUY'
    ) AS DoanhThuSuatChieu
FROM SuatChieu sc
JOIN Phim p ON sc.MaPhim = p.MaPhim
JOIN PhongChieu pc ON sc.MaPhong = pc.MaPhong
WHERE p.MaPhim IN (
    SELECT MaPhim
    FROM Phim
    WHERE TrangThai = 'DANG_CHIEU'
)
ORDER BY DoanhThuSuatChieu DESC;
GO