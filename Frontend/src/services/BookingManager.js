import { http } from "../utils/baseUrl";
import { ThongTinDatVe } from "../_core/models/ThongTinDatVe";


export const LayDanhSachPhongVeService = (scheduleId) => http.get(`schedule/${scheduleId}`)

// export const DatVe = (thongTinDatVe = new ThongTinDatVe()) => http.post(`booking`, thongTinDatVe)
export const DatVe = (data) => http.post(`booking`, data)

export const TaoLichChieu = (dataLichChieu) => http.post(`schedule`, dataLichChieu)

export const LayDanhSachGhe = () => http.get(`seats`)

export const LayDanhSachGheSchedule = () => http.get(`seatSchedule`)

// Lấy ghế của riêng một suất chiếu (lọc tại DB thay vì tải toàn bộ rồi lọc ở client)
export const LayDanhSachGheTheoSuat = (scheduleId) => http.get(`seatSchedule/${scheduleId}`)

export const LayThongTinGheChiTiet = (seatId) => http.get(`seats/${seatId}`)

export const CapNhatGhe = (seatId, data) => http.put(`seats/${seatId}`, data)

export const XoaGhe = (seatId) => http.delete(`seats/${seatId}`)

export const ThemGhe = (data) => http.post(`seats`, data)

export const LayThongTinFoodAndDrink = () => http.get(`foodanddrink`)

export const LayThongTinFoodAndDrinkChiTiet = (foodAndDrinkId) => http.get(`foodanddrink/${foodAndDrinkId}`)

export const DatFoodAndDrink = (data) => http.post(`bookingFoodAndDrink`, data)

// Admin: quản lý combo (đồ ăn/thức uống) theo rạp
export const ThemFoodAndDrink = (data) => http.post(`foodanddrink`, data)

export const CapNhatFoodAndDrink = (id, data) => http.put(`foodanddrink/${id}`, data)

export const XoaFoodAndDrink = (id) => http.delete(`foodanddrink/${id}`)