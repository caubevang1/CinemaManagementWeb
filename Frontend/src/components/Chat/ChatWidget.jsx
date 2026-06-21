import React, { useEffect, useMemo, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Badge } from 'antd';
import { MessageOutlined, CloseOutlined } from '@ant-design/icons';
import UserAvatar from '../UserAvatar';
import ChatWindow from './ChatWindow';
import { LayDanhSachBanBe } from '../../services/FriendService';
import { LayCuocTroChuyen, LaySoTinChuaDoc, LayBanBeOnline } from '../../services/ChatService';
import {
    setConversations, setUnread, setOnlineIds, openWindow, resetChat,
} from '../../redux/reducers/ChatReducer';
import { connectChatSocket, disconnectChatSocket } from '../../utils/chatSocket';

const ChatWidget = () => {
    const dispatch = useDispatch();
    const { isLogin, thongTinNguoiDung } = useSelector((s) => s.UserReducer);
    const { totalUnread, unreadByFriend, onlineIds, conversations, openWindows } =
        useSelector((s) => s.ChatReducer);

    const myId = thongTinNguoiDung?.id || thongTinNguoiDung?.ID;

    const [open, setOpen] = useState(false);
    const [friends, setFriends] = useState([]);

    // Kết nối socket + nạp dữ liệu ban đầu khi đăng nhập; ngắt khi đăng xuất.
    useEffect(() => {
        if (!isLogin || !myId) {
            disconnectChatSocket();
            dispatch(resetChat());
            return;
        }
        connectChatSocket(dispatch, myId);
        (async () => {
            try {
                const [fr, conv, unread, online] = await Promise.all([
                    LayDanhSachBanBe(), LayCuocTroChuyen(), LaySoTinChuaDoc(), LayBanBeOnline(),
                ]);
                setFriends((fr.data.body || []).map((f) => ({
                    userId: f.otherUserId, username: f.otherUsername, avatar: f.otherAvatar,
                })));
                dispatch(setConversations(conv.data.body || []));
                dispatch(setUnread(unread.data.body || { total: 0, byFriend: {} }));
                dispatch(setOnlineIds(online.data.body || []));
            } catch {
                /* im lặng: widget vẫn dùng được khi vài API lỗi */
            }
        })();
        return () => disconnectChatSocket();
    }, [isLogin, myId, dispatch]);

    // Map userId -> tin nhắn cuối để hiển thị preview.
    const lastByUser = useMemo(() => {
        const map = {};
        (conversations || []).forEach((c) => { map[c.userId] = c.lastMessage; });
        return map;
    }, [conversations]);

    if (!isLogin || !myId) return null;

    return (
        <div className="chat-widget">
            <div className="chat-windows">
                {openWindows.map((f) => (
                    <ChatWindow key={f.userId} friend={f} myId={myId} />
                ))}
            </div>

            {open && (
                <div className="chat-panel">
                    <div className="chat-panel__header">
                        <span>Trò chuyện</span>
                        <button onClick={() => setOpen(false)}><CloseOutlined /></button>
                    </div>
                    <div className="chat-panel__list">
                        {friends.length === 0 ? (
                            <p className="chat-panel__empty">Bạn chưa có người bạn nào để trò chuyện.</p>
                        ) : (
                            friends.map((f) => {
                                const unread = unreadByFriend[f.userId] || 0;
                                const online = onlineIds.includes(f.userId);
                                return (
                                    <button
                                        key={f.userId}
                                        className="chat-panel__item"
                                        onClick={() => { dispatch(openWindow(f)); setOpen(false); }}
                                    >
                                        <div className="chat-window__avatar-wrap">
                                            <UserAvatar size={40} avatar={f.avatar} name={f.username} />
                                            <span className={`chat-dot ${online ? 'chat-dot--on' : ''}`} />
                                        </div>
                                        <div className="chat-panel__item-meta">
                                            <span className="chat-panel__item-name">{f.username}</span>
                                            <span className="chat-panel__item-last">
                                                {lastByUser[f.userId] || (online ? 'Đang hoạt động' : 'Ngoại tuyến')}
                                            </span>
                                        </div>
                                        {unread > 0 && <Badge count={unread} />}
                                    </button>
                                );
                            })
                        )}
                    </div>
                </div>
            )}

            <button className="chat-launcher" onClick={() => setOpen((o) => !o)}>
                <Badge count={totalUnread} offset={[-2, 2]}>
                    <MessageOutlined className="chat-launcher__icon" />
                </Badge>
            </button>
        </div>
    );
};

export default ChatWidget;
