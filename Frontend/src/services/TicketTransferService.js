import { http } from "../utils/baseUrl";

// Gửi lời mời chuyển nhượng vé cho một người bạn (kèm mã PIN xác nhận của người gửi)
export const GuiLoiMoiChuyenVe = (bookingId, toUserId, pin) =>
    http.post(`/ticket-transfer`, { bookingId, toUserId, pin })

export const ChapNhanChuyenVe = (id) => http.post(`/ticket-transfer/${id}/accept`)

export const TuChoiChuyenVe = (id) => http.post(`/ticket-transfer/${id}/decline`)

export const HuyChuyenVe = (id) => http.post(`/ticket-transfer/${id}/cancel`)

// Lời mời chuyển vé đến (đang chờ xử lý)
export const LayLoiMoiChuyenVeDen = () => http.get(`/ticket-transfer/incoming`)

// Lời mời chuyển vé đã gửi
export const LayLoiMoiChuyenVeDi = () => http.get(`/ticket-transfer/outgoing`)
