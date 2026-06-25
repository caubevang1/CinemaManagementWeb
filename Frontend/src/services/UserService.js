import { http } from "../utils/baseUrl";

export const LayThongTinTaiKhoan = () => http.get('/users/myInfo')

export const LayThongTinPhimNguoiDungEdit = (user) => http.get(`/users/${user.id}`)

export const DangNhap = userLogin => http.post('/auth/login', userLogin)

export const DangKy = userRegister => http.post('/users/sign-up', userRegister)

export const DangXuat = () => http.post('/auth/logout')

export const LayDanhSachNguoiDung = () => http.get(`/users`)

export const XoaNguoiDung = (ID) => http.delete(`/users/${ID}`)

export const LayDanhSachLoaiNguoiDung = () => http.get(`/roles`)

export const CapNhatThongTinNguoiDung = (user) => http.put(`/users/${user.id}`, user)

// Endpoint gửi OTP gọi SMTP đồng bộ (Brevo có thể mất >10s) → nới timeout riêng cho request này.
export const sendOtpEmail = (data) => http.post('/auth/forget-password', data, { timeout: 30000 });

export const resetPasswordWithOtp = (otp, data) => http.post(`/auth/reset-password/${otp}`, data);

export const LayThongTinBooking = () => http.get('/booking/myBooking')

export const TimKiemNguoiDung = (q) => http.get('/users/search', { params: { q } })

// Đặt/đổi mã PIN chuyển nhượng (currentPin bỏ trống khi đặt lần đầu)
export const CapNhatMaPinChuyenNhuong = ({ currentPin, newPin }) =>
    http.post('/users/transfer-pin', { currentPin, newPin })