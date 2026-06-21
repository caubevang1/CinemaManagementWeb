import React, { useEffect, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { CloseOutlined, SendOutlined } from '@ant-design/icons';
import UserAvatar from '../UserAvatar';
import { LayLichSuChat } from '../../services/ChatService';
import { setMessages, clearUnread, closeWindow } from '../../redux/reducers/ChatReducer';
import { sendChatMessage, sendTypingSignal, sendReadSignal } from '../../utils/chatSocket';

const formatTime = (iso) => {
    if (!iso) return '';
    const d = new Date(iso);
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
};

const ChatWindow = ({ friend, myId }) => {
    const dispatch = useDispatch();
    const messages = useSelector((s) => s.ChatReducer.messagesByFriend[friend.userId]) || [];
    const onlineIds = useSelector((s) => s.ChatReducer.onlineIds);
    const isTyping = useSelector((s) => s.ChatReducer.typingByFriend[friend.userId]);

    const [text, setText] = useState('');
    const listRef = useRef(null);
    const typingTimer = useRef(null);

    const online = onlineIds.includes(friend.userId);

    // Tải lịch sử khi mở cửa sổ.
    useEffect(() => {
        let active = true;
        (async () => {
            try {
                const res = await LayLichSuChat(friend.userId);
                if (active) dispatch(setMessages({ friendId: friend.userId, messages: res.data.body || [] }));
            } catch {
                if (active) dispatch(setMessages({ friendId: friend.userId, messages: [] }));
            }
            sendReadSignal(friend.userId);
            dispatch(clearUnread(friend.userId));
        })();
        return () => { active = false; };
    }, [friend.userId, dispatch]);

    // Cuộn xuống cuối khi có tin mới; đánh dấu đã đọc tin đến.
    useEffect(() => {
        if (listRef.current) listRef.current.scrollTop = listRef.current.scrollHeight;
        const last = messages[messages.length - 1];
        if (last && last.senderId === friend.userId) {
            sendReadSignal(friend.userId);
            dispatch(clearUnread(friend.userId));
        }
    }, [messages, friend.userId, dispatch]);

    const handleChange = (e) => {
        setText(e.target.value);
        sendTypingSignal(friend.userId, true);
        if (typingTimer.current) clearTimeout(typingTimer.current);
        typingTimer.current = setTimeout(() => sendTypingSignal(friend.userId, false), 1500);
    };

    const handleSend = () => {
        const content = text.trim();
        if (!content) return;
        const ok = sendChatMessage(friend.userId, content);
        if (ok) {
            setText('');
            sendTypingSignal(friend.userId, false);
        }
    };

    return (
        <div className="chat-window">
            <div className="chat-window__header">
                <div className="chat-window__peer">
                    <div className="chat-window__avatar-wrap">
                        <UserAvatar size={32} avatar={friend.avatar} name={friend.username} />
                        <span className={`chat-dot ${online ? 'chat-dot--on' : ''}`} />
                    </div>
                    <div className="chat-window__meta">
                        <span className="chat-window__name">{friend.username}</span>
                        <span className="chat-window__status">{online ? 'Đang hoạt động' : 'Ngoại tuyến'}</span>
                    </div>
                </div>
                <button className="chat-window__close" onClick={() => dispatch(closeWindow(friend.userId))}>
                    <CloseOutlined />
                </button>
            </div>

            <div className="chat-window__body" ref={listRef}>
                {messages.length === 0 ? (
                    <p className="chat-window__empty">Hãy bắt đầu cuộc trò chuyện 👋</p>
                ) : (
                    messages.map((m, idx) => {
                        const mine = m.senderId === myId;
                        return (
                            <div key={m.id ?? idx} className={`chat-msg ${mine ? 'chat-msg--mine' : ''}`}>
                                <div className="chat-msg__bubble">{m.content}</div>
                                <div className="chat-msg__time">
                                    {formatTime(m.sentAt)}
                                    {mine && m.readAt && <span className="chat-msg__read"> · Đã xem</span>}
                                </div>
                            </div>
                        );
                    })
                )}
                {isTyping && <div className="chat-typing">đang nhập…</div>}
            </div>

            <div className="chat-window__input">
                <input
                    type="text"
                    value={text}
                    onChange={handleChange}
                    onKeyDown={(e) => e.key === 'Enter' && handleSend()}
                    placeholder="Nhắn tin..."
                />
                <button onClick={handleSend} disabled={!text.trim()}>
                    <SendOutlined />
                </button>
            </div>
        </div>
    );
};

export default ChatWindow;
