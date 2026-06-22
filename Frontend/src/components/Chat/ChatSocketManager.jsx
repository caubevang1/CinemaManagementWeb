import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { LayCuocTroChuyen, LaySoTinChuaDoc, LayBanBeOnline } from '../../services/ChatService';
import {
    setConversations, setUnread, setOnlineIds, resetChat,
} from '../../redux/reducers/ChatReducer';
import { connectChatSocket, disconnectChatSocket } from '../../utils/chatSocket';

/**
 * Quản lý kết nối STOMP toàn cục (không render UI). Giữ chat realtime hoạt động
 * trên mọi trang: badge tin chưa đọc, toast lời mời chuyển vé... kể cả khi không
 * ở trang /messages.
 */
const ChatSocketManager = () => {
    const dispatch = useDispatch();
    const { isLogin, thongTinNguoiDung } = useSelector((s) => s.UserReducer);
    const myId = thongTinNguoiDung?.id || thongTinNguoiDung?.ID;

    useEffect(() => {
        if (!isLogin || !myId) {
            disconnectChatSocket();
            dispatch(resetChat());
            return;
        }
        connectChatSocket(dispatch, myId);
        (async () => {
            try {
                const [conv, unread, online] = await Promise.all([
                    LayCuocTroChuyen(), LaySoTinChuaDoc(), LayBanBeOnline(),
                ]);
                dispatch(setConversations(conv.data.body || []));
                dispatch(setUnread(unread.data.body || { total: 0, byFriend: {} }));
                dispatch(setOnlineIds(online.data.body || []));
            } catch {
                /* im lặng: vẫn dùng được khi vài API lỗi */
            }
        })();
        return () => disconnectChatSocket();
    }, [isLogin, myId, dispatch]);

    return null;
};

export default ChatSocketManager;
