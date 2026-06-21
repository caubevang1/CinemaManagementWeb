import { http } from "../utils/baseUrl";

export const LayBinhLuanTheoPhim = (movieId) => http.get(`/comment/movie/${movieId}`)

export const TaoBinhLuan = (data) => http.post(`/comment`, data) // { movieId, content, parentId }

export const CapNhatBinhLuan = (id, data) => http.put(`/comment/${id}`, data)

export const XoaBinhLuan = (id) => http.delete(`/comment/${id}`)
