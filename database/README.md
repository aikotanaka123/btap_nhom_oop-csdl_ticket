# Thư mục database

Khánh đặt các script ở đây, chạy theo thứ tự tên file:

| File | Nội dung |
|---|---|
| `01_tao_bang.sql` | `CREATE DATABASE DatVeXemPhim`, 13 bảng, khóa chính, khóa ngoại, CHECK, UNIQUE |
| `02_du_lieu_mau.sql` | Dữ liệu mẫu |
| `03_procedure_trigger.sql` | `sp_DatVe`, `fn_DoanhThu`, trigger chống trùng ghế |
| `04_truy_van.sql` | 10–15 truy vấn minh họa cho báo cáo |

Lược đồ chi tiết (kiểu dữ liệu, khóa) xem trang 2 của `docs/ERD_DatVeXemPhim.drawio`.

## Giá trị trạng thái (code Java đọc đúng các chuỗi này)
| Cột | Giá trị |
|---|---|
| `TaiKhoan.VaiTro` | `KHACHHANG`, `NHANVIEN` |
| `TaiKhoan.MatKhau` | chuỗi SHA-256 dạng hex (64 ký tự) – dùng `PasswordUtil.hash()` để tạo dữ liệu mẫu |
| `Phim.TrangThai` | `DANG_CHIEU`, `SAP_CHIEU`, `NGUNG_CHIEU` |
| `Phim.DoTuoi` | `P`, `T13`, `T16`, `T18` |
| `DatVe.TrangThai` | `CHO_THANH_TOAN`, `DA_THANH_TOAN`, `DA_HUY` |
| `Ve.TrangThai` | `HIEU_LUC`, `DA_HUY` |
| `ThanhToan.PhuongThuc` | `TIEN_MAT`, `THE`, `VI_DIEN_TU` |
| `ThanhToan.TrangThai` | `THANH_CONG`, `HOAN_TIEN` |

## Chống trùng ghế
Ghế đã hủy phải đặt lại được, nên dùng **chỉ mục UNIQUE có lọc** thay vì UNIQUE thường:
```sql
CREATE UNIQUE INDEX UX_Ve_Suat_Ghe ON Ve(MaSuat, MaGhe) WHERE TrangThai = 'HIEU_LUC';
```
Trigger của Khánh kiểm tra thêm: ghế phải thuộc đúng phòng của suất chiếu.

## Giao ước `sp_DatVe` (Java gọi trong `DatVeDAOImpl`)
```sql
CREATE PROCEDURE sp_DatVe
    @MaKH        INT,
    @MaSuat      INT,
    @DanhSachGhe VARCHAR(500),   -- danh sách MaGhe cách nhau dấu phẩy, vd '12,13,14'
    @PhuongThuc  VARCHAR(20),    -- TIEN_MAT / THE / VI_DIEN_TU
    @MaDatVe     INT OUTPUT
```
Trong **một transaction**: kiểm tra suất chưa bắt đầu → kiểm tra từng ghế còn trống → thêm `DatVe` (`DA_THANH_TOAN`) → thêm các `Ve` với `GiaVe = SuatChieu.GiaCoBan × LoaiGhe.HeSoGia` → thêm `ThanhToan` (`SoTien` = tổng `GiaVe`) → `COMMIT`. Lỗi thì `ROLLBACK`.

Báo lỗi nghiệp vụ bằng `THROW` với **mã ≥ 50000** và thông báo tiếng Việt; Java chuyển nguyên thông báo đó lên giao diện:
```sql
THROW 50001, N'Suất chiếu đã bắt đầu, không thể đặt vé.', 1;
THROW 50002, N'Ghế đã có người đặt. Vui lòng chọn ghế khác.', 1;
THROW 50003, N'Ghế không thuộc phòng của suất chiếu này.', 1;
```
Có thể tách chuỗi ghế bằng `STRING_SPLIT(@DanhSachGhe, ',')` (SQL Server 2016+).

## Yêu cầu phiên bản
Màn hình tìm kiếm phim (`TimKiemSql`) dùng `STRING_AGG` để gộp tên thể loại, nên cần **SQL Server 2017 trở lên**.

## `fn_DoanhThu`
`ThongKeDAO` dự kiến gọi `fn_DoanhThu(@TuNgay DATE, @DenNgay DATE)` trả về bảng doanh thu, chỉ tính `ThanhToan.TrangThai = 'THANH_CONG'`. Khánh chốt chữ ký hàm rồi cập nhật lại mục này.
