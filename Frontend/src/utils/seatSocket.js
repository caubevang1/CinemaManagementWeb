import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { DOMAIN_BE, LOCALSTORAGE_USER } from './constant';
import { getLocalStorage } from './config';

// Client STOMP riêng cho realtime sơ đồ ghế (tách khỏi chat socket). Khi 1 user giữ/nhả/đặt ghế,
// backend broadcast tới /topic/seats/{scheduleId}; client nhận tín hiệu -> gọi lại API danh sách ghế.

let client = null;
let subscription = null;

const currentToken = () => getLocalStorage(LOCALSTORAGE_USER)?.accessToken || '';

export const connectSeatSocket = (scheduleId, onChanged) => {
    disconnectSeatSocket();

    client = new Client({
        webSocketFactory: () => new SockJS(`${DOMAIN_BE}/ws`),
        reconnectDelay: 5000,
        beforeConnect: () => {
            client.connectHeaders = { Authorization: `Bearer ${currentToken()}` };
        },
        onConnect: () => {
            subscription = client.subscribe(`/topic/seats/${scheduleId}`, () => {
                if (typeof onChanged === 'function') onChanged();
            });
        },
    });

    client.activate();
};

export const disconnectSeatSocket = () => {
    if (subscription) {
        try { subscription.unsubscribe(); } catch { /* ignore */ }
        subscription = null;
    }
    if (client) {
        client.deactivate();
        client = null;
    }
};
