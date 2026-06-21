import { http } from "../utils/baseUrl";

export const LayDanhSachBanBe = () => http.get(`/friend`)

export const LayLoiMoiKetBan = () => http.get(`/friend/requests`)

export const GuiLoiMoiKetBan = (addresseeId) => http.post(`/friend/request`, { addresseeId })

export const ChapNhanLoiMoi = (id) => http.post(`/friend/${id}/accept`)

export const TuChoiLoiMoi = (id) => http.post(`/friend/${id}/decline`)

export const HuyKetBan = (id) => http.delete(`/friend/${id}`)
