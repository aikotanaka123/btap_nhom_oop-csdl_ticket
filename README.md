# Ứng dụng đặt vé xem phim

Bài tập nhóm môn **Cơ sở dữ liệu** và **Lập trình hướng đối tượng (Java)**.
Ứng dụng desktop **Java Swing + Maven + SQL Server**, kết nối qua **JDBC**.

| Thành viên | Vai trò |
|---|---|
| Bùi Minh Khuê (N24DCAT042) | Trưởng nhóm – thiết kế CSDL, khung project, MainFrame, tìm kiếm phim, xác nhận đặt vé |
| Khánh | Script CSDL, stored procedure/trigger, các lớp DAO JDBC, màn hình Admin |
| Trung | Model OOP, màn hình khách hàng, kiểm tra dữ liệu, báo cáo OOP |

## Yêu cầu
- JDK 17 trở lên, Maven 3.8+ (IntelliJ/Eclipse/NetBeans đã có sẵn Maven)
- SQL Server **2017+** (màn hình tìm kiếm dùng `STRING_AGG`) – chỉ cần khi chạy chế độ `jdbc`

## Chạy thử nhanh (chưa cần SQL Server)
```bash
mvn compile exec:java          # mở app, dùng dữ liệu giả
mvn test                       # chạy kiểm thử
mvn compile exec:java -Pdemo   # demo Dynamic SQL trên console
```
Tài khoản dữ liệu giả: `khach / 123456` (khách hàng), `admin / admin123` (nhân viên).

## Kết nối SQL Server
1. Chạy lần lượt các script trong `database/` bằng SSMS.
2. Copy `src/main/resources/db.properties.example` thành `db.properties` (cùng thư mục), sửa user/mật khẩu và đổi `app.mode=jdbc`.
3. `db.properties` nằm trong `.gitignore` nên **không bị đẩy lên GitHub** – mỗi người tự tạo trên máy mình.

## Cấu trúc thư mục
```
DatVeXemPhim/
├── pom.xml                (JDK 17, mssql-jdbc, FlatLaf, JUnit 5)
├── database/              script SQL (Khánh)
├── docs/                  ERD .drawio (4 trang), ảnh chụp màn hình
└── src/main/java/com/nhom/datve/
    ├── Main.java          khởi động: Theme → đăng nhập → MainFrame
    ├── model/             lớp thực thể + enum trạng thái + BoLocTimKiem, KetQuaTimKiem
    ├── dao/               interface DAO, DAOFactory, TimKiemSql
    │   ├── impl/          bản JDBC: <Interface>Impl
    │   └── mock/          bản dữ liệu giả: Mock<Interface>
    ├── service/           DatVeService, TimKiemService, TaiKhoanService
    ├── ui/                MainFrame, Theme, ManHinh, DieuHuong, ManHinhCon
    │   ├── khachhang/     màn hình khách hàng (Trung) + TimKiemPhimPanel, XacNhanDatVeDialog (Khuê)
    │   └── admin/         màn hình quản trị (Khánh)
    ├── util/              DBConnection (Singleton), AppConfig, PasswordUtil, PhienDangNhap
    ├── exception/         NghiepVuException, DataAccessException
    └── demo/              DynamicSqlDemo (bản console)
```

## Giao diện Swing – gắn màn hình của mình vào MainFrame
`MainFrame` là cửa sổ duy nhất sau khi đăng nhập. Phần giữa dùng **CardLayout**, menu (**JMenuBar**) đổi theo vai trò: khách hàng thấy *Đặt vé*, *Vé của tôi*; nhân viên thấy *Quản trị*.

1. Tạo lớp đúng **tên và package** đã khai báo trong `ui/ManHinh.java`, ví dụ `ui/khachhang/SoDoGhePanel.java`. Lớp kế thừa `JPanel`, có constructor nhận `DieuHuong`:
   ```java
   public class SoDoGhePanel extends JPanel implements ManHinhCon {
       private final DieuHuong dieuHuong;
       public SoDoGhePanel(DieuHuong dieuHuong) { this.dieuHuong = dieuHuong; ... }
       @Override public void khiHienThi(Object thamSo) { int maSuat = (Integer) thamSo; /* nạp sơ đồ */ }
   }
   ```
2. Không cần sửa MainFrame: lớp nào chưa có, MainFrame hiện màn hình "đang làm" thay thế.
3. Chuyển màn hình: `dieuHuong.chuyenDen(ManHinh.SO_DO_GHE, maSuat)`; nút quay lại: `dieuHuong.quayLai()`.
4. Tham số quy ước: `CHON_SUAT_CHIEU` nhận `Integer maPhim`, `SO_DO_GHE` nhận `Integer maSuat`, các màn hình khác nhận `null`.
5. Người đang đăng nhập: `dieuHuong.getPhien()` (`getMaKH()`, `isAdmin()`, `getTenHienThi()`).
6. **Đăng nhập (Trung):** tạo `com.nhom.datve.ui.LoginFrame` (constructor không tham số). `Main` tự dùng lớp này thay cho `DangNhapTamDialog`. Đăng nhập thành công thì gọi `new TaiKhoanService().dangNhap(ten, mk)` → `new MainFrame(phien).setVisible(true); dispose();`.
7. **Đặt vé (Trung → Khuê):** trên sơ đồ ghế, nút "Đặt vé" gọi:
   ```java
   Optional<DatVe> kq = XacNhanDatVeDialog.hienThi(dieuHuong.getCuaSo(),
           dieuHuong.getPhien().getMaKH(), soDo.getSuatChieu(), gheDangChon);
   if (kq.isPresent()) dieuHuong.chuyenDen(ManHinh.LICH_SU_VE);
   else napLaiSoDo();   // có thể ghế vừa bị người khác đặt
   ```

**Theme dùng chung** (`ui/Theme.java`): `Theme.nutChinh("Lưu")`, `Theme.nutPhu("Hủy")`, `Theme.tieuDe(...)`, `Theme.dinhDangBang(table)`, `Theme.tien(soTien)` → "75.000 đ", `Theme.ngayGio(t)`, màu ghế `Theme.GHE_TRONG / GHE_DANG_CHON / GHE_DA_DAT / GHE_VIP / GHE_DOI`. Không tự đặt màu/font riêng.

**Bắt buộc với mọi màn hình:** dùng layout manager (không `setLayout(null)`); thao tác CSDL chạy trong `SwingWorker`; lỗi hiện bằng `JOptionPane`; lớp giao diện không viết SQL.

## Quy ước code
1. **Phân tầng một chiều: `ui → service → dao (interface) → CSDL`.** Giao diện không viết SQL, không `new ...DAOImpl()`. Lấy DAO qua `DAOFactory`.
2. **Tên lớp:** không dấu, PascalCase, trùng tên bảng. Interface `XxxDAO`, bản JDBC `XxxDAOImpl` trong `dao/impl`, bản giả `MockXxxDAO` trong `dao/mock` – đặt đúng tên thì `DAOFactory` tự nhận.
3. **Tên biến/phương thức:** camelCase tiếng Việt không dấu. Chữ hiển thị cho người dùng viết tiếng Việt có dấu.
4. **JDBC:** luôn `PreparedStatement` với `?`; mở kết nối bằng try-with-resources.
5. **Lỗi:** nghiệp vụ → `NghiepVuException` (UI hiện `JOptionPane`); CSDL → `DataAccessException`.
6. **Chế độ mock:** chưa có `db.properties` hoặc `app.mode=mock` thì chạy dữ liệu giả; ở chế độ `jdbc`, DAO nào chưa có `Impl` tạm dùng bản mock.
7. **Không commit:** `target/`, file cấu hình IDE, `db.properties`.

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
- `docs/anh-man-hinh/`: ảnh chụp các màn hình để đưa vào báo cáo.
- `database/README.md`: quy ước giữa code Java và CSDL (tên bảng, `sp_DatVe`, mã lỗi).
