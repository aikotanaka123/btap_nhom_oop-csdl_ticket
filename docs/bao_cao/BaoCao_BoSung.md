# Phần bổ sung cho báo cáo – Ứng dụng đặt vé xem phim

Tài liệu gồm hai phần:

- **Phần A – Giao diện (OOP):** ảnh 15 màn hình, chụp khi app chạy **chế độ JDBC** trên SQL Server. Không còn nhãn "dữ liệu giả".
- **Phần B – Cơ sở dữ liệu:** ERD, mô hình quan hệ, chuẩn hóa, bảng và dữ liệu, 15 truy vấn, kiểm thử thủ tục/trigger.

Mọi ảnh ở phần B là **kết quả chạy thật** trên Microsoft SQL Server 2025. CSDL được tạo mới bằng chính `database/tao_csdl.sql`, rồi chạy `database/truy_van.sql`. Mỗi ảnh có câu SQL ở trên và lưới kết quả ở dưới. Đây là ảnh vẽ lại từ kết quả JDBC, **không phải ảnh chụp cửa sổ SSMS**. Nếu giảng viên yêu cầu ảnh chụp SSMS, xem mục [C](#c-nếu-cần-ảnh-chụp-trực-tiếp-từ-ssms).

Thư mục:

```
docs/
├── ERD_DatVeXemPhim_v2.drawio     ERD mới (2 trang) – mở bằng https://app.diagrams.net
├── anh_man_hinh.png               ảnh ghép 15 màn hình
└── bao_cao/
    ├── BaoCao_BoSung.md           tài liệu này
    ├── ERD_1_Chen.png             ảnh xem trước trang 1 của file .drawio
    ├── ERD_2_LuocDoQuanHe.png     ảnh xem trước trang 2 của file .drawio
    ├── anh_oop/                   15 ảnh màn hình
    └── anh_csdl/                  14 bảng + danh sách bảng, 15 truy vấn, 7 kiểm thử
```

---

## A. Giao diện ứng dụng

Tài khoản dùng khi chụp: `khach1 / 123456` (khách hàng Trần Hoàng Nam) và `admin / admin123` (quản trị).

| # | Ảnh | Màn hình | Lớp Java | Người làm |
|---|---|---|---|---|
| 1 | `anh_oop/01_dang_nhap.png` | Đăng nhập, có nhãn chế độ JDBC | `LoginFrame` | Trung |
| 2 | `anh_oop/02_dang_ky.png` | Đăng ký tài khoản khách hàng | `DangKyDialog` | Trung |
| 3 | `anh_oop/03_danh_sach_phim.png` | Danh sách phim đang chiếu, lọc theo tên/thể loại | `DanhSachPhimPanel` | Trung |
| 4 | `anh_oop/04_chi_tiet_phim.png` | Chi tiết phim | `ChiTietPhimDialog` | Trung |
| 5 | `anh_oop/05_tim_kiem_prepared.png` | Tìm kiếm phim, câu SQL dùng tham số `?` | `TimKiemPhimPanel` | Khuê |
| 6 | `anh_oop/06_tan_cong_prepared_an_toan.png` | Chuỗi tấn công với PreparedStatement: 0 kết quả, chuỗi chỉ nằm trong tham số | `TimKiemPhimPanel` | Khuê |
| 7 | `anh_oop/07_tan_cong_noi_chuoi_sql_injection.png` | Chuỗi tấn công khi nối chuỗi: `UNION` lấy được tên đăng nhập và mật khẩu đã băm từ bảng `TaiKhoan` | `TimKiemPhimPanel` | Khuê |
| 8 | `anh_oop/08_chon_suat_chieu.png` | Chọn suất chiếu của phim | `ChonSuatChieuPanel` | Trung |
| 9 | `anh_oop/09_so_do_ghe.png` | Sơ đồ ghế: thường/VIP/đôi, đang chọn 4 ghế, tạm tính | `SoDoGhePanel` | Trung |
| 10 | `anh_oop/10_xac_nhan_dat_ve.png` | Xác nhận đặt vé và chọn phương thức thanh toán | `XacNhanDatVeDialog` | Khuê |
| 11 | `anh_oop/11_ve_cua_toi.png` | Lịch sử vé của khách | `LichSuVePanel` | Trung |
| 12 | `anh_oop/12_admin_quan_ly_phim.png` | Quản lý danh mục phim | `QuanLyPhimPanel` | Khánh |
| 13 | `anh_oop/13_admin_phong_chieu_ghe.png` | Quản lý phòng chiếu và sơ đồ ghế | `QuanLyPhongPanel` | Khánh |
| 14 | `anh_oop/14_admin_lich_suat_chieu.png` | Quản lý lịch suất chiếu | `QuanLySuatChieuPanel` | Khánh |
| 15 | `anh_oop/15_admin_thong_ke.png` | Thống kê doanh thu | `ThongKePanel` | Khánh |

Ghi chú:

- Khi chụp, ứng dụng nối tới một bản CSDL tạo mới từ `tao_csdl.sql`. Vì vậy nhãn trên ảnh ghi tên CSDL là `DatVeXemPhim_BaoCao`, còn dữ liệu đúng như dữ liệu mẫu trong script.
- Ở ảnh 10 hộp thoại được đóng bằng "Hủy", nên không có đơn nào được ghi thêm vào CSDL.

---

## B. Cơ sở dữ liệu

### B.1. Mô hình thực thể – liên kết (ERD)

File `docs/ERD_DatVeXemPhim_v2.drawio`, trang **"1. ERD (Chen)"**, ảnh xem trước `ERD_1_Chen.png`.

**13 thực thể** (bảng `Phim_TheLoai` không phải thực thể mà là liên kết N–M "Thuộc"):

| Thực thể | Khóa | Thuộc tính |
|---|---|---|
| VaiTro | <u>MaVaiTro</u> | TenVaiTro |
| TaiKhoan | <u>MaTK</u> | TenDangNhap, MatKhau, HoTen, Email, SoDienThoai, VaiTro, TrangThai, NgayTao |
| KhachHang | <u>MaKH</u> | HoTen, SoDienThoai, Email, NgaySinh, DiemTichLuy |
| NhanVien | <u>MaNV</u> | HoTen, ChucVu, SoDienThoai |
| TheLoai | <u>MaTL</u> | TenTL |
| Phim | <u>MaPhim</u> | TenPhim, ThoiLuong, DaoDien, NgayKhoiChieu, DoTuoi, TrangThai, Poster, MoTa |
| PhongChieu | <u>MaPhong</u> | TenPhong, SoHang, SoCot, HoatDong |
| LoaiGhe | <u>MaLoaiGhe</u> | TenLoai, HeSoGia |
| Ghe | <u>MaGhe</u> | Hang, SoGhe |
| SuatChieu | <u>MaSuat</u> | GioBatDau, GioKetThuc, GiaCoBan |
| DatVe | <u>MaDatVe</u> | NgayDat, TrangThai, TongTien (dẫn xuất = tổng GiaVe) |
| Ve | <u>MaVe</u> | GiaVe, TrangThai |
| ThanhToan | <u>MaTT</u> | PhuongThuc, SoTien, NgayThanhToan, TrangThai, MaGiaoDich |

**15 mối liên kết:**

| Liên kết | Thực thể | Bản số | Ghi chú |
|---|---|---|---|
| Có vai trò | TaiKhoan – VaiTro | N – 1 | |
| Là | TaiKhoan – NhanVien | 1 – 1 | `NhanVien.MaTK` NOT NULL, UNIQUE |
| Là | TaiKhoan – KhachHang | 1 – 1 | `KhachHang.MaTK` NULL được (khách vãng lai), UNIQUE |
| Lập | TaiKhoan – DatVe | 1 – N | tài khoản **lập đơn** (có thể là nhân viên bán tại quầy) |
| Đặt | KhachHang – DatVe | 1 – N | `DatVe.MaKH` NULL được |
| Dành cho | SuatChieu – DatVe | 1 – N | |
| Gồm | DatVe – Ve | 1 – N | xóa đơn thì xóa vé (ON DELETE CASCADE) |
| Được thanh toán | DatVe – ThanhToan | 1 – N | một đơn có thể có giao dịch thanh toán và giao dịch hoàn tiền |
| Có | Phim – SuatChieu | 1 – N | |
| Diễn ra tại | PhongChieu – SuatChieu | 1 – N | trigger chặn 2 suất trùng giờ trong một phòng |
| Của suất | SuatChieu – Ve | 1 – N | |
| Gắn với | Ghe – Ve | 1 – N | mỗi ghế chỉ có một vé hiệu lực trong một suất |
| Có | PhongChieu – Ghe | 1 – N | |
| Phân loại | LoaiGhe – Ghe | 1 – N | giá vé = GiaCoBan × HeSoGia |
| Thuộc | Phim – TheLoai | N – M | thành bảng `Phim_TheLoai` |

**Tham gia toàn phần (nét đôi trên ERD).** Phía nào có khóa ngoại NOT NULL thì mọi bản ghi phía đó bắt buộc tham gia liên kết, vẽ bằng nét đôi. 12 phía như vậy:

| Thực thể tham gia toàn phần | Liên kết | Khóa ngoại NOT NULL |
|---|---|---|
| TaiKhoan | Có vai trò | `TaiKhoan.MaVaiTro` |
| NhanVien | Là | `NhanVien.MaTK` |
| DatVe | Lập, Dành cho | `DatVe.MaTK`, `DatVe.MaSuat` |
| ThanhToan | Được thanh toán | `ThanhToan.MaDatVe` |
| Ve | Gồm, Của suất, Gắn với | `Ve.MaDatVe`, `Ve.MaSuat`, `Ve.MaGhe` |
| SuatChieu | Có, Diễn ra tại | `SuatChieu.MaPhim`, `SuatChieu.MaPhong` |
| Ghe | Có, Phân loại | `Ghe.MaPhong`, `Ghe.MaLoaiGhe` |

Phía còn lại của các liên kết này là tham gia bộ phận (nét đơn). Ví dụ một phim có thể chưa có suất chiếu, và một khách vãng lai thì không có tài khoản (`KhachHang.MaTK`, `DatVe.MaKH` cho phép NULL).

### B.2. Mô hình quan hệ

File `.drawio`, trang **"2. Lược đồ quan hệ"**, ảnh `ERD_2_LuocDoQuanHe.png`. Khóa chính **gạch chân**, khóa ngoại *in nghiêng* (→ bảng được tham chiếu).

1. **VaiTro**(<u>MaVaiTro</u>, TenVaiTro)
2. **TaiKhoan**(<u>MaTK</u>, TenDangNhap, MatKhau, HoTen, Email, SoDienThoai, *MaVaiTro* → VaiTro, VaiTro, TrangThai, NgayTao)
3. **KhachHang**(<u>MaKH</u>, *MaTK* → TaiKhoan, HoTen, SoDienThoai, Email, NgaySinh, DiemTichLuy)
4. **NhanVien**(<u>MaNV</u>, *MaTK* → TaiKhoan, HoTen, ChucVu, SoDienThoai)
5. **TheLoai**(<u>MaTL</u>, TenTL)
6. **Phim**(<u>MaPhim</u>, TenPhim, ThoiLuong, DaoDien, NgayKhoiChieu, DoTuoi, TrangThai, Poster, MoTa)
7. **Phim_TheLoai**(<u>*MaPhim*, *MaTL*</u>) → Phim, TheLoai
8. **PhongChieu**(<u>MaPhong</u>, TenPhong, SoHang, SoCot, HoatDong)
9. **LoaiGhe**(<u>MaLoaiGhe</u>, TenLoai, HeSoGia)
10. **Ghe**(<u>MaGhe</u>, *MaPhong* → PhongChieu, *MaLoaiGhe* → LoaiGhe, Hang, SoGhe)
11. **SuatChieu**(<u>MaSuat</u>, *MaPhim* → Phim, *MaPhong* → PhongChieu, GioBatDau, GioKetThuc, GiaCoBan)
12. **DatVe**(<u>MaDatVe</u>, *MaTK* → TaiKhoan, *MaKH* → KhachHang, *MaSuat* → SuatChieu, NgayDat, TongTien, TrangThai)
13. **Ve**(<u>MaVe</u>, *MaDatVe* → DatVe, *MaSuat* → SuatChieu, *MaGhe* → Ghe, GiaVe, TrangThai)
14. **ThanhToan**(<u>MaTT</u>, *MaDatVe* → DatVe, PhuongThuc, SoTien, NgayThanhToan, TrangThai, MaGiaoDich)

Ràng buộc chính (đọc từ `tao_csdl.sql`):

| Loại | Ràng buộc |
|---|---|
| Khóa dự tuyển (UNIQUE) | `TaiKhoan.TenDangNhap`, `KhachHang.SoDienThoai`, `KhachHang.MaTK`, `NhanVien.MaTK`, `TheLoai.TenTL`, `PhongChieu.TenPhong`, `LoaiGhe.TenLoai`, `VaiTro.TenVaiTro`, `Ghe(MaPhong, Hang, SoGhe)` |
| UNIQUE có lọc | `UX_Ve_Suat_Ghe ON Ve(MaSuat, MaGhe) WHERE TrangThai = 'HIEU_LUC'`: ghế đã hủy được đặt lại |
| CHECK | `ThoiLuong > 0`, `DoTuoi IN ('P','T13','T16','T18')`, `SoHang 1..26`, `SoCot 1..30`, `HeSoGia > 0`, `GiaCoBan > 0`, `GioKetThuc > GioBatDau`, `DiemTichLuy >= 0`, `TongTien >= 0`, `GiaVe > 0`, `SoTien > 0`, các cột `TrangThai` |
| ON DELETE CASCADE | `Phim_TheLoai` theo `Phim` và `TheLoai`; `Ghe` theo `PhongChieu`; `Ve` theo `DatVe` |
| Trigger | `trg_SuatChieu_KiemTraTrungLich`, `trg_Ve_KiemTraToanVen`, `trg_ThanhToan_DongBoTrangThai` |
| Thủ tục / hàm | `sp_DatVe` (transaction đặt vé trọn gói), `fn_DoanhThu(@TuNgay, @DenNgay)` |

### B.3. Phụ thuộc hàm và chuẩn hóa

#### Bước 1 – Từ quan hệ gộp ban đầu đến 3NF

Gom toàn bộ thông tin của một vé vào một quan hệ duy nhất (ở dạng 1NF, mọi giá trị đều nguyên tố; thể loại của phim đã tách thành từng dòng):

> R(MaDatVe, NgayDat, MaKH, HoTenKH, SoDienThoaiKH, MaSuat, GioBatDau, GioKetThuc, GiaCoBan, MaPhim, TenPhim, ThoiLuong, MaPhong, TenPhong, MaGhe, Hang, SoGhe, MaLoaiGhe, TenLoai, HeSoGia, GiaVe, TrangThaiVe, PhuongThuc, SoTien)

Khóa của R: **{MaDatVe, MaGhe}** (một đơn đặt nhiều ghế, một ghế chỉ thuộc một suất trong một đơn).

Phụ thuộc hàm:

| # | Phụ thuộc hàm | Loại |
|---|---|---|
| f1 | MaDatVe → NgayDat, MaKH, MaSuat, PhuongThuc, SoTien | **bộ phận** (chỉ vào một phần khóa) |
| f2 | MaGhe → Hang, SoGhe, MaLoaiGhe, MaPhong | **bộ phận** |
| f3 | {MaDatVe, MaGhe} → GiaVe, TrangThaiVe | đầy đủ |
| f4 | MaKH → HoTenKH, SoDienThoaiKH | **bắc cầu** qua MaDatVe |
| f5 | MaSuat → GioBatDau, GioKetThuc, GiaCoBan, MaPhim, MaPhong | **bắc cầu** |
| f6 | MaPhim → TenPhim, ThoiLuong | **bắc cầu** |
| f7 | MaPhong → TenPhong | **bắc cầu** |
| f8 | MaLoaiGhe → TenLoai, HeSoGia | **bắc cầu** |

- **R đạt 1NF nhưng không đạt 2NF** vì có f1 và f2. Tách theo f1, f2:
  R1(<u>MaDatVe</u>, NgayDat, MaKH, MaSuat, PhuongThuc, SoTien, …), R2(<u>MaGhe</u>, Hang, SoGhe, MaLoaiGhe, MaPhong, …), R3(<u>MaDatVe, MaGhe</u>, GiaVe, TrangThaiVe).
- **Đạt 2NF nhưng chưa đạt 3NF** vì còn f4–f8. Tách tiếp mỗi phụ thuộc bắc cầu thành một quan hệ:
  KhachHang (f4), SuatChieu (f5), Phim (f6), PhongChieu (f7), LoaiGhe (f8).
  Thông tin thanh toán (PhuongThuc, SoTien) tách thành ThanhToan, vì một đơn có thể có nhiều giao dịch (thanh toán rồi hoàn tiền).
  R3 được đặt khóa đại diện MaVe để dễ tham chiếu, khóa tự nhiên {MaDatVe, MaGhe} vẫn được bảo đảm.
- Các bảng tài khoản (VaiTro, TaiKhoan, NhanVien) và thể loại (TheLoai, Phim_TheLoai) tách theo cách tương tự. Kết quả là 14 bảng như mục B.2.

#### Bước 2 – Kiểm tra dạng chuẩn của 14 bảng

| Bảng | Khóa dự tuyển | Phụ thuộc hàm không tầm thường | Dạng chuẩn |
|---|---|---|---|
| VaiTro | MaVaiTro; TenVaiTro | MaVaiTro → TenVaiTro; TenVaiTro → MaVaiTro | BCNF |
| TaiKhoan | MaTK; TenDangNhap | MaTK → mọi cột; TenDangNhap → mọi cột; *MaVaiTro → VaiTro* (xem ghi chú 1) | BCNF với khóa; có dư thừa có chủ đích |
| KhachHang | MaKH; SoDienThoai; MaTK (khi khác NULL) | vế trái đều là khóa dự tuyển | BCNF |
| NhanVien | MaNV; MaTK | MaNV → mọi cột; MaTK → mọi cột | BCNF |
| TheLoai | MaTL; TenTL | | BCNF |
| Phim | MaPhim | MaPhim → mọi cột | BCNF |
| Phim_TheLoai | {MaPhim, MaTL} | chỉ có phụ thuộc tầm thường | BCNF |
| PhongChieu | MaPhong; TenPhong | | BCNF |
| LoaiGhe | MaLoaiGhe; TenLoai | | BCNF |
| Ghe | MaGhe; {MaPhong, Hang, SoGhe} | MaGhe → mọi cột; {MaPhong, Hang, SoGhe} → MaGhe, MaLoaiGhe | BCNF |
| SuatChieu | MaSuat | MaSuat → mọi cột | BCNF |
| DatVe | MaDatVe | MaDatVe → mọi cột (xem ghi chú 3) | BCNF; TongTien là cột dẫn xuất |
| Ve | MaVe; {MaSuat, MaGhe} khi vé hiệu lực | MaVe → mọi cột; *MaDatVe → MaSuat* (xem ghi chú 2) | 2NF; dư thừa có chủ đích |
| ThanhToan | MaTT | MaTT → mọi cột | BCNF |

**Những chỗ dư thừa được giữ lại có chủ đích** (nên trình bày trong báo cáo, vì giảng viên thường hỏi):

1. **`TaiKhoan.VaiTro` (chuỗi) song song với `TaiKhoan.MaVaiTro`.** Trong dữ liệu có MaVaiTro → VaiTro (1, 2 → `NHANVIEN`; 3 → `KHACHHANG`), tức phụ thuộc bắc cầu MaTK → MaVaiTro → VaiTro, vi phạm 3NF.
   Cột này được giữ vì code Java (`TaiKhoan.VaiTro`, phân quyền menu) đọc trực tiếp chuỗi `KHACHHANG`/`NHANVIEN`.
   *Cách chuẩn hóa hoàn toàn:* bỏ cột `VaiTro`, suy ra từ `VaiTro.TenVaiTro` bằng JOIN (hoặc dùng view).
2. **`Ve.MaSuat` trùng với `DatVe.MaSuat`.** Mọi vé của một đơn đều cùng suất, nên MaVe → MaDatVe → MaSuat là phụ thuộc bắc cầu, Ve chỉ đạt 2NF.
   Cột này **cần thiết**: chỉ mục `UX_Ve_Suat_Ghe(MaSuat, MaGhe) WHERE TrangThai = 'HIEU_LUC'` dùng nó để chống đặt trùng ghế ngay ở mức CSDL. SQL Server không cho ràng buộc UNIQUE trải trên hai bảng.
   Tính nhất quán do `sp_DatVe` bảo đảm, vì thủ tục ghi cùng `@MaSuat` cho đơn và cho mọi vé.
3. **`DatVe.TongTien`, `ThanhToan.SoTien`, `Ve.GiaVe` là giá trị dẫn xuất/lưu vết:**
   - TongTien = Σ GiaVe;
   - GiaVe = GiaCoBan × HeSoGia **tại thời điểm đặt**.
   GiaVe phải được lưu lại, vì sau này giá suất hoặc hệ số ghế có thể đổi mà vé đã bán không được đổi giá. TongTien và SoTien lưu để thống kê nhanh.
4. **HoTen, SoDienThoai, Email có ở cả `TaiKhoan` và `KhachHang`/`NhanVien`.** Đây là dư thừa giữa các bảng, chứ không phải vi phạm dạng chuẩn trong từng bảng. Cách làm này cho phép khách vãng lai (không có tài khoản) vẫn có hồ sơ khách hàng.
5. `DatVe.MaTK` và `DatVe.MaKH` **không** phụ thuộc nhau. MaTK là tài khoản *lập đơn*, MaKH là *khách được đặt cho*. Dữ liệu mẫu có đơn số 3: nhân viên `nhanvien1` (MaTK = 2) bán cho khách MaKH = 1.

**Kết luận:** 12/14 bảng đạt BCNF. `TaiKhoan` và `Ve` còn dư thừa có kiểm soát, đổi lấy tính tiện dụng cho code (TaiKhoan) và khả năng ràng buộc chống trùng ghế (Ve).

### B.4. Bảng và dữ liệu mẫu trong SQL Server

| Ảnh | Nội dung |
|---|---|
| `anh_csdl/bang_00_danh_sach_14_bang.png` | 14 bảng, số cột, số dòng, số khóa ngoại |
| `anh_csdl/bang_01_VaiTro.png` … `bang_14_ThanhToan.png` | mỗi bảng: cấu trúc (kiểu dữ liệu, NULL, PK/FK/UNIQUE, mặc định, CHECK), ràng buộc mức bảng, `SELECT *` dữ liệu mẫu |

Số dòng dữ liệu mẫu: VaiTro 3, TaiKhoan 4, KhachHang 2, NhanVien 2, TheLoai 7, Phim 5, Phim_TheLoai 11, PhongChieu 3, LoaiGhe 3, Ghe 210 (ảnh chỉ hiện 12 dòng đầu), SuatChieu 5, DatVe 3, Ve 6, ThanhToan 3.

### B.5. Truy vấn (`database/truy_van.sql`)

| Ảnh | Truy vấn | Kỹ thuật | Kết quả |
|---|---|---|---|
| `truy_van_01.png` | Tạo view `vw_Phim_TheLoai` | VIEW, LEFT JOIN, STRING_AGG | Commands completed |
| `truy_van_02.png` | Tạo view `vw_SuatChieu_TongQuan` | VIEW, truy vấn con, tỷ lệ lấp đầy | Commands completed |
| `truy_van_03.png` | Doanh thu 30 ngày | hàm `fn_DoanhThu` | 1 dòng |
| `truy_van_04.png` | Phim kèm thể loại | truy vấn view 1 | 5 dòng |
| `truy_van_05.png` | Suất chiếu và tỷ lệ lấp đầy | truy vấn view 2 | 5 dòng |
| `truy_van_06.png` | Doanh thu, số vé theo phim | LEFT JOIN + GROUP BY | 5 dòng |
| `truy_van_07.png` | Doanh thu theo ngày | GROUP BY theo ngày | 3 dòng |
| `truy_van_08.png` | Thể loại có ≥ 2 phim | GROUP BY + HAVING | 3 dòng |
| `truy_van_09.png` | Khách chi tiêu ≥ 150.000 đ | JOIN + GROUP BY + HAVING | 2 dòng |
| `truy_van_10.png` | Chi tiết vé đã bán | INNER JOIN 7 bảng | 6 dòng |
| `truy_van_11.png` | Vé và doanh thu theo loại ghế | LEFT JOIN + GROUP BY | 3 dòng |
| `truy_van_12.png` | Ghế trống của suất 1 | NOT IN | 53 dòng |
| `truy_van_13.png` | Phim chưa có suất chiếu | NOT EXISTS | 1 dòng |
| `truy_van_14.png` | Đơn có tổng tiền > trung bình | truy vấn con vô hướng | 1 dòng |
| `truy_van_15.png` | Doanh thu từng suất của phim đang chiếu | IN + truy vấn con tương quan | 5 dòng |

### B.6. Kiểm thử thủ tục, trigger, ràng buộc

Mỗi kiểm thử chạy trong `BEGIN TRANSACTION … ROLLBACK`, nên dữ liệu không bị thay đổi.

| Ảnh | Kiểm thử | Kết quả mong đợi | Kết quả thật |
|---|---|---|---|
| `kiem_thu_01_sp_DatVe_thanh_cong.png` | `sp_DatVe` đặt C1 (thường) + D1 (VIP) suất 4 | tạo DatVe + 2 Ve + ThanhToan, giá VIP = 85.000 × 1,3 | đúng |
| `kiem_thu_02_sp_DatVe_trung_ghe.png` | đặt cùng ghế 2 lần | lần 2: `THROW 50002` | Msg 50002 |
| `kiem_thu_03_sp_DatVe_suat_da_chieu.png` | đặt vé suất đã bắt đầu | `THROW 50001` | Msg 50001 |
| `kiem_thu_04_trigger_trung_ghe.png` | INSERT vé trùng ghế A1 suất 1 | bị chỉ mục `UX_Ve_Suat_Ghe` chặn | Msg 2601 |
| `kiem_thu_05_trigger_sai_phong.png` | INSERT vé có ghế của phòng khác | trigger `trg_Ve_KiemTraToanVen` chặn | Msg 50000 |
| `kiem_thu_06_trigger_trung_lich.png` | thêm suất trùng giờ cùng phòng | trigger `trg_SuatChieu_KiemTraTrungLich` chặn | Msg 50000 |
| `kiem_thu_07_trigger_hoan_tien.png` | cập nhật thanh toán thành `HOAN_TIEN` | đơn và vé chuyển `DA_HUY` | đúng |

### B.7. Lỗi dữ liệu mẫu đã sửa

Trong `tao_csdl.sql`, phần dữ liệu `Ve`, đơn số 2 (suất 2, Phòng 03 – IMAX) trước đây đặt **ghế 161 và 162**, với `GiaVe = 143000` (= 110.000 × 1,3, tức **giá VIP**).

Theo đoạn sinh ghế tự động, hai ghế này là **D5, D6 – loại Thường**, nên truy vấn 10 và 11 ra vé ghế "Thường" mà giá 143.000 đ.

**Đã sửa:** đổi thành ghế **181, 182** (F1, F2 – VIP của phòng IMAX). Sau khi tạo lại CSDL, cả 6 vé mẫu đều có `GiaVe = GiaCoBan × HeSoGia` đúng loại ghế, và `TongTien` của 3 đơn bằng tổng giá vé. Ảnh trong `anh_csdl/` đã được vẽ lại theo dữ liệu mới. Như vậy giá 143.000 đ khớp với loại ghế, `DatVe.TongTien = 286000` vẫn đúng.

---

## C. Nếu cần ảnh chụp trực tiếp từ SSMS

Nếu giảng viên yêu cầu ảnh chụp cửa sổ SSMS:

1. SSMS → Connect `localhost` (Windows Authentication, tick *Trust server certificate*).
2. **File → Open** `database/tao_csdl.sql` → **Execute**. Script sẽ xóa và tạo lại toàn bộ dữ liệu, nên hãy sao lưu trước nếu đang có dữ liệu thật.
3. Mở `database/truy_van.sql`. Bôi đen từng khối từ dòng `-- N.` tới `GO` → **F5** → chụp cả khung SQL và tab *Results*. Có 15 ảnh, đặt tên theo bảng B.5.
4. Ảnh bảng: **Databases → DatVeXemPhim → Tables**. Chuột phải từng bảng → **Design** (chụp cấu trúc) và **Select Top 1000 Rows** (chụp dữ liệu). Có 14 bảng.
5. Kiểm thử: dán SQL in trên các ảnh `kiem_thu_*.png` (đã kèm `BEGIN TRANSACTION … ROLLBACK`) → F5 → chụp tab *Messages*/*Results*.
6. Sơ đồ CSDL của SSMS (tùy chọn): **Database Diagrams → New Database Diagram** → chọn 14 bảng.

Kết quả sẽ giống ảnh trong `anh_csdl/`, chỉ khác giờ ở các cột ngày giờ (dữ liệu mẫu tính theo ngày chạy script).
