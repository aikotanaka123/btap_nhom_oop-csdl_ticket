-- =====================================================================
-- TAO TOAN BO CSDL DatVeXemPhim (chay 1 lan trong SSMS hoac sqlcmd)
--   PHAN 1: tao database + bang      (truoc day: 01_tao_bang.sql)
--   PHAN 2: du lieu mau              (truoc day: 02_du_lieu_mau.sql)
--   PHAN 3: procedure, function, trigger (truoc day: 03_procedure_trigger.sql)
-- CANH BAO: chay lai file nay se XOA HET bang va du lieu roi tao lai.
-- =====================================================================

-- #####################################################################
-- PHAN 1: 01_tao_bang.sql
-- #####################################################################

IF DB_ID(N'DatVeXemPhim') IS NULL
BEGIN
    CREATE DATABASE DatVeXemPhim;
END
GO

USE DatVeXemPhim;
GO

-- Xóa bảng cũ theo thứ tự ngược khóa ngoại (nếu chạy lại script từ đầu)
DROP TABLE IF EXISTS ThanhToan;
DROP TABLE IF EXISTS Ve;
DROP TABLE IF EXISTS DatVe;
DROP TABLE IF EXISTS SuatChieu;
DROP TABLE IF EXISTS Ghe;
DROP TABLE IF EXISTS LoaiGhe;
DROP TABLE IF EXISTS PhongChieu;
DROP TABLE IF EXISTS Phim_TheLoai;
DROP TABLE IF EXISTS Phim;
DROP TABLE IF EXISTS TheLoai;
DROP TABLE IF EXISTS NhanVien;
DROP TABLE IF EXISTS KhachHang;
DROP TABLE IF EXISTS TaiKhoan;
DROP TABLE IF EXISTS VaiTro;
GO

-- 1. Bảng VaiTro
CREATE TABLE VaiTro (
    MaVaiTro INT IDENTITY(1,1) PRIMARY KEY,
    TenVaiTro NVARCHAR(50) NOT NULL UNIQUE
);
GO

-- 2. Bảng TaiKhoan
CREATE TABLE TaiKhoan (
    MaTK INT IDENTITY(1,1) PRIMARY KEY,
    TenDangNhap VARCHAR(50) NOT NULL UNIQUE,
    MatKhau VARCHAR(255) NOT NULL,
    HoTen NVARCHAR(100) NOT NULL,
    Email VARCHAR(100) NULL,
    SoDienThoai VARCHAR(15) NULL,
    MaVaiTro INT NOT NULL,
    VaiTro VARCHAR(20) NOT NULL DEFAULT 'KHACHHANG',
    TrangThai BIT NOT NULL DEFAULT 1,
    NgayTao DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_TaiKhoan_VaiTro FOREIGN KEY (MaVaiTro) REFERENCES VaiTro(MaVaiTro)
);
GO

-- 3. Bảng KhachHang
CREATE TABLE KhachHang (
    MaKH INT IDENTITY(1,1) PRIMARY KEY,
    MaTK INT NULL UNIQUE,
    HoTen NVARCHAR(100) NOT NULL,
    SoDienThoai VARCHAR(15) NOT NULL UNIQUE,
    Email VARCHAR(100) NULL,
    NgaySinh DATE NULL,
    DiemTichLuy INT NOT NULL DEFAULT 0 CHECK (DiemTichLuy >= 0),
    CONSTRAINT FK_KhachHang_TaiKhoan FOREIGN KEY (MaTK) REFERENCES TaiKhoan(MaTK)
);
GO

-- 3b. Bảng NhanVien (phục vụ TaiKhoanService & quản trị)
CREATE TABLE NhanVien (
    MaNV INT IDENTITY(1,1) PRIMARY KEY,
    MaTK INT NOT NULL UNIQUE,
    HoTen NVARCHAR(100) NOT NULL,
    ChucVu NVARCHAR(50) NOT NULL DEFAULT N'Nhân viên',
    SoDienThoai VARCHAR(15) NULL,
    CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (MaTK) REFERENCES TaiKhoan(MaTK)
);
GO

-- 4. Bảng TheLoai
CREATE TABLE TheLoai (
    MaTL INT IDENTITY(1,1) PRIMARY KEY,
    TenTL NVARCHAR(80) NOT NULL UNIQUE
);
GO

-- 5. Bảng Phim
CREATE TABLE Phim (
    MaPhim INT IDENTITY(1,1) PRIMARY KEY,
    TenPhim NVARCHAR(200) NOT NULL,
    ThoiLuong INT NOT NULL CHECK (ThoiLuong > 0),
    DaoDien NVARCHAR(120) NOT NULL,
    NgayKhoiChieu DATE NOT NULL,
    DoTuoi VARCHAR(10) NOT NULL DEFAULT 'P' CHECK (DoTuoi IN ('P', 'T13', 'T16', 'T18')),
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'DANG_CHIEU'
        CHECK (TrangThai IN ('DANG_CHIEU', 'SAP_CHIEU', 'NGUNG_CHIEU')),
    Poster NVARCHAR(300) NULL,
    MoTa NVARCHAR(MAX) NULL
);
GO

-- 6. Bảng Phim_TheLoai (Quan hệ N-N giữa Phim và TheLoai)
CREATE TABLE Phim_TheLoai (
    MaPhim INT NOT NULL,
    MaTL INT NOT NULL,
    PRIMARY KEY (MaPhim, MaTL),
    CONSTRAINT FK_PhimTheLoai_Phim FOREIGN KEY (MaPhim) REFERENCES Phim(MaPhim) ON DELETE CASCADE,
    CONSTRAINT FK_PhimTheLoai_TheLoai FOREIGN KEY (MaTL) REFERENCES TheLoai(MaTL) ON DELETE CASCADE
);
GO

-- 7. Bảng PhongChieu
CREATE TABLE PhongChieu (
    MaPhong INT IDENTITY(1,1) PRIMARY KEY,
    TenPhong NVARCHAR(80) NOT NULL UNIQUE,
    SoHang INT NOT NULL CHECK (SoHang BETWEEN 1 AND 26),
    SoCot INT NOT NULL CHECK (SoCot BETWEEN 1 AND 30),
    HoatDong BIT NOT NULL DEFAULT 1
);
GO

-- 8. Bảng LoaiGhe
CREATE TABLE LoaiGhe (
    MaLoaiGhe INT IDENTITY(1,1) PRIMARY KEY,
    TenLoai NVARCHAR(50) NOT NULL UNIQUE,
    HeSoGia DECIMAL(4,2) NOT NULL CHECK (HeSoGia > 0)
);
GO

-- 9. Bảng Ghe
CREATE TABLE Ghe (
    MaGhe INT IDENTITY(1,1) PRIMARY KEY,
    MaPhong INT NOT NULL,
    MaLoaiGhe INT NOT NULL,
    Hang CHAR(1) NOT NULL,
    SoGhe INT NOT NULL CHECK (SoGhe > 0),
    CONSTRAINT UQ_Ghe_Phong_ViTri UNIQUE (MaPhong, Hang, SoGhe),
    CONSTRAINT FK_Ghe_PhongChieu FOREIGN KEY (MaPhong) REFERENCES PhongChieu(MaPhong) ON DELETE CASCADE,
    CONSTRAINT FK_Ghe_LoaiGhe FOREIGN KEY (MaLoaiGhe) REFERENCES LoaiGhe(MaLoaiGhe)
);
GO

-- 10. Bảng SuatChieu
CREATE TABLE SuatChieu (
    MaSuat INT IDENTITY(1,1) PRIMARY KEY,
    MaPhim INT NOT NULL,
    MaPhong INT NOT NULL,
    GioBatDau DATETIME2 NOT NULL,
    GioKetThuc DATETIME2 NOT NULL,
    GiaCoBan DECIMAL(12,2) NOT NULL CHECK (GiaCoBan > 0),
    CONSTRAINT CK_SuatChieu_ThoiGian CHECK (GioKetThuc > GioBatDau),
    CONSTRAINT FK_SuatChieu_Phim FOREIGN KEY (MaPhim) REFERENCES Phim(MaPhim),
    CONSTRAINT FK_SuatChieu_PhongChieu FOREIGN KEY (MaPhong) REFERENCES PhongChieu(MaPhong)
);
GO

-- 11. Bảng DatVe (Đơn đặt vé)
CREATE TABLE DatVe (
    MaDatVe INT IDENTITY(1,1) PRIMARY KEY,
    MaTK INT NOT NULL,
    MaKH INT NULL,
    MaSuat INT NOT NULL,
    NgayDat DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    TongTien DECIMAL(14,2) NOT NULL DEFAULT 0 CHECK (TongTien >= 0),
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'DA_THANH_TOAN'
        CHECK (TrangThai IN ('CHO_THANH_TOAN', 'DA_THANH_TOAN', 'DA_HUY')),
    CONSTRAINT FK_DatVe_TaiKhoan FOREIGN KEY (MaTK) REFERENCES TaiKhoan(MaTK),
    CONSTRAINT FK_DatVe_KhachHang FOREIGN KEY (MaKH) REFERENCES KhachHang(MaKH),
    CONSTRAINT FK_DatVe_SuatChieu FOREIGN KEY (MaSuat) REFERENCES SuatChieu(MaSuat)
);
GO

-- 12. Bảng Ve (Chi tiết từng ghế trong đơn đặt vé)
CREATE TABLE Ve (
    MaVe INT IDENTITY(1,1) PRIMARY KEY,
    MaDatVe INT NOT NULL,
    MaSuat INT NOT NULL,
    MaGhe INT NOT NULL,
    GiaVe DECIMAL(12,2) NOT NULL CHECK (GiaVe > 0),
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'HIEU_LUC'
        CHECK (TrangThai IN ('HIEU_LUC', 'HOP_LE', 'DA_SU_DUNG', 'DA_HUY')),
    CONSTRAINT FK_Ve_DatVe FOREIGN KEY (MaDatVe) REFERENCES DatVe(MaDatVe) ON DELETE CASCADE,
    CONSTRAINT FK_Ve_SuatChieu FOREIGN KEY (MaSuat) REFERENCES SuatChieu(MaSuat),
    CONSTRAINT FK_Ve_Ghe FOREIGN KEY (MaGhe) REFERENCES Ghe(MaGhe)
);
GO

-- Chỉ mục UNIQUE có lọc: chống 2 người đặt trùng 1 ghế trong cùng 1 suất chiếu
CREATE UNIQUE INDEX UX_Ve_Suat_Ghe ON Ve(MaSuat, MaGhe) WHERE TrangThai = 'HIEU_LUC';
GO

-- 13. Bảng ThanhToan
CREATE TABLE ThanhToan (
    MaTT INT IDENTITY(1,1) PRIMARY KEY,
    MaDatVe INT NOT NULL,
    PhuongThuc NVARCHAR(50) NOT NULL,
    SoTien DECIMAL(14,2) NOT NULL CHECK (SoTien > 0),
    NgayThanhToan DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'THANH_CONG'
        CHECK (TrangThai IN ('CHO_XU_LY', 'THANH_CONG', 'THAT_BAI', 'HOAN_TIEN')),
    MaGiaoDich VARCHAR(60) NULL,
    CONSTRAINT FK_ThanhToan_DatVe FOREIGN KEY (MaDatVe) REFERENCES DatVe(MaDatVe)
);
GO 

-- #####################################################################
-- PHAN 2: 02_du_lieu_mau.sql
-- #####################################################################

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
(2, 2, 181, 143000, 'HIEU_LUC'),   -- F1 phong IMAX (VIP): 110000 x 1.3
(2, 2, 182, 143000, 'HIEU_LUC'),   -- F2 phong IMAX (VIP)
(3, 3, 61,  75000,  'HIEU_LUC'),
(3, 3, 62,  75000,  'HIEU_LUC');
GO

INSERT INTO ThanhToan (MaDatVe, PhuongThuc, SoTien, NgayThanhToan, TrangThai, MaGiaoDich) VALUES
(1, 'VI_DIEN_TU', 160000, DATEADD(DAY, -2, SYSDATETIME()), 'THANH_CONG', 'GD20261121001'),
(2, 'VI_DIEN_TU', 286000, DATEADD(DAY, -1, SYSDATETIME()), 'THANH_CONG', 'GD20261122002'),
(3, 'TIEN_MAT',   150000, SYSDATETIME(),                   'THANH_CONG', 'GD20261123003');
GO

-- #####################################################################
-- PHAN 3: 03_procedure_trigger.sql
-- #####################################################################

USE DatVeXemPhim;
GO

-- =========================================================================
-- 1. HÀM TÍNH TỔNG DOANH THU THỰC THU THEO KHOẢNG NGÀY (Dùng cho ThongKeDAO)
-- =========================================================================
CREATE OR ALTER FUNCTION dbo.fn_DoanhThu (
    @TuNgay DATE,
    @DenNgay DATE
)
RETURNS DECIMAL(18, 2)
AS
BEGIN
    DECLARE @Tong DECIMAL(18, 2);

    SELECT @Tong = ISNULL(SUM(SoTien), 0)
    FROM ThanhToan
    WHERE TrangThai = 'THANH_CONG'
      AND CAST(NgayThanhToan AS DATE) BETWEEN @TuNgay AND @DenNgay;

    RETURN @Tong;
END;
GO

-- =========================================================================
-- 2. THỦ TỤC ĐẶT VÉ TRỌN GÓI CÓ TRANSACTION (Dùng cho DatVeDAO)
-- =========================================================================
CREATE OR ALTER PROCEDURE dbo.sp_DatVe
    @MaKH INT,
    @MaSuat INT,
    @DanhSachMaGhe VARCHAR(MAX),   -- Ví dụ: '12,13,14'
    @PhuongThuc NVARCHAR(50),
    @MaDatVeOut INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Xác định MaTK tương ứng từ MaKH
        DECLARE @MaTK INT;
        SELECT @MaTK = MaTK FROM KhachHang WHERE MaKH = @MaKH;
        IF @MaTK IS NULL
        BEGIN
            -- Nếu khách vãng lai hoặc không tìm thấy, lấy tài khoản khách hàng mặc định
            SET @MaTK = ISNULL((SELECT TOP 1 MaTK FROM TaiKhoan WHERE MaTK = @MaKH), 1);
        END

        IF NOT EXISTS (SELECT 1 FROM SuatChieu WHERE MaSuat = @MaSuat)
        BEGIN
            THROW 50001, N'Suất chiếu không tồn tại.', 1;
        END

        -- Kiểm tra suất chiếu chưa bắt đầu
        IF EXISTS (SELECT 1 FROM SuatChieu WHERE MaSuat = @MaSuat AND GioBatDau <= SYSDATETIME())
        BEGIN
            THROW 50001, N'Suất chiếu đã bắt đầu, không thể đặt vé.', 1;
        END

        DECLARE @BangGhe TABLE (MaGhe INT PRIMARY KEY);
        INSERT INTO @BangGhe (MaGhe)
        SELECT DISTINCT TRY_CAST(TRIM(value) AS INT)
        FROM STRING_SPLIT(@DanhSachMaGhe, ',')
        WHERE TRY_CAST(TRIM(value) AS INT) IS NOT NULL;

        IF NOT EXISTS (SELECT 1 FROM @BangGhe)
        BEGIN
            THROW 50001, N'Danh sách ghế đặt không hợp lệ.', 1;
        END

        -- Kiểm tra ghế có thuộc đúng phòng của suất chiếu không
        IF EXISTS (
            SELECT 1
            FROM @BangGhe bg
            LEFT JOIN Ghe g ON bg.MaGhe = g.MaGhe
            JOIN SuatChieu sc ON sc.MaSuat = @MaSuat
            WHERE g.MaGhe IS NULL OR g.MaPhong <> sc.MaPhong
        )
        BEGIN
            THROW 50001, N'Có ghế không thuộc phòng chiếu của suất này.', 1;
        END

        -- Kiểm tra trùng ghế đã đặt (khóa cập nhật để tránh 2 người đặt cùng lúc)
        IF EXISTS (
            SELECT 1
            FROM Ve v WITH (UPDLOCK, HOLDLOCK)
            JOIN @BangGhe bg ON v.MaGhe = bg.MaGhe
            WHERE v.MaSuat = @MaSuat
              AND v.TrangThai <> 'DA_HUY'
        )
        BEGIN
            THROW 50002, N'Một hoặc nhiều ghế bạn chọn đã có người đặt trước.', 1;
        END

        -- Tính tổng tiền theo GiaCoBan * HeSoGia
        DECLARE @TongTien DECIMAL(14, 2);
        SELECT @TongTien = SUM(ROUND(sc.GiaCoBan * lg.HeSoGia, 0))
        FROM @BangGhe bg
        JOIN Ghe g ON bg.MaGhe = g.MaGhe
        JOIN LoaiGhe lg ON g.MaLoaiGhe = lg.MaLoaiGhe
        CROSS JOIN SuatChieu sc
        WHERE sc.MaSuat = @MaSuat;

        -- Tạo đơn đặt vé
        INSERT INTO DatVe (MaTK, MaKH, MaSuat, NgayDat, TongTien, TrangThai)
        VALUES (@MaTK, @MaKH, @MaSuat, SYSDATETIME(), @TongTien, 'DA_THANH_TOAN');

        SET @MaDatVeOut = SCOPE_IDENTITY();

        -- Tạo chi tiết từng vé (Trạng thái HIEU_LUC khớp với Java model Ve.HIEU_LUC)
        INSERT INTO Ve (MaDatVe, MaSuat, MaGhe, GiaVe, TrangThai)
        SELECT @MaDatVeOut, @MaSuat, bg.MaGhe, ROUND(sc.GiaCoBan * lg.HeSoGia, 0), 'HIEU_LUC'
        FROM @BangGhe bg
        JOIN Ghe g ON bg.MaGhe = g.MaGhe
        JOIN LoaiGhe lg ON g.MaLoaiGhe = lg.MaLoaiGhe
        CROSS JOIN SuatChieu sc
        WHERE sc.MaSuat = @MaSuat;

        -- Ghi nhận thanh toán
        INSERT INTO ThanhToan (MaDatVe, PhuongThuc, SoTien, NgayThanhToan, TrangThai, MaGiaoDich)
        VALUES (
            @MaDatVeOut,
            @PhuongThuc,
            @TongTien,
            SYSDATETIME(),
            'THANH_CONG',
            'GD' + FORMAT(SYSDATETIME(), 'yyyyMMddHHmmss') + '_' + CAST(@MaDatVeOut AS VARCHAR(10))
        );

        -- Cộng điểm tích lũy cho khách hàng (1 điểm / 10.000đ)
        IF @MaKH IS NOT NULL
        BEGIN
            UPDATE KhachHang
            SET DiemTichLuy = DiemTichLuy + CAST(@TongTien / 10000 AS INT)
            WHERE MaKH = @MaKH;
        END

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- =========================================================================
-- 3. TRIGGER 1: CHẶN TRÙNG LỊCH CHIẾU TRONG CÙNG PHÒNG
-- =========================================================================
CREATE OR ALTER TRIGGER dbo.trg_SuatChieu_KiemTraTrungLich
ON SuatChieu
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN SuatChieu sc
          ON i.MaPhong = sc.MaPhong
         AND i.MaSuat <> sc.MaSuat
         AND i.GioBatDau < sc.GioKetThuc
         AND i.GioKetThuc > sc.GioBatDau
    )
    BEGIN
        RAISERROR(N'Phòng chiếu đã có suất chiếu khác trùng trong khung giờ này.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

-- =========================================================================
-- 4. TRIGGER 2: CHẶN ĐẶT TRÙNG GHẾ CÙNG SUẤT & KIỂM TRA ĐÚNG PHÒNG
-- =========================================================================
CREATE OR ALTER TRIGGER dbo.trg_Ve_KiemTraToanVen
ON Ve
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    -- Kiểm tra ghế có đúng phòng với suất chiếu không
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN SuatChieu sc ON i.MaSuat = sc.MaSuat
        JOIN Ghe g ON i.MaGhe = g.MaGhe
        WHERE sc.MaPhong <> g.MaPhong
    )
    BEGIN
        RAISERROR(N'Ghế được chọn không thuộc phòng chiếu của suất chiếu.', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END

    -- Kiểm tra ghế đã có vé hợp lệ khác trong cùng suất chiếu chưa
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN Ve v
          ON i.MaSuat = v.MaSuat
         AND i.MaGhe = v.MaGhe
         AND i.MaVe <> v.MaVe
        WHERE i.TrangThai <> 'DA_HUY'
          AND v.TrangThai <> 'DA_HUY'
    )
    BEGIN
        RAISERROR(N'Ghế này đã được bán cho một vé khác trong cùng suất chiếu.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

-- =========================================================================
-- 5. TRIGGER 3: ĐỒNG BỘ TRẠNG THÁI ĐƠN ĐẶT VÉ & VÉ KHI HOÀN TIỀN
-- =========================================================================
CREATE OR ALTER TRIGGER dbo.trg_ThanhToan_DongBoTrangThai
ON ThanhToan
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE dv
    SET dv.TrangThai = 'DA_THANH_TOAN'
    FROM DatVe dv
    JOIN inserted i ON dv.MaDatVe = i.MaDatVe
    WHERE i.TrangThai = 'THANH_CONG';

    UPDATE dv
    SET dv.TrangThai = 'DA_HUY'
    FROM DatVe dv
    JOIN inserted i ON dv.MaDatVe = i.MaDatVe
    WHERE i.TrangThai = 'HOAN_TIEN';

    UPDATE v
    SET v.TrangThai = 'DA_HUY'
    FROM Ve v
    JOIN inserted i ON v.MaDatVe = i.MaDatVe
    WHERE i.TrangThai = 'HOAN_TIEN';
END;
GO

