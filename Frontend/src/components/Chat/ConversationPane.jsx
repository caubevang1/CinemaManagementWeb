import React, { useEffect, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { SendOutlined } from '@ant-design/icons';
import UserAvatar from '../UserAvatar';
import TicketOfferCard from './TicketOfferCard';
import { LayLichSuChat } from '../../services/ChatService';
import { setMessages, clearUnread } from '../../redux/reducers/ChatReducer';
import { sendChatMessage, sendTypingSignal, sendReadSignal } from '../../utils/chatSocket';

const formatTime = (iso) => {
    if (!iso) return '';
    return new Date(iso).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
};

const ConversationPane = ({ friend, myId }) => {
    const dispatch = useDispatch();
    const messages = useSelector((s) => s.ChatReducer.messagesByFriend[friend.userId]) || [];
    const onlineIds = useSelector((s) => s.ChatReducer.onlineIds);
    const isTyping = useSelector((s) => s.ChatReducer.typingByFriend[friend.userId]);

    const [text, setText] = useState('');
    const listRef = useRef(null);
    const typingTimer = useRef(null);

    const online = onlineIds.includes(friend.userId);

    // Tải lịch sử khi chọn người bạn.
    useEffect(() => {
        let active = true;
        setText('');
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

    // Cuộn xuống cuối + đánh dấu đã đọc tin đến.
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
        if (sendChatMessage(friend.userId, content)) {
            setText('');
            sendTypingSignal(friend.userId, false);
        }
    };

    return (
        <div className="conversation-pane">
            <div className="conversation-pane__header">
                <div className="chat-window__avatar-wrap">
                    <UserAvatar size={40} avatar={friend.avatar} name={friend.username} />
                    <span className={`chat-dot ${online ? 'chat-dot--on' : ''}`} />
                </div>
                <div className="conversation-pane__meta">
                    <span className="conversation-pane__name">{friend.username}</span>
                    <span className="conversation-pane__status">{online ? 'Đang hoạt động' : 'Ngoại tuyến'}</span>
                </div>
            </div>

            <div className="conversation-pane__body" ref={listRef}>
                {messages.length === 0 ? (
                    <p className="chat-window__empty">Hãy bắt đầu cuộc trò chuyện 👋</p>
                ) : (
                    messages.map((m, idx) => {
                        if (m.type === 'TRANSFER') {
                            return <TicketOfferCard key={m.id ?? idx} transfer={m.transfer} myId={myId} />;
                        }
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

            <div className="conversation-pane__input">
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

export default ConversationPane;
