# WORKLOG

## [2026-06-20 23:18] Phân tích và đánh giá luồng nghiệp vụ dự án
- **Đã làm:**
  - Khảo sát toàn bộ cấu trúc thư mục dự án (Spring Boot backend + React/Vite frontend + MySQL/Redis), xác định 16 controller, các entity, service, enum, scheduler, repository.
  - Đọc và phân tích song song các file backend cốt lõi:
    - `AuthenticationController.java` + `AuthenticationService.java`: luồng login/logout/refresh token, cơ chế rotation + whitelist Redis, reuse detection, grace window.
    - `BookingController.java` + `BookingService.java`: luồng đặt vé, xử lý ghế, food & drink.
    - `PaymentController.java`: tích hợp VNPay (tạo URL + callback).
    - `ScheduleController.java` + `ScheduleService.java`: tạo lịch chiếu và khởi tạo `seat_schedule`.
    - `SeatScheduleService.java`: truy vấn danh sách ghế theo suất chiếu.
    - `UserService.java`: đăng ký, cập nhật thông tin, điểm tích lũy.
    - `SecurityConfig.java`: cấu hình `PUBLIC_ENDPOINTS`, CORS, JWT filter.
    - `ScheduledTasks.java`: scheduler dọn OTP hết hạn.
    - Entity `Booking.java`, `SeatSchedule.java`; DTO `BookingRequest.java`.
  - Đọc và phân tích frontend:
    - `BookingTicket.jsx`: chọn ghế, gọi `DatVe` trực tiếp hoặc lưu localStorage rồi chuyển trang combo.
    - `BookingFoodAndDrink.jsx`: chọn combo, gọi `DatVe` lần hai từ localStorage.
    - `BookingInfo.jsx`: hiển thị lịch sử booking.
    - `BookingManager.js` (service): tập hợp các API call (schedule, booking, seat, food).
  - Trích xuất DDL từ `database/cinema.sql` để phân tích schema (15 bảng InnoDB).
  - Grep toàn bộ `@PreAuthorize`/`@PostAuthorize` trên controller để đánh giá phân quyền.
  - Grep `@Query`, `findBy`, `@Modifying` trên tất cả repository để đánh giá chiến lược truy vấn.
  - Grep `FetchType`, `@ManyToOne`, `@OneToMany`, `@JoinColumn` trên `User.java`, `Movie.java`, `Schedule.java`, `Role.java` để phát hiện EAGER/LAZY.
  - Tổng hợp và xuất bản đánh giá toàn diện cho 4 luồng nghiệp vụ + phân tích DB + truy vấn, bao gồm bảng ưu tiên sửa.

- **Quyết định:**
  - Không có — session này thuần phân tích, không thay đổi code.

- **Còn dở / TODO:**
  - Viết migration SQL để sửa các lỗi cấp 🔴 theo thứ tự ưu tiên đã liệt kê (user chưa xác nhận có muốn không).
  - Cụ thể cần migration:
    1. Xóa `UNIQUE KEY UKjx9a9qmksuasu3taidjqyp0co (user_id)` trên bảng `booking`, thêm index thường.
    2. Xóa FK `fk_foodanddrink_room` (FK thứ hai trỏ `cinema_id` → `room.room_id`) trên bảng `foodanddrink`.
    3. Thêm `UNIQUE(schedule_id, seat_id)` trên bảng `seat_schedule`.
    4. Thêm `UNIQUE(seat_schedule_id)` trên bảng `booking_seat`.
  - Sửa `AuthenticationService.java:161`: đổi `ChronoUnit.SECONDS` → `ChronoUnit.MINUTES` cho OTP hết hạn.
  - Sửa `SeatScheduleRepository` để có `findBySchedule_ScheduleId(int id)` thay vì `findAll()`.
  - Bổ sung `@Transactional` + kiểm tra trạng thái ghế (pessimistic lock hoặc optimistic lock) trong `BookingService.createBooking`.
  - Xử lý IDOR: `GET /booking/{bookingId}` cần kiểm tra chủ sở hữu (so sánh userId từ token với booking.userId).
  - Hoàn thiện luồng thanh toán VNPay: xác minh `vnp_SecureHash` trong callback, lưu giao dịch, cập nhật trạng thái booking.

- **Ghi chú:**
  - **Lỗi schema nghiêm trọng nhất:** `UNIQUE KEY (user_id)` trên bảng `booking` — nhiều khả năng Hibernate tự sinh do từng có mapping `@OneToOne` giữa User và Booking rồi sửa thành `@OneToMany` mà không drop constraint cũ. Lỗi này khiến mỗi user chỉ đặt được 1 vé đời dù code trả `List<BookingResponse>`.
  - **Lỗi logic OTP:** `Instant.now().plus(5, ChronoUnit.SECONDS)` — OTP qua email hết hạn sau 5 giây, gần như không ai kịp dùng. Scheduler dọn OTP mỗi 5 phút và giới hạn spam 1.5 phút — ba mốc thời gian hoàn toàn không ăn khớp.
  - **Luồng booking bị fork đôi trên FE:** nút "Đặt vé" và nút "Chọn combo" đều dẫn đến gọi `POST /booking` nhưng đi hai con đường khác nhau — người dùng bấm cả hai sẽ tạo 2 booking riêng biệt cho cùng ghế.
  - **VNPay callback không xác minh chữ ký:** bất kỳ client nào gọi `GET /payment/payment_info?vnp_ResponseCode=00` đều được coi là thanh toán thành công — rủi ro nghiêm trọng nếu deploy thật.
  - **`booking` và `seat_schedule` thiếu unique constraint ở tầng DB:** cả hai lớp phòng thủ (service + DB) đều bỏ trống — overselling có thể xảy ra dưới tải đồng thời.
  - Bảng `invalidated_token` (token blacklist cũ) còn tồn tại trong schema nhưng đã thay bằng Redis — nên drop để tránh nhầm lẫn.
  - Collation bị chia hai nhóm (`utf8mb4_unicode_ci` vs `utf8mb4_0900_ai_ci`) giữa các bảng — chưa lộ lỗi vì JOIN hiện tại dùng khóa `int`, nhưng sẽ gây `Illegal mix of collations` nếu JOIN qua cột VARCHAR.

## [2026-06-20 22:09] unique-constraints-dup-prevention-antd-icons-debounce-fix

- **Đã làm:**
  - **Khảo sát codebase song song (3 agent):** schema bảng `movie` trong `database/cinema.sql` (dòng 300–312), luồng tạo phim backend (`MovieService`, `MovieRepository`, `MovieMapper`, `TmdbController`), tab nhập tay frontend (`AddNewFilm.jsx`).
  - **Phát hiện cạm bẫy kỹ thuật:** `ddl-auto: none` → Hibernate không tạo schema; `@UniqueConstraint` trên entity chỉ là tài liệu. MySQL coi nhiều `NULL` là phân biệt trong UNIQUE index → ràng buộc `(movie_name, release_date)` vô nghĩa nếu `release_date` luôn `NULL`. `GlobalExceptionHandler` chưa có handler cho `DataIntegrityViolationException` → trả HTTP 500 chung chung. User xác nhận: `release_date` bắt buộc ở tab nhập tay + dùng pre-check tầng service kèm handler.
  - **`database/cinema.sql`:** Sửa `CREATE TABLE movie` — thêm `UNIQUE KEY uq_movie_name_release (movie_name, release_date)` và `UNIQUE KEY uq_tmdb_id (tmdb_id)`. Thêm 2 câu `ALTER TABLE movie ADD UNIQUE ...` ngay sau để chạy trên DB đang chạy mà không cần dump lại (dữ liệu seed đều có `release_date`/`tmdb_id = NULL` nên không xung đột).
  - **`Backend/.../repository/MovieRepository.java`:** Thêm 2 derived-query method: `existsByMovieNameAndReleaseDate(String, LocalDate)` và `existsByTmdbId(Integer)`.
  - **`Backend/.../exception/ErrorCode.java`:** Thêm nhóm `5xxx` phim: `MOVIE_ALREADY_EXISTS` và `TMDB_ALREADY_IMPORTED` (theo pattern `(code, message, HttpStatus)` nhất quán với file).
  - **`Backend/.../dto/request/MovieRequest.java`:** Thêm `import jakarta.validation.constraints.NotNull`, thêm annotation `@NotNull(message = "RELEASE_DATE_IS_NULL")` lên field `releaseDate`. Thêm field `String trailerUrl`.
  - **`Backend/.../entity/Movie.java`:** Thêm `@Table(uniqueConstraints = {...})` — chú thích tài liệu, không tạo schema do `ddl-auto: none`.
  - **`Backend/.../service/MovieService.java`:** Thêm pre-check trước `save`: gọi `existsByMovieNameAndReleaseDate` → ném `AppException(MOVIE_ALREADY_EXISTS)` nếu trùng.
  - **`Backend/.../controller/TmdbController.java`:** Thêm pre-check `existsByTmdbId` trước khi import → ném `AppException(TMDB_ALREADY_IMPORTED)`.
  - **`Backend/.../exception/GlobalExceptionHandler.java`:** Thêm handler `@ExceptionHandler(DataIntegrityViolationException.class)` trả `MOVIE_ALREADY_EXISTS` làm lưới an toàn nếu race condition vượt qua pre-check.
  - **`Frontend/.../AddNewFilm.jsx` — bổ sung trường form:** Thêm `DatePicker` từ antd vào ManualForm (bắt buộc, chuyển đổi dayjs → `LocalDate` string trước khi submit), thêm `Input` cho `trailerUrl`. Thêm `releaseDate` vào Formik initialValues + Yup required.
  - **`Frontend/.../AddNewFilm.jsx` — thay emoji bằng antd icons:** Thêm import `{ SearchOutlined, FormOutlined, StarFilled, PictureOutlined }` từ `@ant-design/icons` (v5.6.1 có sẵn). Thay thế: prefix `🔍` của Input tìm kiếm → `<SearchOutlined />`; label tab "Tìm từ TMDB" `🔍` → `<SearchOutlined />`; label tab "Nhập tay" `✏️` → `<FormOutlined />`; rating `⭐` → `<StarFilled style={{ color: '#fadb14' }} />`; placeholder poster "No img" → `<PictureOutlined style={{ fontSize: 20 }} />`.
  - **`Frontend/.../AddNewFilm.jsx` — sửa debounce bị broken:** Phát hiện `doSearch = debounce(...)` nằm trong thân component → tạo instance mới mỗi render (mỗi keystroke) → các timer không huỷ nhau → mỗi ký tự vẫn bắn 1 request. Sửa bằng: bọc `doSearch` trong `useMemo(() => debounce(..., 500), [])` để giữ ổn định instance qua render, thêm `useEffect(() => () => doSearch.cancel(), [doSearch])` để huỷ timer khi unmount. Thêm `useMemo, useEffect` vào import React.

- **Quyết định:**
  - Dùng pre-check ở tầng service thay vì chỉ dựa vào UNIQUE constraint DB, vì: constraint DB trả `DataIntegrityViolationException` không dịch thành thông báo tiếng Việt; pre-check cho phép trả `AppException` với message rõ ràng + có handler DB làm lưới an toàn cho race condition hiếm gặp.
  - `release_date` bắt buộc ở tab nhập tay vì MySQL coi nhiều `NULL` là không bằng nhau trong UNIQUE index — nếu để optional thì `UNIQUE(movie_name, release_date)` không chặn được trùng khi `release_date = NULL`.
  - Không dùng `ALTER TABLE ... ADD CONSTRAINT ... DEFERRABLE` hay trigger vì MySQL InnoDB không hỗ trợ deferrable constraint; pre-check đủ cho usecase single-user admin.
  - Dùng `useMemo` thay `useRef` cho debounce vì đồng nhất hơn với React idiom khi giá trị không có side effect phụ khi tạo.

- **Còn dở / TODO:**
  - Chưa verify trên trình duyệt: chạy `npm run dev`, vào Admin → Thêm phim mới, kiểm tra tab Network khi gõ tìm kiếm (chỉ 1 request sau 500ms dừng gõ), kiểm tra DatePicker và ô Trailer URL hiển thị đúng ở tab Nhập tay.
  - Chạy `ALTER TABLE movie ADD UNIQUE ...` trên DB đang chạy (câu lệnh đã có trong `cinema.sql` nhưng chưa xác nhận được thực thi trên môi trường thật).
  - Backend chưa compile-check (`mvn clean compile`); cần xác nhận `@NotNull` và `@Valid` đã được áp dụng nhất quán tại `@RequestBody @Valid MovieRequest` trong controller.
  - `FilmReducer.js` / `themPhim` service call chưa kiểm tra xử lý error code `MOVIE_ALREADY_EXISTS` (5xxx) để hiển thị toast/Swal đúng.

- **Ghi chú:**
  - `ddl-auto: none` trong `application.yaml` là điểm dễ bỏ qua: annotation JPA trên entity không có tác dụng trên DB thật — mọi schema change phải đi qua `cinema.sql` và `ALTER` thủ công.
  - Dữ liệu seed hiện tại đều có `release_date = NULL` và `tmdb_id = NULL`; do MySQL cho phép nhiều NULL trong UNIQUE index, `ALTER ADD UNIQUE` chạy được ngay mà không cần dọn dữ liệu cũ — đây là điều kiện thuận lợi cần ghi nhớ nếu schema thay đổi sau này.
  - `debounce` từ `lodash.debounce` (không phải `lodash`) — package đã có trong `package.json` của dự án, không cần cài thêm.

## [2026-06-20 21:22] Tư vấn thiết kế: thêm imdbId vào entity Movie thay vì thay thế movieId

- **Đã làm:**
  - Phân tích yêu cầu của người dùng: xem xét khả năng dùng IMDb ID thay cho `movieId` nội bộ trong database.
  - Tìm kiếm toàn bộ codebase bằng pattern `class Movie` → xác định 6 file liên quan: `Movie.java` (entity), `MovieRequest.java`, `MovieResponse.java`, `MovieController.java`, `MovieService.java` (main + test).
  - Tìm kiếm từ khoá `imdb|imdbId|imdbID` (case-insensitive) → không có file nào: xác nhận dự án chưa có field IMDb nào.
  - Đọc `Backend/src/main/java/com/cinemaweb/API/Cinema/Web/entity/Movie.java` → phát hiện entity đã có sẵn `tmdbId` (Integer, dòng ~29); `movieId` là PK kiểu auto-increment (`@GeneratedValue(strategy = IDENTITY)`).
  - Đưa ra khuyến nghị kỹ thuật: **không thay thế `movieId` bằng IMDb ID**, mà **thêm `imdbId` làm cột phụ** với annotation `@Column(unique = true)`, kiểu `String` (vì IMDb ID có dạng `tt0111161`).
  - Đề xuất cụ thể cho bước tiếp theo: thêm field vào `Movie.java`, `MovieRequest.java`, `MovieResponse.java` và xử lý trong `MovieService.java`; thêm lookup endpoint `GET /movies/imdb/{imdbId}`.

- **Quyết định:**
  - Giữ `movieId` (auto-increment Integer) làm PK thay vì thay bằng IMDb ID — lý do: `movieId` đang là FK ở nhiều bảng liên quan (`Showtime`, `Booking`...); đổi kiểu PK đòi migration toàn bộ FK, rủi ro cao. Ngoài ra, IMDb ID là dữ liệu ngoài (không kiểm soát được), không phải phim nào cũng có (phim nội địa, chiếu sớm), và nguyên tắc thiết kế DB khuyến cáo dùng surrogate key ổn định cho PK.
  - Dùng kiểu `String` cho `imdbId` (khác `tmdbId` kiểu `Integer`) — lý do: IMDb ID có tiền tố `tt` không phải số thuần túy.
  - Cho phép `imdbId` NULL — lý do: không phải phim nào cũng được đăng ký trên IMDb.

- **Còn dở / TODO:**
  - Chưa thực hiện thay đổi code nào — session kết thúc ở bước tư vấn, chờ xác nhận từ người dùng.
  - Cần thêm `String imdbId` vào `Movie.java` với `@Column(unique = true)`.
  - Cần cập nhật `MovieRequest.java` và `MovieResponse.java` để include `imdbId`.
  - Cần xử lý trong `MovieService.java`: validation, tránh duplicate khi tạo/update phim.
  - Nên tạo endpoint `GET /movies/imdb/{imdbId}` trong `MovieController.java` để lookup theo IMDb.
  - Cần viết migration SQL (`ALTER TABLE movie ADD COLUMN imdb_id VARCHAR(20) UNIQUE`).

- **Ghi chú:**
  - Entity `Movie.java` đã có `tmdbId` (TMDB ID, kiểu Integer) — cần phân biệt rõ `tmdbId` vs `imdbId` khi thêm vào request/response để tránh nhầm lẫn ở frontend.
  - IMDb ID có định dạng chuẩn `tt` + 7–8 chữ số (ví dụ `tt0111161`) — nên thêm validation regex khi nhận từ API request.
  - Nếu dự án có plan gọi OMDb API hoặc tra cứu metadata từ IMDb, `imdbId` là field quan trọng để map.

## [2026-06-20 18:22] fix-login-state-persistence — thêm withCredentials vào axios để refresh token cookie hoạt động

- **Đã làm:**
  - Điều tra bug: người dùng bị đăng xuất sau ~15 phút (khi access token hết hạn) dù refresh token còn hạn 10 ngày
  - Khám phá backend (`Backend/`) để xác định luồng xác thực: endpoint `POST /auth/refresh-Token` đọc refresh token **từ HttpOnly cookie** qua `readRefreshCookie()`, không phải từ request body; backend đã cấu hình `setAllowCredentials(true)` và `allowedOrigins("http://localhost:5173")` trong CORS config — backend hoạt động đúng
  - Khám phá frontend, đọc `Frontend/src/utils/baseUrl.js`: axios instance được tạo **không có `withCredentials: true`**
    ```js
    export const http = axios.create({
        baseURL: DOMAIN_BE,   // http://localhost:8080
        timeout: 10000
        // ← thiếu withCredentials: true
    });
    ```
  - Xác định nguyên nhân gốc rễ: frontend (`localhost:5173`) và backend (`localhost:8080`) là cross-origin; trình duyệt mặc định **không đính kèm cookie** vào cross-origin request trừ khi client bật `withCredentials: true` — cookie `refresh_token` tồn tại trong trình duyệt nhưng không bao giờ được gửi lên server
  - Xác nhận luồng lỗi: access token hết hạn → interceptor gọi `/auth/refresh-Token` → cookie không gửi → backend nhận `null` từ `readRefreshCookie()` → ném `INVALID_REFRESH_TOKEN` (401) → interceptor xóa localStorage → redirect về `/login`
  - Sửa `Frontend/src/utils/baseUrl.js`: thêm `withCredentials: true` vào `axios.create({})`

- **Quyết định:**
  - Chỉ sửa phía client (thêm `withCredentials: true`), không chạm backend — vì backend đã cấu hình đúng (`setAllowCredentials(true)` + `allowedOrigins` cụ thể), fix tối thiểu tránh tạo side effect
  - Loại trừ `SameSite=Strict` là thủ phạm vì ở môi trường dev cả hai đều là `localhost` (same site), không ảnh hưởng cookie gửi

- **Còn dở / TODO:**
  - Kiểm thử end-to-end: hạ tạm `access-token-ttl` trong `application.yaml` xuống ~30s, đăng nhập, đợi hết hạn, thao tác và quan sát Network tab — `/auth/refresh-Token` phải có header `Cookie` trong request và trả `200`, request gốc được retry thành công, F5 vẫn giữ đăng nhập
  - Xác nhận `withCredentials` không phá vỡ các endpoint khác (upload file, các POST có body, v.v.)

- **Ghi chú:**
  - Cạm bẫy điển hình của luồng refresh token cross-origin: backend log cho thấy endpoint hoạt động đúng nhưng thực ra cookie chưa bao giờ tới server — dễ nhầm là lỗi backend
  - Backend phải dùng `allowedOrigins(...)` cụ thể (không phải `allowedOrigins("*")`) thì `setAllowCredentials(true)` mới có hiệu lực — cấu hình hiện tại đã đúng
  - Nếu sau này deploy lên production (frontend/backend khác domain hẳn), cần đảm bảo cookie có `SameSite=None; Secure` và backend cập nhật `allowedOrigins` cho domain production

## [2026-06-20 18:06] standardize-api-response-handling

- **Đã làm:**
  - Phân tích cách FE xác định thành công/thất bại: Axios hiện dùng HTTP status code (2xx → `try`, 4xx/5xx → `catch`), bỏ qua field `code` trong body. Phát hiện hai vấn đề tồn đọng:
    - BE không đồng nhất: các controller Cinema, Movie, Room, FoodAndDrink, Seat, Schedule, BookingFoodAndDrink, SeatSchedule trả dữ liệu thô (`List`, object, `String`) không có field `code`; chỉ Auth/User/Role/Permission/Booking và response lỗi mới có `ApiResponse` wrapper.
    - FE đọc sai field message lỗi: phần lớn các chỗ gọi `error.response.data.content` (field BE không có), chỉ `Login.jsx` đọc đúng `.message`.
  - **BE — 9 controller bọc trong `ApiResponse`:**
    - `CinemaController.java`: GET list/detail → `ApiResponse<List<Cinema>>`, `ApiResponse<Cinema>`; create/update/delete → `ApiResponse<Void>` với `.message(...)`.
    - `MovieController.java`: tương tự, trả `ApiResponse<List<MovieResponse>>` / `ApiResponse<MovieResponse>`.
    - `RoomController.java`: wrap `ApiResponse<List<RoomResponse>>`, `ApiResponse<RoomResponse>`.
    - `FoodAndDrinkController.java`: wrap `ApiResponse<List<FoodAndDrinkResponse>>`, `ApiResponse<FoodAndDrinkResponse>`.
    - `SeatController.java`: wrap `ApiResponse<List<SeatResponse>>`, `ApiResponse<SeatResponse>`.
    - `ScheduleController.java`: wrap `ApiResponse<List<ScheduleResponse>>`, `ApiResponse<ScheduleResponse>`.
    - `BookingController.java`: đã có `ApiResponse` một phần, hoàn chỉnh nốt.
    - `BookingFoodAndDrinkController.java`: wrap `ApiResponse<List<BookingFoodAndDrinkResponse>>`, `ApiResponse<BookingFoodAndDrinkResponse>`.
    - `SeatScheduleController.java` (chỉ có GET): wrap `ApiResponse<List<SeatScheduleResponse>>`.
    - `PaymentController.java`: **giữ nguyên** — shape VNPay đặc thù, HTTP 200 → interceptor cho qua, bọc vào `ApiResponse` sẽ phá luồng redirect.
  - **FE — `utils/baseUrl.js`:** Sửa success handler trong response interceptor: kiểm tra nếu body có `code !== 1000` thì `Promise.reject` (đẩy vào nhánh `catch`); body không có `code` (PaymentController) thì bỏ qua, cho qua bình thường. Logic refresh token 401 giữ nguyên.
  - **FE — đổi `.data` → `.data.body`** tại các call site sau khi endpoint đã wrap:
    - `redux/reducers/CinemaReducer.js`, `FilmReducer.js`, `RoomReducer.js`, `ScheduleReducer.js`
    - `components/Detail/ShowtimeDetail.jsx`, `components/Home/MenuCinema.jsx`
    - `pages/User/BookingTicket.jsx`, `pages/User/BookingInfo.jsx`, `pages/User/BookingFoodAndDrink.jsx`
    - Các component và template liên quan đến Seat, Schedule chi tiết.
  - **FE — chuẩn hóa `.content` → `.message`:**
    - Tất cả các `SwalConfig(error.response.data.content, ...)` đổi thành `error.response.data.message`.
    - Toast thành công: `result.data.content` → `result.data.message`.
    - Các file ảnh hưởng gồm các reducer và page: `ResetPassword.jsx` và các component CRUD admin.
  - Chạy kiểm tra: `mvn -q -DskipTests -f .../Backend/pom.xml compile` → EXIT=0 (chỉ warning JDK `sun.misc.Unsafe` từ guice, không liên quan thay đổi); `npm run build` (Vite) tại `Frontend/` → ✓ trong ~25s (chỉ warning CJS deprecation và chunk size có sẵn từ trước).

- **Quyết định:**
  - Chuẩn hóa BE (wrap mọi controller trong `ApiResponse`) thay vì FE tolerant (chỉ sửa interceptor để chấp nhận cả body thô lẫn body có `code`): đảm bảo mọi response đều nhất quán, FE có thể check cứng `code === 1000` mà không cần logic phân nhánh phức tạp. Cách tolerant tuy ít thay đổi hơn nhưng tạo ra hai "hợp đồng" khác nhau tồn tại song song, dễ gây bug khi thêm endpoint mới.
  - `PaymentController` giữ nguyên: luồng VNPay dùng redirect HTTP, không phải JSON response tiêu chuẩn; wrapping sẽ phá logic callback của VNPay.
  - Logic reject trong interceptor kiểm tra `response.data?.code !== undefined && response.data.code !== 1000` thay vì `!== 1000` thuần túy: tránh false-reject khi body không phải `ApiResponse` (phòng thủ cho PaymentController và bất kỳ endpoint thuần HTTP nào về sau).

- **Còn dở / TODO:**
  - Test thủ công chưa thực hiện (không có test tự động):
    1. Trang chủ, Detail, admin: danh sách phim/cụm rạp/lịch chiếu/ghế phải hiển thị đúng (dữ liệu giờ ở `.data.body`).
    2. Admin thêm/sửa/xóa phim, cụm rạp, phòng → toast phải lấy đúng text từ `result.data.message`.
    3. Đăng nhập, đặt vé, đặt combo → luồng end-to-end chạy bình thường.
    4. Gây lỗi có chủ ý (JSON parse error, validation fail) → `catch` phải hiển thị đúng `error.response.data.message` từ BE (vd: `{code:9001, message:"JSON parse error..."}`).
    5. Smoke test luồng thanh toán VNPay (PaymentController không đổi nhưng cần xác nhận interceptor không can thiệp).

- **Ghi chú:**
  - FE đang đọc `.content` vì tại một thời điểm BE có thể đã từng dùng field đó, hoặc nhầm lẫn khi tham chiếu `ApiResponse` (có `.body`) và `ApiResponseError` (có `.message`). `ApiResponseError.java` chỉ có `int code` và `String message`, không có `content` → mọi toast lỗi trước đây hiển thị `undefined`.
  - `BannerReducer.js` không gọi API thực → không cần sửa `.data`.
  - Các handler `handleDatVe` trong `BookingTicket.jsx` và `BookingFoodAndDrink.jsx` chỉ `await` không đọc body response → không cần đổi, an toàn.
  - `mvn compile` cần chạy từ thư mục cha với `-f path/pom.xml` vì working directory mặc định là scratchpad, không phải `Backend/`; chạy `cd Backend` rồi tiếp lệnh trong cùng một PowerShell call bị lỗi path.

## [2026-06-20 00:25] sass-migration-use-api: chuyển toàn bộ SCSS từ @import sang @use/@forward + modern compiler API

- **Đã làm:**
  - Điều tra lỗi Vite dev server báo `Failed to parse source for import analysis because the content contains invalid JS syntax` trên hàng loạt file JSX — xác định nguyên nhân gốc là esbuild `include` regex trong `vite.config.js` không match `.jsx`, đã được sửa trước đó; lỗi JSX không còn tồn tại khi session bắt đầu làm Sass.
  - Lên kế hoạch (Plan mode) và được duyệt: migration Dart Sass `@import` → `@use`/`@forward` + bật `api: 'modern-compiler'` để dẹp 3 loại deprecation warning của legacy JS API.
  - Sửa `Frontend/vite.config.js`: thêm `scss: { api: 'modern-compiler' }` trong block `css.preprocessorOptions` để Vite dùng Dart Sass modern compiler thay vì legacy JS API.
  - Sửa `Frontend/src/assets/sass/abstracts/_mixin.scss`:
    - Thêm `@use 'sass:map';` và `@use 'variables' as *;` ở đầu file.
    - Đổi `map-has-key($map: $breakpoint, $key: $screen)` → `map.has-key($breakpoint, $screen)`.
    - Đổi `map-get($map: $breakpoint, $key: $screen)` → `map.get($breakpoint, $screen)`.
  - Thêm `@use '../abstracts/variables' as *;` vào đầu hai file tiêu thụ biến:
    - `Frontend/src/assets/sass/layout/_footer.scss`
    - `Frontend/src/assets/sass/components/_carousel.scss`
  - Thêm `@use '../abstracts/mixin' as *;` vào đầu năm file tiêu thụ mixin:
    - `Frontend/src/assets/sass/pages/_login.scss`
    - `Frontend/src/assets/sass/pages/_register.scss`
    - `Frontend/src/assets/sass/pages/_news.scss`
    - `Frontend/src/assets/sass/components/_card.scss`
    - `Frontend/src/assets/sass/components/_movie-list.scss`
  - Sửa `Frontend/src/assets/sass/main.scss`: chuyển toàn bộ `@import` → `@use`; bỏ hai dòng import abstracts thừa (vì các partial đã tự `@use` trực tiếp).
  - Chạy kiểm tra: `npx sass src/assets/sass/main.scss __main_check.css --no-source-map` → EXIT: 0, không có warning nào ra stderr.

- **Quyết định:**
  - Dùng `@use '...' as *` thay vì đặt namespace (`as vars`, `as mix`) để tránh phải đổi tên `$ColorPrimary`, `@include responsive(...)` trong toàn bộ rule body — giảm diff tối thiểu, ít rủi ro hơn.
  - Bỏ hoàn toàn dòng import abstracts trong `main.scss` thay vì giữ lại: vì với `@use`, mỗi partial cần tự khai báo dependency, còn import từ entry point không có tác dụng forward vào partial nữa — giữ lại sẽ gây nhầm lẫn.
  - Không dùng `@forward` trong `main.scss` vì đây là entry point cuối, không có file nào `@use` lại `main.scss`.

- **Còn dở / TODO:**
  - Chưa restart `npm run dev` để xác nhận terminal sạch hoàn toàn 3 loại Sass deprecation warning trong môi trường Vite thực tế — cần verify thủ công.
  - Các file SCSS khác (nếu có) chưa được scan toàn bộ để xem còn `@import` nào sót không — nên chạy `grep -r "@import" src/assets/sass/` để chắc chắn.

- **Ghi chú:**
  - Lỗi JSX ban đầu (`Room.jsx:86`, `EditRoom.jsx:37`, `AddRoom.jsx:33`, `Seat.jsx:20`, `Schedule.jsx:128`, `BookingFoodAndDrink.jsx:92`) là do esbuild transform plugin không nhận file `.jsx` — đã được fix ở session trước, không phải việc của session này.
  - Dart Sass modern API yêu cầu `map.has-key()` / `map.get()` thay vì dạng named-argument cũ (`map-has-key($map: ..., $key: ...)`) — nếu bỏ sót sẽ báo lỗi compile thay vì chỉ warning.
  - Lệnh kiểm tra `npx sass ... --no-source-map` bị lỗi lần đầu do path separator Windows (dùng `:` để phân tách input/output theo cú pháp Unix) — phải đổi sang output file tương đối mới chạy được.

## [2026-06-19 23:50] Migrate CRA (webpack) sang Vite + fix lỗi JSX import-analysis
- **Đã làm:**
  - Khảo sát cấu trúc dự án frontend tại `Frontend/` — xác nhận đang dùng **Create React App** (CRA) với `react-scripts 5.0.1`, webpack chạy ẩn bên dưới.
  - Cập nhật `Frontend/package.json`:
    - Xóa `react-scripts` khỏi `dependencies`
    - Thêm `vite` và `@vitejs/plugin-react` vào `devDependencies`
    - Đổi scripts: `"start"` → `"dev": "vite"`, `"build"` → `"vite build"`, thêm `"preview": "vite preview"`
  - Tạo mới `Frontend/vite.config.js` với: plugin `@vitejs/plugin-react` (include `**/*.{jsx,js}`), `css.preprocessorOptions.scss`, `optimizeDeps.esbuildOptions.loader: {'.js': 'jsx'}`, và sau đó thêm `esbuild.loader: 'jsx'` + `esbuild.include: /src\/.*\.js$/` ở top-level.
  - Tạo `Frontend/index.html` (Vite entry point) từ nội dung `public/index.html` cũ: thay tất cả `%PUBLIC_URL%` thành `/`, thêm `<script type="module" src="/src/index.js"></script>`.
  - Xóa `Frontend/public/index.html` (đã di chuyển lên root level để Vite nhận đúng entry).
  - Chạy `npm install` trong `Frontend/`: loại bỏ 1173 packages CRA, thêm 13 packages Vite mới, tổng còn 422 packages.
  - Kiểm tra `process.env` trong `src/` bằng grep — không có file nào sử dụng (không cần đổi sang `import.meta.env`).
  - Phát hiện và xóa 2 import thừa (bug sẵn có trong code, CRA bỏ qua nhưng Vite bắt lỗi khi scan dependencies):
    - `Frontend/src/pages/User/Register.jsx`: xóa `import { GROUPID } from '../../utils/constant'` — `GROUPID` không tồn tại trong `constant.js`
    - `Frontend/src/utils/baseUrl.js`: đổi `import { DOMAIN_BE, LOCALSTORAGE_USER, TOKEN }` → `import { DOMAIN_BE, LOCALSTORAGE_USER }` — `TOKEN` không tồn tại trong `constant.js`
  - Sau khi user báo lỗi runtime `vite:import-analysis` trên `src/index.js:21` (JSX trong file `.js`): cập nhật lại `vite.config.js` thêm block `esbuild: { loader: 'jsx', include: /src\/.*\.js$/ }` ở cấp top-level (ngoài `optimizeDeps`).
  - Chạy kiểm tra cuối: `npm run dev` → **VITE v5.4.21 ready in 560ms** tại `http://localhost:5174`, không còn lỗi.

- **Quyết định:**
  - Dùng `esbuild` top-level (`esbuild.loader + esbuild.include`) thay vì chỉ `optimizeDeps.esbuildOptions.loader`: plugin `vite:import-analysis` (core) chạy trước plugin người dùng; nếu chỉ config trong `optimizeDeps` thì `import-analysis` vẫn thấy JSX thô trong `.js` và lỗi trước khi plugin React kịp transform. Config top-level `esbuild` đảm bảo Vite's esbuild transform chạy trước bước phân tích import.
  - Dùng `Write` (ghi lại toàn bộ file) thay vì `Edit` cho `package.json` do lỗi string-mismatch khi Edit (có thể do BOM/CRLF hoặc ký tự ẩn trong file gốc CRA).
  - Xóa import thừa `GROUPID`/`TOKEN` thay vì thêm export giả vào `constant.js`: đây là dead code thực sự, không có chỗ nào trong codebase sử dụng hai hằng số này.

- **Còn dở / TODO:**
  - Verify app hoạt động đầy đủ trên browser tại `localhost:5173/5174` — chỉ kiểm tra server start, chưa mở browser để test UI thực tế.
  - Kiểm tra `npm run build` (Vite production build) có pass không.
  - Warning "The CJS build of Vite's Node API is deprecated" còn xuất hiện — cần nâng cấp `vite.config.js` sang ESM (`"type": "module"` trong `package.json`) hoặc đổi tên file thành `vite.config.mjs` để dùng ES module natively.
  - Chưa kiểm tra SCSS compilation thực tế (chỉ config `css.preprocessorOptions.scss` trống).

- **Ghi chú:**
  - `vite:import-analysis` là plugin **nội bộ của Vite core**, chạy sớm hơn toàn bộ user plugins — đây là cạm bẫy phổ biến khi migrate CRA sang Vite với codebase dùng JSX trong file `.js`.
  - `optimizeDeps.esbuildOptions` chỉ ảnh hưởng đến bước pre-bundling (deps trong `node_modules`), không ảnh hưởng đến transform của source files trong `src/` — phải dùng `esbuild` top-level cho source files.
  - CRA (webpack) với `babel-loader` mặc định xử lý tất cả `.js` như JSX nên không bao giờ lỗi với import thừa hay JSX syntax — Vite strict hơn đáng kể trong cả hai vấn đề này.
  - Dev port đổi từ `3000` (CRA mặc định) sang `5173` (Vite mặc định); cần update CORS config trong Spring Boot backend nếu có whitelist port cứng.

## [2026-06-19 23:26] fix-build-errors-backend

- **Đã làm:**
  - Điều tra lỗi build Maven từ output log do người dùng cung cấp, xác định hai vấn đề riêng biệt.
  - **Điều tra lỗi 1 — MapStruct warning `[38,10] Unmapped target property: "scheduleId"`:**
    - Dùng agent Explore để quét tất cả các file mapper trong `Backend/src/main/java/com/cinemaweb/API/Cinema/Web/mapper/`.
    - Đọc lần lượt `SeatScheduleMapper.java`, `BookingMapper.java`, `BookingSeatMapper.java`, `ScheduleMapper.java` để xác định file nào có warning tại dòng 38.
    - Xác định nguyên nhân: `ScheduleMapper.java` — method `toUpdateSchedule` (nhận `ScheduleRequest` và `@MappingTarget Schedule`) thiếu annotation `@Mapping(target = "scheduleId", ignore=true)`. MapStruct cảnh báo vì field `scheduleId` (khóa chính `@Id @GeneratedValue` trong entity `Schedule`) không có nguồn mapping và không được bỏ qua tường minh.
    - Đọc `entity/Schedule.java` để xác nhận `scheduleId` là `@Id @GeneratedValue(strategy = IDENTITY)`.
    - Sửa `ScheduleMapper.java`: thêm `@Mapping(target = "scheduleId", ignore=true)` vào method `toUpdateSchedule`.
  - **Điều tra lỗi 2 — Compilation error `cannot find symbol class Role` tại `src/test/.../service/UserService.java:[8,42]`:**
    - Đọc file test `Backend/src/test/java/com/cinemaweb/API/Cinema/Web/service/UserService.java`.
    - Phát hiện: dòng 8 import `com.cinemaweb.API.Cinema.Web.enums.Role` — enum trong source thực tế tên là `Roles` (số nhiều), không phải `Role`.
    - Dòng 61 trong method tạo user: `roleRepository.findAllById(List.of(Role.USER.name()))` dùng tên sai.
    - Sửa `UserService.java` (test): đổi import từ `enums.Role` → `enums.Roles`; đổi usage `Role.USER.name()` → `Roles.USER.name()`.
  - Hướng dẫn người dùng chạy `mvn test-compile` để xác nhận cả hai lỗi đã được giải quyết.

- **Quyết định:**
  - Dùng `@Mapping(target = "scheduleId", ignore=true)` thay vì xóa mapping: vì đây là update method với `@MappingTarget`, bỏ qua khóa chính là đúng đắn — không muốn MapStruct cố gán lại ID từ request vào entity đang được cập nhật.
  - Sửa test file (tên enum) thay vì tạo alias `Role`: enum `Roles` là tên gốc trong production code, test file bị viết sai tên — sửa test cho khớp source là cách đúng.

- **Còn dở / TODO:**
  - Chạy `mvn test-compile` (hoặc `mvn test`) để xác nhận build xanh sau hai fix trên.
  - Kiểm tra xem còn mapper nào khác có field `@Id` chưa được ignore trong update method không (tìm `@MappingTarget` trong tất cả mapper).
  - Warning unchecked cast trong `ConfigPayment.java` chưa xử lý — chưa ảnh hưởng build nhưng cần xem xét khi có thời gian.

- **Ghi chú:**
  - Cạm bẫy: warning MapStruct `[38,10]` ban đầu bị nhầm là thuộc `SeatScheduleMapper.java` (file được Explore agent tìm đầu tiên) — thực ra thuộc `ScheduleMapper.java` dòng 38. Cần đọc tất cả mapper trước khi kết luận.
  - Enum đặt tên `Roles` (số nhiều) trong production nhưng test viết `Role` (số ít) — lỗi đánh máy trong test file, không phải refactor enum.
  - MapStruct chỉ phát sinh warning (không lỗi) cho unmapped property trong trường hợp này vì `ScheduleMapper` không dùng `unmappedTargetPolicy = ERROR`. Nếu muốn bắt sớm hơn, có thể thêm `@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)`.

## [2026-06-19 23:12] Điều tra lỗi Maven compilation failure — TypeTag UNKNOWN

- **Đã làm:**
  - Nhận lỗi build từ user: `mvn clean install` trên project `API-Cinema-Web 0.0.1-SNAPSHOT` (130 source files) thất bại với `[ERROR] Fatal error compiling: java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`
  - Đọc `Backend/pom.xml` để xác định Spring Boot version (3.4.4), Java target version (21), và các dependency annotation processor (Lombok, MapStruct)
  - Phân tích warning trong output: `sun.misc.Unsafe::staticFieldBase has been called by com.google.inject.internal.aop.HiddenClassDefiner` — ban đầu nghi ngờ JDK mới hơn 21 làm Lombok crash do mất internal API `TypeTag.UNKNOWN`
  - Xác nhận môi trường qua output user cung cấp thủ công:
    - `java --version` → OpenJDK 21.0.11 Temurin (Eclipse Adoptium)
    - `mvn --version` → Apache Maven 3.9.11, Java 21.0.11, runtime tại `C:\Users\ADMIN\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.11.10-hotspot`
  - Đánh giá lại nguyên nhân: JDK version là đúng (21 ↔ target 21), loại bỏ giả thuyết version mismatch
  - Kết luận nguyên nhân mới: **Maven local repository bị corrupt** — JAR của Lombok hoặc MapStruct tải xuống không hoàn chỉnh dẫn đến `ExceptionInInitializerError` khi annotation processor khởi tạo
  - Đề xuất fix: xóa `~/.m2/repository/org/projectlombok` và `~/.m2/repository/org/mapstruct` rồi chạy lại `mvn clean install`; nếu vẫn lỗi, chạy `mvn clean install -e` để lấy full stack trace

- **Quyết định:**
  - Không dùng PowerShell tool để tự chạy lệnh kiểm tra JDK/Maven (user reject tool call) — chờ user cung cấp output thủ công rồi phân tích
  - Ưu tiên xóa chính xác 2 thư mục Lombok + MapStruct thay vì xóa toàn bộ `.m2/repository` để tránh tải lại toàn bộ dependency không cần thiết

- **Còn dở / TODO:**
  - Chưa xác nhận fix thành công — user chưa chạy lệnh xóa `.m2` và rebuild
  - Nếu lỗi vẫn còn sau khi xóa local cache, cần chạy `mvn clean install -e` và xem full stack trace để xác định đúng annotation processor bị lỗi (Lombok hay MapStruct)
  - Nên kiểm tra version cụ thể của Lombok trong `pom.xml` (chưa đọc đủ file) — nếu dùng Lombok < 1.18.30, có thể có bug với javac 21

- **Ghi chú:**
  - Lỗi `TypeTag :: UNKNOWN` là dấu hiệu của Lombok (hoặc MapStruct) dùng javac internal API bị thay đổi — thường gặp khi JDK không khớp hoặc khi annotation processor JAR bị corrupt/partial download
  - Warning `sun.misc.Unsafe::staticFieldBase will be removed` đến từ Guice (trong Maven runtime) không liên quan trực tiếp đến lỗi compile, gây nhầm hướng ban đầu
  - Maven đang dùng JDK tại `AppData\Local\Programs\Eclipse Adoptium` (user install), không phải system JDK — cần lưu ý nếu có nhiều JDK trên máy

## [2026-06-19 22:26] Kiểm tra cấu hình Java, tạo docker-compose, debug lỗi JWT Token invalid

- **Đã làm:**
  - Đọc `Backend/pom.xml` để xác nhận phiên bản Java thực tế đang dùng qua thẻ `<java.version>` (dòng 30) và cấu hình `<source>`/`<target>` compiler plugin (dòng 124–125) — kết quả: **Java 23**.
  - Tư vấn hạ về Java 21 LTS (hỗ trợ đến 2029; Java 23 non-LTS hết support từ 3/2025); xác định chỉ cần sửa 1 chỗ trong `pom.xml` vì toàn bộ dùng `${java.version}`.
  - Đọc `Frontend/package.json` phát hiện frontend đang dùng **Create React App** (`react-scripts 5.0.1`, đã deprecated), không phải Vite; tư vấn không cần migrate vì là side project gần xong; liệt kê 7 bước cụ thể nếu muốn migrate (cài Vite, tạo `vite.config.js`, chuyển `index.html`, đổi biến `REACT_APP_*` → `VITE_*`, xoá `react-scripts`).
  - Kiểm tra Redis trong project: grep `redis|cache` trên `Backend/src` và `pom.xml` — **không có dependency, không có config, không có code**; JWT blacklist đang dùng bảng `InvalidatedToken` MySQL, OTP dùng bảng `PasswordOTP` MySQL.
  - Đọc `Backend/src/main/resources/application.yaml` để lấy thông tin DB (`cinemaweb`, password `0000`, port `3306`) và cấu trúc thư mục gốc (`Assets/`, `Backend/`, `Frontend/`, `database/`).
  - Tạo file **`CinemaManagementWeb/docker-compose.yml`** tại root với 2 service:
    - `cinemaweb-mysql` (mysql:8.0): env `MYSQL_ROOT_PASSWORD=0000`, `MYSQL_DATABASE=cinemaweb`, port `3306:3306`, auto-import `database/cinema.sql` qua `/docker-entrypoint-initdb.d/`, volume `mysql_data`.
    - `cinemaweb-redis` (redis:7-alpine): port `6379:6379`, volume `redis_data`.
  - Hướng dẫn kết nối BE với DB/Redis: khi BE chạy trên IDE (không Docker), chỉ cần thêm config Redis vào `application.yaml` (`spring.data.redis.host: localhost`, `port: 6379`) và thêm dependency `spring-boot-starter-data-redis` vào `pom.xml`.
  - Đánh giá kiến trúc FE: đọc `App.js`, `store.js`, `UserReducer.js`, `FilmService.js`, `baseUrl.js`, `constant.js`, `_core/models/`; xác định các vấn đề: tên biến lẫn tiếng Việt/Anh, thunks nằm trong Reducer file, **token lấy không nhất quán** (`baseUrl.js` lấy qua `getLocalStorage('USER').accessToken` vs `UserReducer.js` lấy thẳng `localStorage.getItem('accessToken')`), dùng `unstable_HistoryRouter`, `BannerReducer` có thể dead code.
  - Đánh giá SASS vs Tailwind: đọc `AdminTemplate.jsx` (dùng Tailwind thuần), `main.scss` (cấu trúc phân theo pages/components/layout); kết luận giữ cả hai với phân vai rõ: Tailwind cho utility, SASS cho override Ant Design và animation phức tạp.
  - Debug lỗi `Token invalid` ở `CustomJwtDecoder.java:33`:
    - Đọc `CustomJwtDecoder.java`, `AuthenticationService.java` (hàm `isAccessTokenValid` dòng 255–260 dùng `redisTemplate.hasKey(ACCESS_BLACKLIST_PREFIX + jti)`).
    - Phát hiện `application.yaml` đã được cập nhật: bỏ hardcode credentials, thêm Redis config (`spring.data.redis.host/port`), dùng env vars cho DB/JWT/mail.
    - Đọc `SecurityConfig.java` và `AuthenticationController.java`; CORS đang set `allowedOrigins("http://localhost:5173")` — phù hợp với Vite, không phải nguyên nhân lỗi.
    - Spawn 2 agent explore: (1) đọc toàn bộ `AuthenticationService.java` để hiểu `verifyToken()` và reuse detection; (2) kiểm tra `constant.js` và `application.yaml` — phát hiện `DOMAIN_BE = 'http://localhost:8080'` (không có `/api`) trong khi BE có `context-path: /api`.
    - Xác định root cause: **2 request refresh token đồng thời** (log BE 21:34:28 và 21:34:29) kích hoạt cơ chế reuse detection, token đầu thành công thì token thứ hai bị invalidate → cả session bị logout.
    - Ghi plan vào `C:\Users\ADMIN\.claude\plans\fe-t-v-n-th-y-hidden-wolf.md`.

- **Quyết định:**
  - Đặt `docker-compose.yml` ở root thay vì trong `Backend/` — vì file quản lý infrastructure cho cả project, không thuộc riêng một layer nào.
  - Khuyến nghị giữ cả SASS lẫn Tailwind, không loại bỏ cái nào — SASS đã có cấu trúc tốt và cần thiết cho Ant Design override, Tailwind đang dùng mạnh trong AdminTemplate.
  - Không sửa CORS khi user xác nhận đang dùng Vite port 5173 — CORS đúng rồi, không cần thay đổi.

- **Còn dở / TODO:**
  - Plan fix concurrent refresh token chưa được implement: cần thêm mutex/debounce ở FE (`baseUrl.js` trong Axios interceptor) để chỉ gửi 1 request refresh đồng thời, các request khác chờ kết quả.
  - `ExitPlanMode` bị lỗi `Tool permission stream closed` — plan đã ghi vào file nhưng chưa được execute.
  - Java chưa được hạ từ 23 xuống 21 trong `pom.xml:30` — user chưa confirm thực hiện.
  - Thêm `spring-boot-starter-data-redis` vào `pom.xml` nếu muốn dùng Redis trong code (hiện `isAccessTokenValid` đã dùng `redisTemplate` nhưng dependency chưa rõ có trong pom chưa).
  - Kiểm tra `DOMAIN_BE` trong `Frontend/src/utils/constant.js` có thiếu `/api` path hay không — hiện là `http://localhost:8080`, BE có `context-path: /api`.
  - Vấn đề token inconsistency trong FE chưa fix: `baseUrl.js` và `UserReducer.js` đọc token từ 2 key localStorage khác nhau.

- **Ghi chú:**
  - `application.yaml` đã thay đổi đáng kể so với lúc đầu session: từ hardcode credentials sang env vars (`${DB_URL}`, `${JWT_SIGNER_KEY}`, v.v.) và đã thêm `spring.data.redis` config — điều này giải thích tại sao token cũ bị invalid (signer key thay đổi khi restart BE với env var mới).
  - Root cause lỗi JWT không phải cookie thiếu hay Redis chưa chạy mà là **concurrent refresh request** — cạm bẫy phổ biến khi implement refresh token: nhiều tab hoặc nhiều request 401 cùng lúc đều trigger refresh, reuse detection của BE invalidate session ngay lập tức.
  - FE `DOMAIN_BE` không có `/api` trong khi BE có `context-path: /api` — cần verify xem tất cả API call có đang hoạt động đúng không, hay đã có cách khác compensate (ví dụ từng service tự thêm prefix).

## [2026-06-19 22:07] Restructure API-Cinema-Web project directory

- **Đã làm:**
  - Kiểm tra cấu trúc thư mục gốc tại `C:\Users\ADMIN\Desktop\SideProj\CinemaManagementWeb\Backend\`, phát hiện hai lớp thư mục trung gian thừa: `API Cinema Web\API-Cinema-Web\`.
  - Di chuyển toàn bộ 7 mục từ `Backend\API Cinema Web\API-Cinema-Web\` lên thẳng `Backend\` bằng `Move-Item` trong PowerShell (dùng `Get-ChildItem -Force` để bắt cả file ẩn):
    - Thư mục: `.mvn`, `src`, `target`
    - File: `.gitattributes`, `.gitignore`, `mvnw`, `pom.xml`
  - Xóa thư mục trung gian rỗng `Backend\API Cinema Web\` bằng `Remove-Item -Recurse -Force`.
  - Xác minh cấu trúc sau khi move: `Backend/` chứa trực tiếp `.idea`, `.mvn`, `database`, `src`, `target`, `.gitattributes`, `.gitignore`, `mvnw`, `pom.xml` — đúng yêu cầu.
  - Kiểm tra toàn bộ cây thư mục `CinemaManagementWeb/` ở độ sâu 3 cấp để đánh giá mức độ gọn gàng tổng thể.
  - Đọc `Backend\.gitignore` — xác nhận `target/` đã có trong danh sách ignore, nên thư mục build không bị commit.
  - Phát hiện và điều tra thư mục `.github\modernize\java-upgrade\hooks\scripts\recordToolUse.ps1` — đây là file do VS Code extension (Claude Code - Java Upgrade) tự sinh, không phải do người dùng tạo; thư mục này có `.gitignore` riêng (`**/*`) nên không bị commit.
  - Phát hiện hai thư mục `.idea` tồn tại song song: `.idea/` ở root chứa `BTL-CNPM.iml` (cũ), `Backend/.idea/` chứa `API-Cinema-Web-SpringBoot.iml` (đúng chỗ).

- **Quyết định:**
  - Dùng `Move-Item` với `Get-ChildItem -Force` thay vì copy để đảm bảo bắt được file ẩn (`.mvn`, `.gitignore`, `.gitattributes`) — nếu không có `-Force` sẽ bỏ sót.
  - Không tự động xóa `.idea/` ở root và `.github/modernize/` mà hỏi người dùng trước — vì đây là thao tác xóa không reversible và `.idea/` ở root có thể vẫn được dùng nếu mở monorepo bằng IntelliJ.

- **Còn dở / TODO:**
  - Xác nhận với người dùng có muốn xóa `.idea/` ở root (`CinemaManagementWeb/.idea/` chứa `BTL-CNPM.iml`) không.
  - Xác nhận có muốn xóa `.github/modernize/` không (rác từ extension, không bị commit nhưng gây nhiễu).
  - Người dùng cần reload project trong IDE (IntelliJ) để nhận diện lại đường dẫn mới của `Backend/` sau khi move.

- **Ghi chú:**
  - Thư mục `Backend/target/` tuy có mặt trên disk nhưng không phải vấn đề — đã được `Backend/.gitignore` cover bằng dòng `target/`.
  - `.github\modernize\java-upgrade\` là artifact của VS Code extension, không nên nhầm với workflow CI/CD thủ công. File `recordToolUse.ps1` bên trong ghi lại `run_in_terminal` và `appmod-*` tool calls dưới dạng JSONL cho extension xử lý.
  - Sau khi restructure, `Backend/` đóng vai trò root của Maven project (có `pom.xml` và `mvnw` trực tiếp) — các lệnh Maven (`./mvnw clean compile`) cần chạy từ `Backend/`.

## [2026-06-19 21:29] Tích hợp TMDB API vào hệ thống quản lý phim Cinema Management

- **Đã làm:**
  - Khảo sát toàn bộ codebase dự án Cinema Management Web (Spring Boot backend + React/Vite frontend + MySQL):
    - Đọc `Backend/database/cinema.sql`, `README.md`, `application.yaml`, `Movie.java`, `MovieController.java`, `MovieService.java`, `AddNewFilm.jsx`, `FilmService.js`
    - Phát hiện entity `Movie` có các field cơ bản nhưng thiếu `tmdbId`, `trailerUrl`, `movieLanguage`, `releaseDate`
    - Phát hiện `TMDB_KEY=3a86835538699123b0bd9f1efc62b29c` đã tồn tại sẵn trong file `Backend/.env`
  - Lập kế hoạch 3 phase tích hợp TMDB API:
    - Phase 1 — Backend: thêm cột DB, cập nhật entity, tạo `TmdbService.java` + `TmdbController.java`
    - Phase 2 — Frontend: tạo `TmdbService.js`, `TmdbReducer.js`, cập nhật `AddNewFilm.jsx` thêm tab tìm kiếm TMDB
    - Phase 3 — Frontend: hiển thị trailer YouTube embed trên `Detail.jsx`
  - Tạo file migration SQL `Backend/database/migrate_tmdb_fields.sql`:
    - `ALTER TABLE movie ADD COLUMN tmdb_id INT NULL, trailer_url VARCHAR(255) NULL, release_date DATE NULL`
  - Tạo mới `TmdbService.java` (backend): gọi 3 TMDB endpoint — search movie, get movie detail (bao gồm genres/runtime), get trailer video; parse `JsonNode` để lấy YouTube trailer key
  - Tạo mới `TmdbController.java` với 3 endpoint:
    - `GET /api/tmdb/search?query=...` → trả `ApiResponse<List<TmdbMovieResult>>`
    - `POST /api/tmdb/import/{tmdbId}` → lưu phim vào DB cinemaweb
    - `GET /api/tmdb/db/detail/{tmdbId}` → lấy chi tiết phim để điền form
  - Tạo mới `Frontend/src/services/TmdbService.js` với 3 hàm: `searchTmdb`, `importFromTmdb`, `getTmdbDetail`
  - Cập nhật `Frontend/src/pages/Admin/Film/AddNewFilm.jsx`:
    - Thêm state `activeTab` để điều khiển Tabs (controlled component)
    - Thêm tab "🔍 Tìm từ TMDB": debounced search 500ms, hiển thị list kết quả có poster + tên + năm
    - Mỗi kết quả có 2 button: "Import thẳng" (gọi `importFromTmdb`) và "Điền vào form" (gọi `getTmdbDetail` rồi auto-fill formik)
    - Sau khi fill form, tự động chuyển sang tab "✏️ Nhập tay" (`setActiveTab('manual')`) để user thấy form đã được điền
    - Giữ tab "✏️ Nhập tay" cũ làm fallback nhập tay
  - Cập nhật `Frontend/src/pages/User/Detail.jsx`:
    - Thêm block render `<iframe>` YouTube embed khi `filmDetail.trailerUrl` tồn tại
    - Parse `v` param từ URL YouTube bằng `URLSearchParams(new URL(...).search).get('v')`
  - Sửa 4 bug phát sinh trong quá trình implement:
    - **Bug 1 — Search trả rỗng:** `TmdbController` trả `List<TmdbMovieResult>` trực tiếp thay vì wrap `ApiResponse<T>` → axios interceptor đọc `res.data.body` ra `undefined` → fix bằng wrap tất cả response trong `ApiResponse.<T>builder().body(...).build()`
    - **Bug 2 — "Điền vào form" lỗi "Không thể lấy chi tiết phim":** `TmdbService.getMovieDetail` dùng `jsonNode.get("name").asText()` với `JsonNode.get()` có thể trả Java `null` cho field thiếu → NPE → RuntimeException → GlobalExceptionHandler trả code 9001 → fix bằng đổi sang `jsonNode.path("name").asText("")` (`path()` trả `MissingNode` không bao giờ null)
    - **Bug 3 — "getTmdbDetail is not defined":** import ở dòng 7 của `AddNewFilm.jsx` chỉ có `searchTmdb, importFromTmdb` thiếu `getTmdbDetail` → fix bằng thêm vào import statement
    - **Bug 4 — "Điền vào form" không thấy gì:** fill formik xong nhưng không chuyển tab, vẫn đứng ở tab TMDB thấy trống → fix bằng thêm state `activeTab`, đổi `defaultActiveKey` thành controlled `activeKey={activeTab} onChange={setActiveTab}`, gọi `setActiveTab('manual')` trong `handleSelectForForm`

- **Quyết định:**
  - Dùng TMDB làm nguồn metadata phim thay vì scrape CGV/Galaxy: các rạp VN không có public API, dữ liệu ghế real-time không lấy được, TMDB miễn phí và có đủ poster/mô tả/trailer
  - TMDB chỉ dùng ở tầng admin (import vào DB) thay vì fetch real-time cho user: tránh phụ thuộc TMDB uptime, user chỉ thấy phim mà rạp thực sự đang chiếu
  - Không làm microservice: dự án quy mô nhỏ/BTL, overhead infrastructure (Eureka, API Gateway, Config Server) không tương xứng; đề xuất Modular Monolith hoặc chỉ tách riêng Payment service nếu muốn thực hành
  - Dùng `JsonNode.path()` thay `JsonNode.get()` khi parse response TMDB: `path()` trả `MissingNode` an toàn, `get()` trả Java `null` gây NPE khi call `.asText()`
  - Tabs dùng controlled `activeKey` (state) thay `defaultActiveKey` để có thể tự động chuyển tab sau khi fill form

- **Còn dở / TODO:**
  - Người dùng cần chạy SQL migration: `Backend/database/migrate_tmdb_fields.sql` trên DB `cinemaweb` (ALTER TABLE thêm 3 cột)
  - Restart Spring Boot backend sau khi thêm code mới
  - Test end-to-end luồng: tìm phim TMDB → "Điền vào form" → form hiện đúng data → submit lưu vào DB → Detail.jsx hiển thị trailer
  - Kiểm tra Detail.jsx với phim có `trailerUrl` dạng `youtu.be/...` (short URL) — parser hiện chỉ xử lý `youtube.com/watch?v=...`

- **Ghi chú:**
  - File backend thực tế nằm ở đường dẫn khác với git history (git status hiện path cũ đã xóa) — cần dùng `find` để locate trước khi đọc
  - `application.yaml` chứa thông tin nhạy cảm: DB password `phuong4404`, mail password `mdfabuwjbybeuaal`; `Backend/.env` chứa `VNPAY_SECRET_KEY`, `VNPAY_TMN_CODE`, `JWT_SIGNER_KEY`
  - Axios interceptor trong project đọc response theo convention `res.data.body` — mọi endpoint backend phải wrap trong `ApiResponse<T>`, không trả raw object/list
  - TMDB rate limit 40 req/10s — đủ dùng cho admin, không cần throttle thêm

## [2026-06-18 13:11] Giải thích cấu hình endpoint công khai trong SecurityConfig

- **Đã làm:**
  - Đọc và phân tích đoạn code từ `SecurityConfig.java` (dòng 26–55), cụ thể hai mảng `PUBLIC_ENDPOINTS` và `PUBLIC_ENDPOINTS_GET` trong class cấu hình Spring Security.
  - Giải thích vai trò của từng mảng:
    - `PUBLIC_ENDPOINTS`: cho phép mọi HTTP method không cần xác thực — chủ yếu các endpoint auth (`/auth/login`, `/auth/logout`, `/auth/refresh-Token`, `/auth/forget-password`, `/auth/reset-password/**`) và một số endpoint tạo/ghi (`/users/sign-up`, `/bookingFoodAndDrink`, `/booking`, `/schedule`, `/foodanddrink`).
    - `PUBLIC_ENDPOINTS_GET`: chỉ mở công khai cho HTTP GET — gồm các endpoint đọc dữ liệu dành cho khách vãng lai: danh sách phim (`/movies`, `/movies/{movieId}`), rạp chiếu (`/cinemas`, `/cinemas/{cinemaId}`), lịch chiếu (`/schedule`, `/schedule/{scheduleId}`), sơ đồ ghế (`/seatSchedule`), đồ ăn uống (`/foodanddrink`), đặt vé (`/booking/{bookingId}`), và hai endpoint thanh toán đang ở trạng thái test (`/payment/create_payment`, `/payment/payment_info`).
  - Phát hiện và nêu rõ ba vấn đề tiềm ẩn trong cấu hình hiện tại (xem mục Ghi chú).

- **Quyết định:** không có (session chỉ phân tích, không chỉnh sửa code).

- **Còn dở / TODO:**
  - Xóa `/payment/create_payment` và `/payment/payment_info` khỏi `PUBLIC_ENDPOINTS_GET` trước khi triển khai production — hai endpoint này có comment `// test` và đang bị để hở hoàn toàn.
  - Kiểm tra phần `SecurityFilterChain` bên dưới trong `SecurityConfig.java` để xác nhận `PUBLIC_ENDPOINTS_GET` thực sự chỉ áp dụng cho method GET (phụ thuộc vào cách gọi `requestMatchers`).
  - Làm sạch phần trùng lặp: `/booking`, `/bookingFoodAndDrink`, `/schedule`, `/foodanddrink` xuất hiện ở cả hai mảng — cần xác định ý định (cho phép cả GET lẫn POST công khai, hay dư thừa).

- **Ghi chú:**
  - `/auth/reset-password/**` dùng wildcard `**` — đúng cú pháp Spring Security AntMatcher, nhưng cần đảm bảo không vô tình mở rộng quá mức nếu sau này thêm sub-path nhạy cảm.
  - Việc đặt tên `PUBLIC_ENDPOINTS_GET` gợi ý rõ ràng là chỉ GET, nhưng đây chỉ là convention đặt tên — bảo mật thực tế phụ thuộc hoàn toàn vào cách `requestMatchers(...).permitAll()` được cấu hình trong filter chain, không phải tên biến.

## [2026-06-18 00:14] Đánh giá thiết kế cơ sở dữ liệu Cinema

- **Đã làm:**
  - Đọc toàn bộ file `c:\Users\ADMIN\Desktop\SideProj\CinemaManagementWeb\Backend\database\cinema.sql` (MySQL dump 8.0.41, database `cinemaweb`).
  - Phân tích cấu trúc schema gồm các bảng: `cinema`, `room`, `seat`, `movie`, `schedule`, `seat_schedule`, `booking`, `booking_seat`, `bookingfoodanddrink`, `foodanddrink`, `user`, `password_otp`, `invalidated_token`.
  - Phát hiện và phân loại các vấn đề theo 4 nhóm: lỗi nghiêm trọng, vấn đề thiết kế, collation không nhất quán, naming convention.
  - **Lỗi nghiêm trọng đã chỉ ra:**
    - Bảng `foodanddrink` có 2 FK xung đột trên cùng cột `cinema_id`: `FK2bfct4r9wwpgl4ee44p4kydrn` trỏ đúng sang `cinema(cinema_id)`, nhưng `fk_foodanddrink_room` lại trỏ sai sang `room(room_id)` — lỗi do JPA tự sinh.
    - Bảng `schedule` lưu thừa `cinema_id` (có thể suy ra qua `room_id → room → cinema_id`), nguy cơ data inconsistency nếu không đồng bộ.
    - Bảng `seat_schedule` thiếu `UNIQUE(schedule_id, seat_id)` — có thể đặt trùng ghế cho cùng suất chiếu.
    - Toàn bộ 180 ghế trong `seat` có `seat_number = 0`, không phân biệt được; thiếu `UNIQUE(room_id, seat_row, seat_number)`.
    - `booking_seat.booking_id` và `booking_seat.seat_schedule_id` đang `DEFAULT NULL` thay vì `NOT NULL`.
  - **Vấn đề thiết kế đã chỉ ra:**
    - Tất cả cột tiền (`booking.price`, `booking_seat.price`, `bookingfoodanddrink.price`, `foodanddrink.fd_price`, `seat.seat_price`) dùng `DOUBLE` — cần đổi sang `DECIMAL(10,2)` để tránh lỗi làm tròn floating-point.
    - `movie_genre` lưu string thô, không thể filter hiệu quả.
    - `movie_description` dùng `varchar(255)` — quá ngắn, nên dùng `TEXT`.
    - `image_food_and_drink` dùng `LONGTEXT` — khả năng cao đang lưu base64 trực tiếp trong DB.
    - `cinema_name` nullable trong khi rạp bắt buộc phải có tên.
    - `user.gender` dùng `int` (giá trị không tường minh) — nên dùng `ENUM('MALE','FEMALE','OTHER')`.
    - `user.point` dùng `double` — nên dùng `DECIMAL` hoặc `INT`.
    - `password_otp.otp` làm Primary Key — nếu 2 user trùng OTP thì insert fail; nên dùng auto-increment PK.
    - Bảng `booking` thiếu cột `status` (không phân biệt pending/confirmed/cancelled/refunded).
    - `user_id_UNIQUE` key thừa vì PK đã đảm bảo uniqueness.
  - **Collation không nhất quán:** `booking`, `cinema`, `movie`, `user` dùng `utf8mb4_unicode_ci`; `booking_seat`, `bookingfoodanddrink`, `seat_schedule`, `invalidated_token` dùng `utf8mb4_0900_ai_ci`.
  - **Naming convention lộn xộn:** `bookingfoodanddrink` không có underscore, `fd_id/fd_name/fd_price` viết tắt không nhất quán, FK tự sinh của JPA như `FKkgseyy7t56x7lkjgu3wah5s3t` không readable.

- **Quyết định:**
  - Không có quyết định kỹ thuật nào được thực hiện trong session này — chỉ dừng ở bước phân tích và đánh giá.

- **Còn dở / TODO:**
  - Fix FK xung đột trên `foodanddrink.cinema_id`: xóa `fk_foodanddrink_room`, giữ lại `FK2bfct4r9wwpgl4ee44p4kydrn`.
  - Thêm `UNIQUE(schedule_id, seat_id)` vào bảng `seat_schedule`.
  - Migrate toàn bộ cột tiền từ `DOUBLE` sang `DECIMAL(10,2)`.
  - Cập nhật data `seat.seat_number` từ 0 về giá trị thực, thêm `UNIQUE(room_id, seat_row, seat_number)`.
  - Thêm cột `status ENUM('PENDING','CONFIRMED','CANCELLED','REFUNDED')` vào bảng `booking`.
  - Quyết định cách xử lý `image_food_and_drink`: migrate sang lưu đường dẫn/URL thay vì base64.
  - Thống nhất collation toàn bộ schema về `utf8mb4_unicode_ci`.
  - Đánh giá xem có nên bỏ `cinema_id` khỏi `schedule` hay giữ lại (denormalization có chủ đích) kèm trigger/constraint đảm bảo consistency.

- **Ghi chú:**
  - FK xung đột trên `foodanddrink` là dấu hiệu điển hình của việc dùng JPA `@ManyToOne` tự sinh DDL, sau đó lại manually thêm FK — hai nguồn DDL chồng lên nhau.
  - Schema có kiến trúc phân layer hợp lý (`cinema → room → seat → schedule → booking`), các vấn đề chủ yếu nằm ở data type và thiếu constraint, không phải cấu trúc quan hệ.
  - Cần cẩn thận khi migrate `DOUBLE → DECIMAL` nếu đã có data production, vì giá trị floating-point có thể bị làm tròn không mong muốn khi cast.

## [2026-06-17 08:34] Khảo sát thiết kế database dự án quản lý rạp chiếu phim

- **Đã làm:**
  - Đọc và phân tích toàn bộ file `Backend/database/cinema.sql` (MySQL dump 8.0.41, database `cinemaweb`).
  - Xác định schema gồm **14 bảng**, phân thành 4 nhóm chức năng:
    - **Người dùng & Phân quyền:** `user`, `user_roles`, `role`, `role_permissions`, `permission`, `password_otp`, `invalidated_token` — RBAC đầy đủ với blacklist JWT qua bảng `invalidated_token`.
    - **Rạp & Phòng:** `cinema`, `room`, `seat`, `foodanddrink` — `seat` lưu `seat_type`, `seat_row`, `seat_number`, `seat_price`; `foodanddrink` gắn FK `cinema_id`.
    - **Phim & Lịch chiếu:** `movie`, `schedule`, `seat_schedule` — `seat_schedule` là bảng trung gian lưu snapshot trạng thái ghế (`seat_state` 0/1) riêng cho từng suất chiếu.
    - **Đặt vé:** `booking`, `booking_seat`, `bookingfoodanddrink` — `booking_seat` lưu giá từng ghế tại thời điểm đặt; `bookingfoodanddrink` lưu đồ ăn/uống kèm số lượng và giá.
  - Vẽ sơ đồ quan hệ và luồng đặt vé: `user → booking → schedule → booking_seat → seat_schedule` (cập nhật `seat_state=1`), tùy chọn thêm `bookingfoodanddrink → foodanddrink`.
  - Ghi nhận các điểm kỹ thuật đáng lưu ý trong schema (xem phần Ghi chú).

- **Quyết định:** không có (session chỉ là đọc hiểu, không thay đổi code hay schema).

- **Còn dở / TODO:**
  - Kiểm tra lại constraint trùng trên `foodanddrink.cinema_id` — bảng này có vẻ đang có 2 FK constraint chồng nhau (trỏ vào cả `cinema` lẫn `room`), cần xem lại DDL để xác nhận lỗi thiết kế và sửa nếu cần.
  - Cột `user.point` tồn tại nhưng chưa có logic tích lũy/sử dụng điểm ở bất kỳ đâu — cần quyết định implement hay bỏ.
  - Chưa xác nhận có đủ index trên các cột JOIN thường dùng: `booking.user_id`, `booking.schedule_id`, `seat_schedule.schedule_id`, `seat_schedule.seat_id`.

- **Ghi chú:**
  - `seat_schedule` là thiết kế đúng đắn: tạo bản ghi riêng cho mỗi cặp (suất chiếu, ghế) thay vì dùng `seat.state` chung — tránh race condition khi nhiều suất cùng dùng một phòng.
  - `booking_seat.price` và `bookingfoodanddrink.price` lưu giá tại thời điểm đặt — phòng ngừa sai lệch khi `seat_price` hoặc `fd_price` bị cập nhật sau.
  - `invalidated_token` dùng để blacklist JWT sau logout — cần lưu ý cleanup định kỳ các record đã hết `expiry_time` để tránh bảng phình to.
  - Lỗi thiết kế nghi ngờ: `foodanddrink` có thể đang FK vào cả `cinema` và `room` cùng lúc qua cùng cột `cinema_id` — nếu đúng thì là constraint thừa/sai, cần verify trực tiếp trên DDL.

## [2026-06-15 23:17] Khảo sát kiến trúc backend: dependency, DB index, auth, phân quyền, concurrency

- **Đã làm:**
  - Xác định vị trí thư viện MySQL connector trong `pom.xml:53-57`:
    - Artifact là `com.mysql:mysql-connector-j` (tên mới, đổi từ `mysql:mysql-connector-java`), scope `runtime`, version do `spring-boot-starter-parent` quản lý tự động.
  - Phân tích index trong `Backend/database/cinema.sql`:
    - Hầu hết FK đều có `KEY` đi kèm (ví dụ `fk_booking_schedule_idx` trên `booking.schedule_id`, `fk_room_cinema_idx` trên `room.cinema_id`, composite PK trên `role_permissions`/`user_roles`).
    - Phát hiện: cột `booking.user_id` có FK constraint (`FKkgseyy7t56x7lkjgu3wah5s3t` tới `user.user_id`) nhưng **không có KEY/index riêng** — khác với các FK khác trong cùng bảng.
    - Bảng `user` có 3 UNIQUE index: `username_UNIQUE`, `email_UNIQUE`, `user_id_UNIQUE` (cinema.sql:460-462).
    - Các bảng nhỏ (`cinema`, `movie`, `role`, `permission`, `invalidated_token`) chỉ có PRIMARY KEY — hợp lý do kích thước nhỏ.
  - Giải thích lý do cần đánh index trên cột FK: tốc độ JOIN, tránh full table scan khi filter, giảm lock phạm vi rộng khi DELETE/UPDATE ở bảng cha có `ON DELETE CASCADE`.
  - Phân tích cơ chế refresh token (`src/main/java/.../service/AuthenticationService.java:156-173`, controller `AuthenticationController.java:47-53`, DTO `RefreshTokenRequest.java`):
    - Endpoint `POST /refresh-Token` nhận **chính access token JWT cũ**, verify còn hạn + chữ ký hợp lệ + chưa bị blacklist.
    - Lưu `jwtId` cũ vào bảng `invalidated_token` để vô hiệu hóa, rồi generate JWT mới.
    - Không có bảng `refresh_token` riêng; cơ chế thực chất là "rotate JWT hiện tại" — chỉ hoạt động khi access token **chưa hết hạn**, không đạt được lợi ích cốt lõi của refresh token chuẩn (kéo dài session sau khi access token hết hạn).
  - Phân tích phân quyền Spring Security (`SecurityConfig.java`, `AuthenticationService.java:221-233`, các controller):
    - `buildScope()` nhúng cả `ROLE_<name>` lẫn tên từng permission vào claim `scope` của JWT, cách nhau bởi khoảng trắng.
    - `SecurityConfig` set `authorityPrefix = ""` → mỗi token trong `scope` thành 1 `GrantedAuthority` nguyên bản (không bị prefix `SCOPE_`).
    - Tất cả `@PreAuthorize` trong controllers hiện chỉ dùng `hasRole('ADMIN')` — chưa có `hasAuthority('XXX_PERMISSION')` nào, dù DB và JWT đã mang đủ dữ liệu permission.
    - `UserController` có thêm `@PostAuthorize("hasRole('ADMIN') || returnObject.body.username == authentication.name")` — kiểm tra "chính chủ".
  - Phân tích xử lý concurrency đặt ghế (`BookingService.java:89-145`, `SeatScheduleRepository.java`, `SeatSchedule.java`, schema `cinema.sql:62`):
    - Phát hiện 3 lỗ hổng: (1) `createBooking()` không có `@Transactional` — mỗi `save()` là transaction riêng, dữ liệu rác nếu lỗi giữa chừng; (2) `findBySeatScheduleId` không lock, code không check `seatState` hiện tại trước khi set `true`; (3) `booking_seat.seat_schedule_id` chỉ là `KEY` thường, không phải `UNIQUE` → DB cho phép insert nhiều booking_seat cùng seat_schedule_id.
    - Race condition thực tế: 2 user đọc cùng 1 ghế (`seatState=false`) đồng thời, cả 2 đều set `true` và INSERT thành công → double-booking.
  - Đề xuất fix concurrency (chưa implement): thêm `@Transactional` cho `createBooking`, thêm method `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`) trong `SeatScheduleRepository`, check `seatState == false` sau khi lock, thêm `UNIQUE` constraint trên `booking_seat.seat_schedule_id` làm lớp bảo vệ cuối.
  - Session bị dừng tại câu hỏi "cách thiết kế db" do lỗi API 403.

- **Quyết định:**
  - Không implement fix concurrency hay upgrade refresh token trong session này — chỉ phân tích và đề xuất, chờ xác nhận từ người dùng.
  - Pessimistic locking được đề xuất thay vì optimistic locking (`@Version`) vì booking cần báo lỗi ngay cho user (ghế đã hết) chứ không nên retry tự động.

- **Còn dở / TODO:**
  - Câu hỏi về thiết kế DB (`cinema.sql`) bị gián đoạn do lỗi API — cần trả lời trong session tiếp theo.
  - Chưa implement: (1) index `booking.user_id` (`ALTER TABLE booking ADD KEY fk_booking_user_idx (user_id)`); (2) fix double-booking trong `BookingService.createBooking`; (3) nâng cấp cơ chế refresh token nếu cần.
  - Nếu thêm role mới (`STAFF`, `MANAGER`), cần đổi `@PreAuthorize` từ `hasRole('ADMIN')` sang `hasAuthority('<PERMISSION_NAME>')` ở các controller liên quan — cụ thể là `FoodAndDrinkController.java:36,43` và các controller khác dùng `hasRole`.

- **Ghi chú:**
  - Lỗi `InputValidationError: Grep failed ... An unexpected parameter -rn was provided` khi dùng `-rn: "true"` — tham số cần truyền đúng format của tool (dùng `-n: true` riêng, không gộp `-rn`).
  - Lỗi `API Error: 403 Request not allowed` ở cuối session cắt ngang câu trả lời về thiết kế DB — không rõ nguyên nhân, cần retry.
  - Hạ tầng permission đã hoàn chỉnh (DB bảng `permission`/`role_permissions` + JWT mang permission) nhưng chưa được khai thác ở tầng authorization controller — là technical debt tiềm ẩn khi scale thêm role.
  - Token JWT có thời hạn 1 giờ (`plus(1, ChronoUnit.HOURS)` tại `AuthenticationService.java:200`) — ngắn nhưng refresh chỉ dùng được khi token còn hạn, nên user vẫn có thể bị đăng xuất nếu không dùng đúng lúc.
