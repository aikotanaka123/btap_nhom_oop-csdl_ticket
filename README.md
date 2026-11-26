# Ứng dụng đặt vé xem phim

Bài tập nhóm môn **Cơ sở dữ liệu** và **Lập trình hướng đối tượng (Java)**.
Ứng dụng desktop **Java Swing + Maven + SQL Server**, kết nối qua **JDBC**.

| Thành viên | Vai trò |
|---|---|
| Bùi Minh Khuê (N24DCAT042) | Trưởng nhóm – thiết kế CSDL, khung project, MainFrame, tìm kiếm phim, xác nhận đặt vé |
| Khánh | Script CSDL, stored procedure/trigger, các lớp DAO JDBC, màn hình Admin |
| Trung | Model OOP, màn hình khách hàng, kiểm tra dữ liệu, báo cáo OOP |

> **Cách nhanh nhất:** bấm đúp **`run.exe`** ở thư mục gốc → app tự kết nối SQL Server và mở ở chế độ **JDBC**
> (xem [Cách 3](#cách-3--chạy-bằng-runexe-tự-kết-nối-sql-server)).

## Mục lục
1. [Yêu cầu](#yêu-cầu)
2. [Hai chế độ chạy: MOCK và JDBC](#hai-chế-độ-chạy-mock-và-jdbc)
3. [Cách 1 – Chạy thử nhanh (không cần SQL Server)](#cách-1--chạy-thử-nhanh-không-cần-sql-server)
4. [Cách 2 – Chạy bằng Maven với SQL Server](#cách-2--chạy-bằng-maven-với-sql-server)
5. [Cách 3 – Chạy bằng run.exe (tự kết nối SQL Server)](#cách-3--chạy-bằng-runexe-tự-kết-nối-sql-server)
6. [Xem dữ liệu của app trong SSMS](#xem-dữ-liệu-của-app-trong-ssms)
7. [Gặp lỗi?](#gặp-lỗi)
8. [Cấu trúc thư mục](#cấu-trúc-thư-mục)
9. [Dành cho lập trình viên](#giao-diện-swing--gắn-màn-hình-của-mình-vào-mainframe)
10. [Nhật ký cập nhật](#nhật-ký-cập-nhật)

## Yêu cầu
- JDK 17 trở lên, Maven 3.8+ (IntelliJ/Eclipse/NetBeans đã có sẵn Maven)
- SQL Server **2017+** (màn hình tìm kiếm dùng `STRING_AGG`) – chỉ cần khi chạy chế độ `jdbc`
- SSMS và `sqlcmd` (đi kèm khi cài SQL Server/SSMS) – để tạo database và xem dữ liệu
- Chạy `run.exe`: chỉ cần **Java 17+** và SQL Server (kèm `sqlcmd`) – không cần Maven

## Hai chế độ chạy: MOCK và JDBC

| | **MOCK** (dữ liệu giả) | **JDBC** (SQL Server) |
|---|---|---|
| Dữ liệu lưu ở đâu | Trong RAM – **tắt app là mất** | Database `DatVeXemPhim` trên SQL Server |
| Cần SQL Server? | Không | Có |
| Khi nào dùng | Code giao diện, chạy test, demo dự phòng | Chạy thật, nộp bài, demo CSDL |
| Bật khi | Không có `db.properties`, hoặc `app.mode=mock` | `db.properties` có `app.mode=jdbc` |

App đọc file cấu hình ở:
- chạy bằng **Maven / IDE**: `src/main/resources/db.properties` (bản được copy vào `target/classes`);
- chạy bằng **file exe**: `%APPDATA%\DatVeXemPhim\db.properties` (exe tự tạo lần đầu).

### App luôn cho biết đang chạy chế độ nào
Khi khởi động, app in ra console và gắn nhãn trên **màn hình đăng nhập** (dưới banner) và **màn hình chính**
(thanh tiêu đề, cạnh "Xin chào…"). Rê chuột vào nhãn để xem lý do.

```
==================================================
 Che do JDBC - da ket noi SQL Server (DatVeXemPhim)
 Ly do: app.mode=jdbc trong src/main/resources/db.properties - du lieu duoc doc/ghi vao SQL Server.
==================================================
```

| Nhãn | Ý nghĩa |
|---|---|
| ✔ nền xanh – *Chế độ JDBC – đã kết nối SQL Server* | Dữ liệu đọc/ghi vào SQL Server |
| ⚠ nền vàng – *Chế độ MOCK – dữ liệu giả trong RAM* | Không lưu vào SQL Server |
| ⚠ nền vàng – *Chế độ JDBC – KHÔNG kết nối được* | Đã đặt `app.mode=jdbc` nhưng không vào được SQL Server – app hiện thêm hộp cảnh báo kèm cách khắc phục |

> Khi SQL Server tắt, app có thể chờ vài giây lúc khởi động vì phải thử kết nối trước khi mở cửa sổ.

## Cách 1 – Chạy thử nhanh (không cần SQL Server)
```bash
mvn compile exec:java          # mở app, dùng dữ liệu giả
mvn test                       # chạy kiểm thử (dùng dữ liệu giả, không cần SQL Server)
mvn compile exec:java -Pdemo   # demo Dynamic SQL trên console
```
Tài khoản dữ liệu giả: `khach / 123456` (khách hàng), `admin / admin123` (nhân viên).

## Cách 2 – Chạy bằng Maven với SQL Server

App **không tự tạo database** khi chạy bằng Maven. `db.properties` nằm trong `.gitignore` nên vừa clone về
app luôn chạy MOCK. Mỗi người làm **một lần** các bước sau trên máy mình.

### Bước 1 – Cài phần mềm
- **SQL Server 2017+** bản Developer hoặc Express, chọn instance mặc định `MSSQLSERVER`.
- **SSMS** (SQL Server Management Studio).
- `sqlcmd` (script ở bước 3 cần lệnh này).

### Bước 2 – Tạo database bằng SSMS
1. Mở SSMS → Connect: Server name `localhost`, Authentication **Windows Authentication**,
   tick **Trust server certificate** → Connect.
2. **File → Open → File…**, mở `database/tao_csdl.sql` rồi bấm **Execute (F5)**.
   File gồm 3 phần chạy liền: tạo database + bảng → dữ liệu mẫu → `sp_DatVe`, trigger chống trùng ghế.
3. Chuột phải **Databases → Refresh**, thấy `DatVeXemPhim` là xong.

> ⚠ Chạy lại `tao_csdl.sql` sẽ **xóa hết bảng và dữ liệu** rồi tạo lại.

### Bước 3 – Tạo tài khoản kết nối và file `db.properties`
Mở **PowerShell bằng "Run as Administrator"** tại thư mục gốc dự án, chạy (không tham số):
```powershell
powershell -ExecutionPolicy Bypass -File database\tu_cau_hinh.ps1
```
Script sẽ (chạy lại nhiều lần vẫn an toàn; nếu bỏ qua bước 2 thì script tự tạo database):
- bật dịch vụ SQL Server, TCP/IP cổng **1433**, SQL Server Authentication (chỉ khởi động lại khi cần);
- tạo login SQL `datve_app` (mật khẩu ngẫu nhiên) có quyền trên `DatVeXemPhim`;
- ghi `src/main/resources/db.properties` với `app.mode=jdbc`;
- thử kết nối – thấy dòng `KetNoiTCP_OK` là thành công (nhật ký: `%TEMP%\datve_cau_hinh.log`).

<details>
<summary>Không chạy được script (instance có tên khác, dùng tài khoản <code>sa</code>…)? Tạo file bằng tay</summary>

Tạo file `src/main/resources/db.properties` với nội dung dưới, sửa `db.user` / `db.password`
(và `db.url` nếu instance không phải `localhost:1433`):
```properties
# mock = dung du lieu gia (khong can SQL Server) ; jdbc = ket noi SQL Server that
app.mode=jdbc
db.url=jdbc:sqlserver://localhost:1433;databaseName=DatVeXemPhim;encrypt=true;trustServerCertificate=true
db.user=sa
db.password=doi_mat_khau_o_day
```
Nhớ bật TCP/IP cổng 1433 và **SQL Server and Windows Authentication mode** trong SQL Server Configuration Manager / SSMS.
</details>

### Bước 4 – Mở app
```bash
mvn compile exec:java
```
Hoặc trong IDE chạy `com.nhom.datve.Main`. Thấy nhãn xanh **✔ Chế độ JDBC** là thành công.
Từ đó trở đi, mỗi lần mở app sẽ tự kết nối SQL Server.

Tài khoản mẫu trong database: `admin / admin123` (nhân viên), `khach1 / 123456` (khách hàng).

- Quay về dữ liệu giả: đặt `app.mode=mock` trong `db.properties`.
- Sửa `db.properties` xong phải chạy lại `mvn compile` (app đọc bản trong `target/classes`).

## Cách 3 – Chạy bằng run.exe (tự kết nối SQL Server)

### Dùng run.exe
Yêu cầu: máy có **Java 17+** và **SQL Server** (instance mặc định `MSSQLSERVER`) kèm `sqlcmd`.
`run.exe` **không** cài SQL Server hay Java.

1. Bấm đúp **`run.exe`** (thư mục gốc dự án; copy riêng file này sang máy khác cũng chạy được).
2. `run.exe` tự giải nén app vào `%LOCALAPPDATA%\DatVeXemPhim\run`, tự tìm Java 17+ (`JAVA_HOME`, `PATH`,
   registry, `Program Files`) rồi mở app ở chế độ **JDBC** – không hỏi gì thêm.
3. **Lần đầu trên máy** (hoặc khi mất kết nối), app tự chạy `tu_cau_hinh.ps1`; Windows chỉ hỏi quyền
   Administrator (UAC) → **Yes**. Script tự động:
   - bật dịch vụ SQL Server, TCP/IP cổng 1433 và SQL Server Authentication (chỉ khởi động lại SQL Server khi cần);
   - tạo database `DatVeXemPhim` từ `tao_csdl.sql` **nếu chưa có** (đã có thì giữ nguyên dữ liệu);
   - tạo login `datve_exe` và lưu cấu hình vào `%APPDATA%\DatVeXemPhim\db.properties` (`app.mode=jdbc`).
4. App mở với nhãn xanh **✔ Chế độ JDBC**. Từ lần sau bấm `run.exe` là vào thẳng.

Nếu cấu hình lỗi (từ chối UAC, chưa cài SQL Server…), app báo lý do và tạm chạy dữ liệu giả;
nhật ký ở `%APPDATA%\DatVeXemPhim\cau_hinh.log`. Mở lại `run.exe` sẽ tự thử cấu hình lại.

- Muốn cấu hình lại từ đầu: xóa thư mục `%APPDATA%\DatVeXemPhim` rồi mở `run.exe`.
- `run.exe` dùng login riêng `datve_exe`, **không** ảnh hưởng login `datve_app` / `db.properties` khi chạy bằng Maven.

### Tạo lại run.exe (sau khi sửa code)
Tại thư mục gốc dự án:
```powershell
powershell -ExecutionPolicy Bypass -File dong_goi_exe.ps1
```
Script build jar, bỏ `db.properties` ra khỏi jar (không lộ mật khẩu), nhúng jar + `tao_csdl.sql` + `tu_cau_hinh.ps1`
vào **một file** `run.exe` (~2,5 MB, biên dịch bằng `csc` có sẵn trong Windows), rồi dọn `target\`.

Thêm `-KemJava` để tạo thêm bản `dist\DatVeXemPhim\DatVeXemPhim.exe` **kèm sẵn Java** (~130 MB, ~270 file,
máy khác không cần cài Java – gửi cả thư mục `dist\DatVeXemPhim`).

## Xem dữ liệu của app trong SSMS
1. Mở SSMS → Connect: `localhost`, **Windows Authentication**, tick **Trust server certificate**.
2. **Databases → DatVeXemPhim → Tables** – có 14 bảng: `VaiTro`, `TaiKhoan`, `KhachHang`, `NhanVien`,
   `TheLoai`, `Phim`, `Phim_TheLoai`, `PhongChieu`, `LoaiGhe`, `Ghe`, `SuatChieu`, `DatVe`, `Ve`, `ThanhToan`.
3. Chuột phải một bảng (vd `dbo.DatVe`) → **Select Top 1000 Rows**.

Xem vé vừa đặt (**New Query**):
```sql
USE DatVeXemPhim;

SELECT dv.MaDatVe, dv.NgayDat, p.TenPhim, sc.GioBatDau,
       g.Hang + CAST(g.SoGhe AS VARCHAR) AS Ghe, v.GiaVe, v.TrangThai
FROM Ve v
JOIN DatVe dv     ON dv.MaDatVe = v.MaDatVe
JOIN SuatChieu sc ON sc.MaSuat  = v.MaSuat
JOIN Phim p       ON p.MaPhim   = sc.MaPhim
JOIN Ghe g        ON g.MaGhe    = v.MaGhe
ORDER BY dv.NgayDat DESC;
```
Đặt vé trong app rồi chạy lại truy vấn (F5) sẽ thấy đơn mới – nếu không thấy, kiểm tra nhãn trong app có phải **JDBC** không.
Thêm truy vấn mẫu trong `database/truy_van.sql`.

### Dữ liệu thật nằm ở đâu?
- Các file `.sql` trong `database/` chỉ là **lệnh tạo** database, không chứa dữ liệu app ghi vào.
- SQL Server lưu dữ liệu trong `DatVeXemPhim.mdf` (dữ liệu) và `DatVeXemPhim_log.ldf` (nhật ký giao dịch) ở thư mục
  `DATA` của SQL Server, vd `C:\Program Files\Microsoft SQL Server\MSSQL17.MSSQLSERVER\MSSQL\DATA\`
  (xem đường dẫn trên máy mình: `SELECT physical_name FROM sys.master_files WHERE database_id = DB_ID('DatVeXemPhim')`).
- **Đừng copy trực tiếp** 2 file này khi SQL Server đang chạy. Muốn chuyển sang máy khác: SSMS → chuột phải database →
  **Tasks → Back Up…** tạo file `.bak`, rồi **Restore Database** ở máy kia.

## Gặp lỗi?
| Hiện tượng | Cách xử lý |
|---|---|
| Nhãn MOCK dù đã cấu hình (Maven) | Kiểm tra `src/main/resources/db.properties` có `app.mode=jdbc`; sửa xong chạy lại `mvn compile` |
| Nhãn MOCK khi chạy `run.exe` | Lần đầu đã từ chối UAC hoặc cấu hình lỗi → mở lại `run.exe` để thử lại; xem `%APPDATA%\DatVeXemPhim\cau_hinh.log` |
| `run.exe` báo không tìm thấy Java | Cài JDK 17+ (vd Eclipse Temurin) hoặc đặt biến `JAVA_HOME` |
| *JDBC – KHÔNG kết nối được* | `services.msc` → **SQL Server (MSSQLSERVER)** phải **Running**; kiểm tra TCP/IP cổng 1433; sai mật khẩu thì chạy lại bước 3 (Maven) hoặc mở lại exe để cấu hình lại |
| `Cannot open database "DatVeXemPhim"` | Chưa tạo database (bước 2, hoặc chạy `database\tu_cau_hinh.ps1`) |
| `run.exe` báo cấu hình chưa thành công | Đọc `%APPDATA%\DatVeXemPhim\cau_hinh.log` (thiếu `sqlcmd`, chưa cài SQL Server, từ chối quyền Admin…) |
| Không thấy `DatVeXemPhim` trong SSMS | Chuột phải **Databases → Refresh**; vẫn không có thì chưa chạy script tạo database |

## Lưu ý bảo mật
- **Không commit `db.properties`** – file chứa mật khẩu, đã nằm trong `.gitignore`.
- `mvn package` đóng `db.properties` vào file `target/*.jar` → không gửi jar đó ra ngoài nhóm
  (`run.exe` tạo bằng `dong_goi_exe.ps1` đã tự bỏ file này ra).
- Không chia sẻ `%APPDATA%\DatVeXemPhim\db.properties`.

## Cấu trúc thư mục
Toàn bộ dự án gọn trong **~20 file**. Mọi lớp Java nằm chung package `com.nhom.datve`, gom theo tầng vào 9 file:
```
DatVeXemPhim/
├── pom.xml                  (JDK 17, mssql-jdbc, FlatLaf, JUnit 5)
├── README.md                tài liệu này
├── run.exe                  ▶ bấm đúp để chạy app (tự kết nối SQL Server, chế độ JDBC)
├── dong_goi_exe.ps1         tạo lại run.exe sau khi sửa code
├── .gitignore
├── database/                (Khánh)
│   ├── tao_csdl.sql         tạo database + bảng, dữ liệu mẫu, procedure/trigger (chạy 1 lần)
│   ├── truy_van.sql         view + truy vấn minh họa cho báo cáo
│   └── tu_cau_hinh.ps1      cấu hình SQL Server + db.properties (Maven: datve_app, exe: datve_exe)
├── docs/
│   ├── ERD_DatVeXemPhim.drawio   ERD 4 trang
│   ├── ERD_DatVeXemPhim.png
│   ├── ERD_DatVeXemPhim_v2.drawio  ERD mới: trang 1 ký hiệu Chen, trang 2 lược đồ quan hệ
│   ├── anh_man_hinh.png          ảnh ghép 15 màn hình (chế độ JDBC)
│   └── bao_cao/                  phần bổ sung báo cáo: BaoCao_BoSung.md + ảnh OOP, ảnh CSDL
├── src/main/resources/db.properties   cấu hình kết nối của máy mình (không commit)
├── src/main/java/com/nhom/datve/
│   ├── Main.java            khởi động + tiện ích: AppConfig, CheDoChay, DBConnection, TuCauHinh,
│   │                        PasswordUtil, PhienDangNhap, Validator, NghiepVuException, DataAccessException
│   ├── Model.java           lớp thực thể, enum trạng thái, BoLocTimKiem, KetQuaTimKiem
│   ├── DAO.java             interface DAO, DAOFactory, TimKiemSql, JdbcHelper + bản JDBC <Interface>Impl
│   ├── MockDAO.java         bản dữ liệu giả Mock<Interface>, MockData
│   ├── Service.java         DatVeService, PhimService, TaiKhoanService, TimKiemService, SoDoGhe
│   ├── GiaoDien.java        Theme, LoginFrame, DangKyDialog, MainFrame, ManHinh, DieuHuong...
│   ├── GiaoDienKhachHang.java  màn hình khách hàng (Trung) + TimKiemPhimPanel, XacNhanDatVeDialog (Khuê)
│   ├── GiaoDienAdmin.java   màn hình quản trị (Khánh)
│   └── DynamicSqlDemo.java  demo Dynamic SQL bản console
└── src/test/java/com/nhom/datve/KiemThuTest.java   toàn bộ kiểm thử JUnit
```
Thư mục `target/` (Maven) và `dist/` (bản exe kèm Java, `dong_goi_exe.ps1 -KemJava`) **tự sinh ra khi build**,
không cần giữ – xóa thoải mái.
Mỗi lớp cũ giữ nguyên tên và code, chỉ được gom vào file; trong file có dòng tiêu đề
`// ===== TenLop (truoc day: ...)` để tìm nhanh (Ctrl+F tên lớp).

| Lớp tiện ích (trong `Main.java`) | Việc |
|---|---|
| `AppConfig` | Đọc `db.properties` (classpath khi chạy Maven, `%APPDATA%` khi chạy exe); `reload()` để đọc lại |
| `DBConnection` | Singleton giữ thông tin kết nối; `getConnection()`, `testConnection()`, `reset()` |
| `CheDoChay` | Xác định MOCK/JDBC, thử kết nối 1 lần lúc khởi động, tạo dòng thông báo + lý do |
| `TuCauHinh` | Chỉ dùng cho exe: hỏi người dùng rồi chạy `tu_cau_hinh.ps1` với quyền Admin |

## Giao diện Swing – gắn màn hình của mình vào MainFrame
`MainFrame` là cửa sổ duy nhất sau khi đăng nhập. Phần giữa dùng **CardLayout**, menu (**JMenuBar**) đổi theo vai trò: khách hàng thấy *Đặt vé*, *Vé của tôi*; nhân viên thấy *Quản trị*.

1. Tạo lớp đúng **tên** đã khai báo trong enum `ManHinh` (file `GiaoDien.java`), đặt trong `GiaoDienKhachHang.java` hoặc `GiaoDienAdmin.java`. Lớp kế thừa `JPanel`, có constructor nhận `DieuHuong`:
   ```java
   class SoDoGhePanel extends JPanel implements ManHinhCon {
       private final DieuHuong dieuHuong;
       public SoDoGhePanel(DieuHuong dieuHuong) { this.dieuHuong = dieuHuong; ... }
       @Override public void khiHienThi(Object thamSo) { int maSuat = (Integer) thamSo; /* nạp sơ đồ */ }
   }
   ```
2. Không cần sửa MainFrame: lớp nào chưa có, MainFrame hiện màn hình "đang làm" thay thế.
3. Chuyển màn hình: `dieuHuong.chuyenDen(ManHinh.SO_DO_GHE, maSuat)`; nút quay lại: `dieuHuong.quayLai()`.
4. Tham số quy ước: `CHON_SUAT_CHIEU` nhận `Integer maPhim`, `SO_DO_GHE` nhận `Integer maSuat`, các màn hình khác nhận `null`.
5. Người đang đăng nhập: `dieuHuong.getPhien()` (`getMaKH()`, `isAdmin()`, `getTenHienThi()`).
6. **Đăng nhập (Trung):** lớp `com.nhom.datve.LoginFrame` (constructor không tham số). `Main` tự dùng lớp này thay cho `DangNhapTamDialog`. Đăng nhập thành công thì gọi `new TaiKhoanService().dangNhap(ten, mk)` → `new MainFrame(phien).setVisible(true); dispose();`.
7. **Đặt vé (Trung → Khuê):** trên sơ đồ ghế, nút "Đặt vé" gọi:
   ```java
   Optional<DatVe> kq = XacNhanDatVeDialog.hienThi(dieuHuong.getCuaSo(),
           dieuHuong.getPhien().getMaKH(), soDo.getSuatChieu(), gheDangChon);
   if (kq.isPresent()) dieuHuong.chuyenDen(ManHinh.LICH_SU_VE);
   else napLaiSoDo();   // có thể ghế vừa bị người khác đặt
   ```

**Theme dùng chung** (lớp `Theme` trong `GiaoDien.java`): `Theme.nutChinh("Lưu")`, `Theme.nutPhu("Hủy")`, `Theme.tieuDe(...)`, `Theme.dinhDangBang(table)`, `Theme.tien(soTien)` → "75.000 đ", `Theme.ngayGio(t)`, `Theme.nhanCheDo()` (nhãn MOCK/JDBC), màu ghế `Theme.GHE_TRONG / GHE_DANG_CHON / GHE_DA_DAT / GHE_VIP / GHE_DOI`. Không tự đặt màu/font riêng.

**Bắt buộc với mọi màn hình:** dùng layout manager (không `setLayout(null)`); thao tác CSDL chạy trong `SwingWorker`; lỗi hiện bằng `JOptionPane`; lớp giao diện không viết SQL.

## Quy ước code
1. **Phân tầng một chiều: `ui → service → dao (interface) → CSDL`.** Giao diện không viết SQL, không `new ...DAOImpl()`. Lấy DAO qua `DAOFactory`.
2. **Tên lớp:** không dấu, PascalCase, trùng tên bảng. Interface `XxxDAO`, bản JDBC `XxxDAOImpl` trong `DAO.java`, bản giả `MockXxxDAO` trong `MockDAO.java` – đặt đúng tên thì `DAOFactory` tự nhận.
3. **Tên biến/phương thức:** camelCase tiếng Việt không dấu. Chữ hiển thị cho người dùng viết tiếng Việt có dấu.
4. **JDBC:** luôn `PreparedStatement` với `?`; mở kết nối bằng try-with-resources.
5. **Lỗi:** nghiệp vụ → `NghiepVuException` (UI hiện `JOptionPane`); CSDL → `DataAccessException`.
6. **Chế độ mock:** chưa có `db.properties` hoặc `app.mode=mock` thì chạy dữ liệu giả; ở chế độ `jdbc`, DAO nào chưa có `Impl` tạm dùng bản mock (in cảnh báo `[DAOFactory]` ra console). Hiện cả 13 DAO đều đã có bản `Impl`. Muốn biết app đang chạy chế độ nào, dùng `CheDoChay` – không tự đọc `app.mode`.
7. **Không commit:** `target/`, `dist/`, file cấu hình IDE, `db.properties`.

## Demo chương 10 – Dynamic SQL trên giao diện
Màn hình **Đặt vé → Tìm kiếm phim** có khung *Demo chương 10*:
1. Chọn *PreparedStatement*, bấm "Điền chuỗi tấn công", bấm Tìm kiếm → không ra kết quả, khung SQL cho thấy câu lệnh cố định và chuỗi tấn công chỉ nằm trong danh sách tham số.
2. Chọn *Nối chuỗi bằng Statement*, bấm Tìm kiếm lại → chuỗi `' UNION SELECT ... FROM TaiKhoan --` bị ghép vào SQL, bảng kết quả hiện **tên đăng nhập và mật khẩu đã băm** của mọi tài khoản.
3. Gõ một tên có dấu nháy, ví dụ `Ngôi Nhà O'Brien`, ở chế độ nối chuỗi → SQL Server báo lỗi cú pháp, chế độ PreparedStatement thì chạy bình thường.

Bước 2–3 cần SQL Server (`app.mode=jdbc`); ở chế độ dữ liệu giả màn hình chỉ hiện câu SQL được tạo.

## Quy trình Git
- Không push thẳng lên `main`. Mỗi việc một nhánh: `ten/viec-lam`, ví dụ `khanh/phim-dao`, `trung/so-do-ghe`.
- Commit nhỏ, thông điệp rõ. Xong việc → push nhánh → Pull Request vào `main` → Khuê review và merge.
- Trước khi làm việc mới: `git checkout main && git pull` rồi mới tạo nhánh.

## Tài liệu
- `docs/ERD_DatVeXemPhim.drawio` (mở bằng https://app.diagrams.net): trang 1 ERD, trang 2 lược đồ quan hệ, trang 3 phụ thuộc hàm, trang 4 các bước chuẩn hóa.
- `docs/ERD_DatVeXemPhim_v2.drawio`: ERD vẽ lại – trang 1 ERD ký hiệu Chen (13 thực thể, 15 liên kết), trang 2 lược đồ quan hệ 14 bảng (chân quạ).
- `docs/anh_man_hinh.png`: ảnh ghép 15 màn hình chụp ở chế độ JDBC; ảnh riêng từng màn hình ở `docs/bao_cao/anh_oop/`.
- `docs/bao_cao/BaoCao_BoSung.md`: phần bổ sung báo cáo – mô hình quan hệ, phụ thuộc hàm & chuẩn hóa, ảnh 14 bảng + dữ liệu, 15 truy vấn, 7 kiểm thử thủ tục/trigger (`docs/bao_cao/anh_csdl/`).
- Mục [Quy ước CSDL](#quy-ước-csdl) bên dưới: quy ước giữa code Java và CSDL (tên bảng, `sp_DatVe`, mã lỗi).

## Quy ước CSDL
Lược đồ chi tiết (kiểu dữ liệu, khóa) xem trang 2 của `docs/ERD_DatVeXemPhim.drawio`.

### Giá trị trạng thái (code Java đọc đúng các chuỗi này)
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

### Chống trùng ghế
Ghế đã hủy phải đặt lại được, nên dùng **chỉ mục UNIQUE có lọc** thay vì UNIQUE thường:
```sql
CREATE UNIQUE INDEX UX_Ve_Suat_Ghe ON Ve(MaSuat, MaGhe) WHERE TrangThai = 'HIEU_LUC';
```
Trigger của Khánh kiểm tra thêm: ghế phải thuộc đúng phòng của suất chiếu.

### Giao ước `sp_DatVe` (Java gọi trong `DatVeDAOImpl`)
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

### Yêu cầu phiên bản
Màn hình tìm kiếm phim (`TimKiemSql`) dùng `STRING_AGG` để gộp tên thể loại, nên cần **SQL Server 2017 trở lên**.

### `fn_DoanhThu`
`ThongKeDAO` dự kiến gọi `fn_DoanhThu(@TuNgay DATE, @DenNgay DATE)` trả về bảng doanh thu, chỉ tính `ThanhToan.TrangThai = 'THANH_CONG'`. Khánh chốt chữ ký hàm rồi cập nhật lại mục này.

## Nhật ký cập nhật

### Tài liệu báo cáo bổ sung
- Chụp lại 15 màn hình ở chế độ JDBC (bỏ ảnh MOCK cũ có màn hình tạm "chưa có lớp DanhSachPhimPanel").
- Thêm `docs/ERD_DatVeXemPhim_v2.drawio` và `docs/bao_cao/` (mô hình quan hệ, chuẩn hóa, ảnh kết quả SQL chạy thật).
- Sửa hiển thị: ký tự 🎬, ✔, ⚠ và `──` hiện thành ô vuông (font Segoe UI không có) → bỏ/đổi sang ký tự có sẵn; nút ghế ở màn hình Phòng chiếu bị cắt thành "..." → giảm lề nút.

### run.exe – chạy 1 chạm ở chế độ JDBC
- `dong_goi_exe.ps1` giờ tạo `run.exe` ở thư mục gốc: 1 file, nhúng sẵn jar + script CSDL, tự tìm Java 17+.
- `TuCauHinh`: thêm chế độ tự động (`-Ddatve.tudong=true`, `run.exe` bật sẵn) – không hỏi Yes/No, luôn cấu hình để chạy JDBC.

### Rút gọn file (~580 → ~20 file)
- Gom 105 file `.java` vào 9 file theo tầng (cùng package `com.nhom.datve`), 4 file test vào `KiemThuTest.java`. Code từng lớp giữ nguyên.
- Gom `01_tao_bang.sql`, `02_du_lieu_mau.sql`, `03_procedure_trigger.sql` thành `tao_csdl.sql`; `04_truy_van.sql` → `truy_van.sql`.
- `setup_ket_noi.ps1` gộp vào `tu_cau_hinh.ps1` (chạy không tham số = cấu hình cho Maven).
- `database/README.md`, `db.properties.example` gộp vào README; 6 ảnh màn hình ghép thành `docs/anh_man_hinh.png`.
- Xóa file build trong `target/`, `dist/` (tạo lại bằng `mvn package` / `dong_goi_exe.ps1`).

### Thông báo chế độ chạy MOCK / JDBC
- Thêm `util/CheDoChay.java`: lúc khởi động xác định chế độ, thử kết nối SQL Server 1 lần, in dòng thông báo ra console.
- Thêm `Theme.nhanCheDo()`: nhãn xanh/vàng trên `LoginFrame` và `MainFrame` (thay chữ "dữ liệu giả" cũ chỉ hiện ở chế độ mock).
- `Main.java`: hiện hộp cảnh báo khi đặt `app.mode=jdbc` nhưng không kết nối được.

### File exe tự cấu hình JDBC
- Thêm `dong_goi_exe.ps1`: build jar, bỏ `db.properties` ra khỏi jar, đóng gói bằng `jpackage` thành `dist\DatVeXemPhim\` (kèm Java).
- Thêm `database/tu_cau_hinh.ps1`: bật SQL Server + TCP 1433 + SQL Server Authentication, tạo database nếu chưa có, tạo login `datve_exe`, ghi cấu hình vào `%APPDATA%\DatVeXemPhim\`.
- Thêm `util/TuCauHinh.java`: khi chạy exe mà chưa cấu hình/không kết nối được thì hỏi người dùng và chạy script trên với quyền Admin.
- `AppConfig`: bản exe đọc cấu hình ở `%APPDATA%\DatVeXemPhim\db.properties`; thêm `reload()`. `DBConnection`: thêm `reset()`.
- `.gitignore`: thêm `dist/`.

### README
- Viết lại hướng dẫn cài đặt SQL Server từng bước (đúng thứ tự: tạo database **trước**, rồi mới chạy `setup_ket_noi.ps1`).
- Thêm hướng dẫn xem dữ liệu trong SSMS, vị trí file dữ liệu `.mdf`/`.ldf`, bảng xử lý lỗi, lưu ý bảo mật.
