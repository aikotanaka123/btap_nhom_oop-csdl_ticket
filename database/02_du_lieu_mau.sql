USE DatVeXemPhim;
GO

-- 1. Vai trò
INSERT INTO VaiTro (TenVaiTro) VALUES
(N'ADMIN'),
(N'NHANVIEN'),
(N'KHACHHANG');
GO

-- 2. Tài khoản (admin / admin123, còn lại / 123456 - mật khẩu băm SHA-256)
INSERT INTO TaiKhoan (TenDangNhap, MatKhau, HoTen, Email, SoDienThoai, MaVaiTro, VaiTro, TrangThai) VALUES
('admin',     '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', N'Quản trị viên Hệ thống', 'admin@cinema.vn',     '0901000001', 1, 'NHANVIEN', 1),
('nhanvien1', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Nguyễn Minh Anh',        'nv1@cinema.vn',       '0901000002', 2, 'NHANVIEN', 1),
('khach1',    '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Trần Hoàng Nam',         'nam.tran@gmail.com',  '0901000003', 3, 'KHACHHANG', 1),
('khach2',    '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Lê Thu Hà',              'ha.le@gmail.com',     '0901000004', 3, 'KHACHHANG', 1);
GO

-- 3. Khách hàng
INSERT INTO KhachHang (MaTK, HoTen, SoDienThoai, Email, NgaySinh, DiemTichLuy) VALUES
(3, N'Trần Hoàng Nam', '0901000003', 'nam.tran@gmail.com', '1998-05-15', 120),
(4, N'Lê Thu Hà',      '0901000004', 'ha.le@gmail.com',    '2001-10-20', 80);
GO

-- 3b. Nhân viên
INSERT INTO NhanVien (MaTK, HoTen, ChucVu, SoDienThoai) VALUES
(1, N'Quản trị viên Hệ thống', N'Quản lý', '0901000001'),
(2, N'Nguyễn Minh Anh',        N'Thu ngân', '0901000002');
GO

-- 4. Thể loại phim
INSERT INTO TheLoai (TenTL) VALUES
(N'Hành động'),
(N'Khoa học viễn tưởng'),
(N'Phiêu lưu'),
(N'Hoạt hình'),
(N'Tâm lý - Tình cảm'),
(N'Kinh dị'),
(N'Hài hước');
GO

-- 5. Danh mục phim
INSERT INTO Phim (TenPhim, ThoiLuong, DaoDien, NgayKhoiChieu, DoTuoi, TrangThai, Poster, MoTa) VALUES
(N'Dune: Hành Tinh Cát - Phần Hai', 166, N'Denis Villeneuve', '2026-11-01', 'T13', 'DANG_CHIEU', 'dune2.jpg',
 N'Paul Atreides hợp sức cùng Chani và người Fremen trên hành trình báo thù những kẻ đã hủy hoại gia tộc.'),
(N'Robot Hoang Dã', 102, N'Chris Sanders', '2026-11-05', 'P', 'DANG_CHIEU', 'wild_robot.jpg',
 N'Hành trình sinh tồn đầy cảm xúc của robot Roz trên một hòn đảo hoang vắng.'),
(N'Lật Mặt 8: Vòng Tay Nắng', 135, N'Lý Hải', '2026-11-10', 'T13', 'DANG_CHIEU', 'latmat8.jpg',
 N'Câu chuyện gia đình miền Trung giàu cảm xúc và những thử thách của thế hệ trẻ.'),
(N'Vùng Đất Câm Lặng: Ngày Một', 99, N'Michael Sarnoski', '2026-11-15', 'T16', 'DANG_CHIEU', 'quiet_place.jpg',
 N'Những giờ phút đầu tiên khi sinh vật săn mồi bằng âm thanh đổ bộ xuống New York.'),
(N'Mufasa: Vua Sư Tử', 118, N'Barry Jenkins', '2026-12-20', 'P', 'SAP_CHIEU', 'mufasa.jpg',
 N'Hành trình trưởng thành của chú sư tử mồ côi Mufasa trước khi trở thành vị vua của Xứ Kiêu Hãnh.');
GO

-- 6. Gán thể loại cho phim
INSERT INTO Phim_TheLoai (MaPhim, MaTL) VALUES
(1, 1), (1, 2), (1, 3),
(2, 3), (2, 4),
(3, 5), (3, 7),
(4, 2), (4, 6),
(5, 3), (5, 4);
GO

-- 7. Loại ghế
INSERT INTO LoaiGhe (TenLoai, HeSoGia) VALUES
(N'Thường', 1.00),
(N'VIP',    1.30),
(N'Đôi',    2.00);
GO

-- 8. Phòng chiếu
INSERT INTO PhongChieu (TenPhong, SoHang, SoCot, HoatDong) VALUES
(N'Phòng 01 - Standard', 6, 10, 1),
(N'Phòng 02 - VIP',      7, 10, 1),
(N'Phòng 03 - IMAX',     8, 12, 1);
GO

-- 9. Tự động sinh sơ đồ ghế cho tất cả các phòng (Hàng cuối ghế Đôi, 2 hàng áp chót ghế VIP, còn lại ghế Thường)
DECLARE @MaPhong INT, @SoHang INT, @SoCot INT, @h INT, @c INT, @MaLoai INT, @SoGheHang INT;
DECLARE curPhong CURSOR FOR SELECT MaPhong, SoHang, SoCot FROM PhongChieu;
OPEN curPhong;
FETCH NEXT FROM curPhong INTO @MaPhong, @SoHang, @SoCot;

WHILE @@FETCH_STATUS = 0
BEGIN
    SET @h = 0;
    WHILE @h < @SoHang
    BEGIN
        IF (@h = @SoHang - 1 AND @SoHang >= 3)
            SET @MaLoai = 3; -- Ghế Đôi
        ELSE IF (@h >= @SoHang - 3 AND @SoHang >= 3)
            SET @MaLoai = 2; -- Ghế VIP
        ELSE
            SET @MaLoai = 1; -- Ghế Thường

        SET @SoGheHang = CASE WHEN @MaLoai = 3 THEN @SoCot / 2 ELSE @SoCot END;
        SET @c = 1;
        WHILE @c <= @SoGheHang
        BEGIN
            INSERT INTO Ghe (MaPhong, MaLoaiGhe, Hang, SoGhe)
            VALUES (@MaPhong, @MaLoai, CHAR(65 + @h), @c);
            SET @c = @c + 1;
        END
        SET @h = @h + 1;
    END
    FETCH NEXT FROM curPhong INTO @MaPhong, @SoHang, @SoCot;
END
CLOSE curPhong;
DEALLOCATE curPhong;
GO

-- 10. Suất chiếu mẫu (hôm nay và ngày mai)
DECLARE @HomNay DATE = CAST(GETDATE() AS DATE);
DECLARE @NgayMai DATE = DATEADD(DAY, 1, @HomNay);

INSERT INTO SuatChieu (MaPhim, MaPhong, GioBatDau, GioKetThuc, GiaCoBan) VALUES
(1, 1, DATEADD(HOUR, 14, CAST(@HomNay AS DATETIME2)), DATEADD(MINUTE, 181, DATEADD(HOUR, 14, CAST(@HomNay AS DATETIME2))), 80000),
(1, 3, DATEADD(HOUR, 19, CAST(@HomNay AS DATETIME2)), DATEADD(MINUTE, 181, DATEADD(HOUR, 19, CAST(@HomNay AS DATETIME2))), 110000),
(2, 2, DATEADD(HOUR, 16, CAST(@HomNay AS DATETIME2)), DATEADD(MINUTE, 117, DATEADD(HOUR, 16, CAST(@HomNay AS DATETIME2))), 75000),
(3, 1, DATEADD(HOUR, 18, CAST(@NgayMai AS DATETIME2)), DATEADD(MINUTE, 150, DATEADD(HOUR, 18, CAST(@NgayMai AS DATETIME2))), 85000),
(4, 2, DATEADD(HOUR, 20, CAST(@NgayMai AS DATETIME2)), DATEADD(MINUTE, 114, DATEADD(HOUR, 20, CAST(@NgayMai AS DATETIME2))), 90000);
GO

-- 11. Dữ liệu đặt vé, vé & thanh toán mẫu để hiển thị trên màn hình Thống kê
INSERT INTO DatVe (MaTK, MaKH, MaSuat, NgayDat, TongTien, TrangThai) VALUES
(3, 1, 1, DATEADD(DAY, -2, SYSDATETIME()), 160000, 'DA_THANH_TOAN'),
(4, 2, 2, DATEADD(DAY, -1, SYSDATETIME()), 286000, 'DA_THANH_TOAN'),
(2, 1, 3, SYSDATETIME(),                   150000, 'DA_THANH_TOAN');
GO

INSERT INTO Ve (MaDatVe, MaSuat, MaGhe, GiaVe, TrangThai) VALUES
(1, 1, 1,   80000,  'HIEU_LUC'),
(1, 1, 2,   80000,  'HIEU_LUC'),
(2, 2, 161, 143000, 'HIEU_LUC'),
(2, 2, 162, 143000, 'HIEU_LUC'),
(3, 3, 61,  75000,  'HIEU_LUC'),
(3, 3, 62,  75000,  'HIEU_LUC');
GO

INSERT INTO ThanhToan (MaDatVe, PhuongThuc, SoTien, NgayThanhToan, TrangThai, MaGiaoDich) VALUES
(1, 'VI_DIEN_TU', 160000, DATEADD(DAY, -2, SYSDATETIME()), 'THANH_CONG', 'GD20261121001'),
(2, 'VI_DIEN_TU', 286000, DATEADD(DAY, -1, SYSDATETIME()), 'THANH_CONG', 'GD20261122002'),
(3, 'TIEN_MAT',   150000, SYSDATETIME(),                   'THANH_CONG', 'GD20261123003');
GO