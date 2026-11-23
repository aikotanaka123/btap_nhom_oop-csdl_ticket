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