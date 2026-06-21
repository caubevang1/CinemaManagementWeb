import { http } from "../utils/baseUrl";

// Danh sách cuộc trò chuyện gần đây (kèm tin cuối, số chưa đọc, online)
export const LayCuocTroChuyen = () => http.get(`/chat/conversations`)

// Lịch sử chat với một người bạn (cũ → mới)
export const LayLichSuChat = (friendId, page = 0, size = 30) =>
    http.get(`/chat/${friendId}`, { params: { page, size } })

// Tổng số + số chưa đọc theo từng bạn
export const LaySoTinChuaDoc = () => http.get(`/chat/unread`)

// Danh sách bạn bè đang online
export const LayBanBeOnline = () => http.get(`/chat/online`)

// Đánh dấu đã đọc (fallback REST khi socket chưa kết nối)
export const DanhDauDaDoc = (friendId) => http.post(`/chat/${friendId}/read`)
