# Cinema Ticket Web App



## Mục lục
- Giới thiệu
- Tính năng
- Công nghệ
- Kiến trúc hệ thống
- Cài đặt & chạy dự án
- Biến môi trường
- Khởi tạo cơ sở dữ liệu
- Thiết lập ngrok cho VNPay IPN


## 1.Giới thiệu
Trong thời đại công nghệ kỹ xảo ngày càng phát triển, nhu cầu xem phim của khán giả cũng không ngừng 
tăng cao, đặc biệt là với những trải nghiệm chân thực, sống động tại rạp chiếu. Điều này thúc đẩy
sự cần thiết của một hệ thống đặt vé tiện lợi, nhanh chóng và thân thiện với người dùng. Dự án xây 
dựng một hệ thống không chỉ tiện lợi đối với người dùng mà còn giúp ích rất nhiều công việc quản lý 
của rạp chiếu, giảm chi phí thuê nhân công mà hiệu suất công việc lại vượt trội.


## 2.Tính năng
**Khách hàng:**
- Đăng nhập, đăng ký, đặt lại mật khẩu.
- Quản lý thông tin cá nhân.
- Tìm kiếm phim
- Xem thông tin giới thiệu về phim 
- Đặt vé xem phim và đồ ăn theo từng rạp, từng ghế và suất chiếu.
- Thanh toán trực tuyến qua VNPay, chuyển nhượng vé, kết bạn và trò chuyện realtime.


**Quản trị viên**
- Quản lý tài khoản khách hàng (thay đổi một số thông tin cho phép, tích điểm, tạo xóa tài khoản...)
- Quản lý thông tin rạp (Địa chỉ, phòng chiếu, ghế ngồi)
- Quản lý phim, suất chiếu theo rạp


## 3.Công nghệ
- **Backend:** Spring Boot 3.4, Java 21, Spring Security (JWT/OAuth2 Resource Server), JPA/Hibernate, Resilience4j (circuit breaker), SpringDoc OpenAPI.
- **Frontend:** React 18, Vite 5, Redux Toolkit, Ant Design, TailwindCSS/SASS, STOMP/SockJS (chat & cập nhật ghế realtime).
- **Database:** MySQL 8.
- **Hạ tầng:** Redis Stack (cache + RediSearch), RabbitMQ (phát vé sau thanh toán), Cloudinary (lưu ảnh), VNPay (thanh toán), Docker Compose, ngrok (expose IPN).


## 4.Kiến trúc hệ thống
**Hệ thống chia làm hai phần:**
- Front-End (FE): Giao diện người dùng, gọi API đến server.
- Back-End (BE): Server Spring Boot xác thực người dùng, xử lý logic nghiệp vụ, kết nối CSDL 
và cung cấp API cho FE.
- BE sử dụng RESTful API và bảo mật thông qua JWT. Người dùng được phân quyền theo mô hình role-permission.
  Dữ liệu được lưu trữ trong MySQL, dùng Redis cho cache/tìm kiếm và RabbitMQ cho xử lý bất đồng bộ.


## 5.Cài đặt & chạy dự án

### Cách 1 — Docker Compose (khuyến nghị)
Toàn bộ stack (MySQL, Redis Stack, RabbitMQ, Backend, Frontend, ngrok) được đóng gói trong `docker-compose.yml`.

```bash
# 1. Clone repository
git clone <repo-url>
cd CinemaManagementWeb

# 2. Tạo file cấu hình biến môi trường (xem mục 6)
cp Backend/.env.example Backend/.env
#   rồi điền giá trị thật vào Backend/.env
#   tạo thêm file .env ở thư mục gốc cho ngrok (NGROK_AUTHTOKEN, NGROK_DOMAIN)

# 3. Build & chạy toàn bộ
docker-compose up -d --build

# 4. Xem log / dừng
docker-compose logs -f backend
docker-compose down
```

Sau khi chạy:
- Frontend: http://localhost (cổng 80)
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- RabbitMQ Management: http://localhost:15672 (guest/guest)
- MySQL: localhost:3307 (database `cinemaweb`)
- ngrok inspector: http://localhost:4040

> Lưu ý: container `mysql` tự nạp `database/cinema.sql` ở lần khởi tạo đầu tiên (xem mục 7).

### Cách 2 — Chạy thủ công (dev)
Cần có sẵn MySQL, Redis Stack và RabbitMQ chạy cục bộ.

**Back-End**
```bash
cd Backend
# tạo Backend/.env từ Backend/.env.example và điền giá trị
./mvnw clean install
./mvnw spring-boot:run
```

**Front-End**
```bash
cd Frontend
npm install
npm run dev      # chạy dev server Vite (mặc định http://localhost:5173)
npm run build    # build production ra thư mục dist/
```


## 6.Biến môi trường
Backend đọc cấu hình từ `Backend/.env` (tham chiếu mẫu đầy đủ tại `Backend/.env.example`).
Các biến của ngrok đặt ở file `.env` tại thư mục gốc (được docker-compose nạp tự động).

| Biến | Bắt buộc | Mô tả |
|------|:---:|------|
| `DB_URL` | ✓ | JDBC URL tới MySQL (mặc định `jdbc:mysql://localhost:3307/cinemaweb`) |
| `DB_USERNAME` / `DB_PASSWORD` | ✓ | Tài khoản MySQL |
| `MAIL_HOST` / `MAIL_PORT` | ✓ | SMTP host/port (vd Brevo) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | ✓ | Thông tin đăng nhập SMTP |
| `MAIL_FROM` | ✓ | Địa chỉ "From" đã verify (khác `MAIL_USERNAME`) |
| `JWT_SIGNER_KEY` | ✓ | Khoá ký HMAC cho JWT |
| `VNPAY_SECRET_KEY` / `VNPAY_TMN_CODE` | ✓ | Thông tin merchant VNPay |
| `VNPAY_RETURN_URL` | ✓ | URL VNPay redirect sau thanh toán |
| `TMDB_KEY` | ✓ | API key TMDB (import phim) |
| `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | ✓ | Lưu trữ ảnh trên Cloudinary |
| `CORS_ALLOWED_ORIGINS` | – | Origin FE được phép gọi API, nhiều origin cách nhau bằng dấu phẩy (mặc định `http://localhost:5173`) |
| `REDIS_HOST` / `REDIS_PORT` | – | Mặc định `localhost:6379` (compose tự đặt `redis`) |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` / `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | – | Mặc định `localhost:5672`, `guest/guest` |
| `NGROK_AUTHTOKEN` / `NGROK_DOMAIN` | – | Đặt ở `.env` gốc; chỉ cần khi dùng ngrok cho VNPay IPN (mục 8) |

> File `.env` đã được `.gitignore` — không commit credentials thật lên git.


## 7.Khởi tạo cơ sở dữ liệu
- Schema gốc (source of truth): `database/cinema.sql`. Hibernate chạy với `ddl-auto: none` nên **không** tự sinh bảng — schema phải được tạo trước.
- Khi dùng Docker Compose, file này tự được nạp vào container MySQL ở lần khởi tạo đầu tiên (mount vào `/docker-entrypoint-initdb.d`). Nếu cần nạp lại sạch: `docker-compose down -v` rồi `up` lại.
- Khi chạy thủ công, import bằng tay:
  ```bash
  mysql -h 127.0.0.1 -P 3307 -u root -p cinemaweb < database/cinema.sql
  ```
- Các thay đổi schema bổ sung nằm ở `database/migration_fixes.sql` — chạy thủ công sau `cinema.sql` nếu cập nhật từ phiên bản cũ.


## 8.Thiết lập ngrok cho VNPay IPN
VNPay gọi IPN (Instant Payment Notification) theo cơ chế server-to-server, nên backend phải có URL public truy cập được từ internet. Dự án dùng ngrok với domain tĩnh.

1. Đăng ký tài khoản tại https://dashboard.ngrok.com và lấy `NGROK_AUTHTOKEN`.
2. Tạo một static domain (gói free có 1 domain) và lấy giá trị `NGROK_DOMAIN`.
3. Tạo file `.env` ở thư mục gốc dự án:
   ```bash
   NGROK_AUTHTOKEN=<token-cua-ban>
   NGROK_DOMAIN=<your-domain>.ngrok-free.dev
   ```
4. Khai báo URL IPN trên VNPay portal trỏ tới: `https://<NGROK_DOMAIN>/api/payment/vnpay-ipn`.
5. Sau khi `docker-compose up`, mở http://localhost:4040 để theo dõi request IPN khi debug.

> Dùng static domain giúp URL public cố định, không phải cập nhật lại IPN URL trên VNPay mỗi lần restart.
