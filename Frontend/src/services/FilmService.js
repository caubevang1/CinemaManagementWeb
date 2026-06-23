import { http } from "../utils/baseUrl";


export const LayDanhSachPhim = () => http.get(`/movies`)

export const LayDanhSachPhimAdmin = () => http.get(`/movies/all`)

export const LayThongTinPhimChiTiet = (id) => http.get(`/movies/${id}`)

// Tìm kiếm full-text qua RediSearch (server-side). status tùy chọn (NOW_SHOWING/COMING_SOON/ENDED).
export const TimKiemPhim = (q, status) => http.get(`/movies/search`, { params: { q, status } })

// Gợi ý autocomplete tên phim theo prefix.
export const GoiYTenPhim = (q) => http.get(`/movies/suggest`, { params: { q } })

export const themPhim = (formData) => http.post(`/movies`, formData)

export const capNhatPhim = (formData, id) => http.put(`/movies/${id}`, formData)

export const xoaPhim = (id) => http.delete(`/movies/${id}`)

