import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { DOMAIN_BE, LOCALSTORAGE_USER } from './constant';
import { getLocalStorage, SwalConfig } from './config';
import {
    setConnected, setTyping, setUserOnline, addMessage, applyReadReceipt,
    updateTransferInMessages,
} from '../redux/reducers/ChatReducer';

let client = null;
let myId = null;

const currentToken = () => getLocalStorage(LOCALSTORAGE_USER)?.accessToken || '';

const safeParse = (body) => {
    try { return JSON.parse(body); } catch { return null; }
};

/**
 * Kết nối STOMP qua SockJS. Truyền dispatch của store và id người dùng hiện tại
 * (để xác định "friendId" ở phía bên kia mỗi tin nhắn).
 */
export const connectChatSocket = (dispatch, userId) => {
    if (client && client.active) return;
    myId = userId;

    client = new Client({
        webSocketFactory: () => new SockJS(`${DOMAIN_BE}/ws`),
        reconnectDelay: 5000,
        // Làm mới token mỗi lần (re)connect để tránh dùng token đã hết hạn.
        beforeConnect: () => {
            client.connectHeaders = { Authorization: `Bearer ${currentToken()}` };
        },
        onConnect: () => {
            dispatch(setConnected(true));

            client.subscribe('/user/queue/messages', (frame) => {
                const msg = safeParse(frame.body);
                if (!msg) return;
                const friendId = msg.senderId === myId ? msg.recipientId : msg.senderId;
                const incoming = msg.senderId !== myId;
                dispatch(addMessage({ friendId, message: msg, incoming }));
                if (incoming && msg.type === 'TRANSFER') {
                    const mv = msg.transfer?.movieName || 'phim';
                    SwalConfig(`${msg.transfer?.fromUsername || 'Bạn bè'} muốn chuyển vé "${mv}" cho bạn`, 'info', false, 2500);
                }
            });

            client.subscribe('/user/queue/typing', (frame) => {
                const signal = safeParse(frame.body);
                if (!signal) return;
                dispatch(setTyping({ friendId: signal.fromUserId, typing: signal.typing }));
            });

            client.subscribe('/user/queue/read', (frame) => {
                const receipt = safeParse(frame.body);
                if (!receipt) return;
                dispatch(applyReadReceipt(receipt));
            });

            client.subscribe('/user/queue/presence', (frame) => {
                const ev = safeParse(frame.body);
                if (!ev) return;
                dispatch(setUserOnline({ userId: ev.userId, online: ev.online }));
            });

            // Kết quả lời mời (accepted/declined/cancelled) → cập nhật bong bóng tin nhắn
            client.subscribe('/user/queue/ticket-transfer-result', (frame) => {
                const result = safeParse(frame.body);
                if (!result) return;
                dispatch(updateTransferInMessages(result));
                if (result.status === 'ACCEPTED' && result.fromUserId === myId) {
                    SwalConfig(`${result.toUsername} đã nhận vé "${result.movieName || ''}"`, 'success', false, 2500);
                } else if (result.status === 'DECLINED' && result.fromUserId === myId) {
                    SwalConfig(`${result.toUsername} đã từ chối nhận vé`, 'info', false, 2500);
                }
                // Báo cho danh sách vé tự nạp lại (đổi quyền sở hữu)
                window.dispatchEvent(new CustomEvent('ticket-transfer-updated'));
            });
        },
        onWebSocketClose: () => dispatch(setConnected(false)),
        onStompError: () => dispatch(setConnected(false)),
    });

    client.activate();
};

export const disconnectChatSocket = () => {
    if (client) {
        client.deactivate();
        client = null;
        myId = null;
    }
};

export const sendChatMessage = (recipientId, content) => {
    if (!client || !client.connected) return false;
    client.publish({ destination: '/app/chat.send', body: JSON.stringify({ recipientId, content }) });
    return true;
};

export const sendTypingSignal = (recipientId, typing) => {
    if (!client || !client.connected) return;
    client.publish({ destination: '/app/chat.typing', body: JSON.stringify({ recipientId, typing }) });
};

export const sendReadSignal = (friendId) => {
    if (!client || !client.connected) return;
    client.publish({ destination: '/app/chat.read', body: JSON.stringify({ friendId }) });
};
