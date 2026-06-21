import { createSlice } from '@reduxjs/toolkit'

const initialState = {
    connected: false,
    conversations: [],          // [{ userId, username, avatar, lastMessage, lastMessageAt, unreadCount, online }]
    messagesByFriend: {},       // { [friendId]: [ {id, senderId, recipientId, content, sentAt, readAt} ] }
    onlineIds: [],              // [friendId]
    unreadByFriend: {},         // { [friendId]: number }
    totalUnread: 0,
    typingByFriend: {},         // { [friendId]: boolean }
    openWindows: [],            // [{ userId, username, avatar }]
}

const recomputeTotal = (state) => {
    state.totalUnread = Object.values(state.unreadByFriend).reduce((a, b) => a + (b || 0), 0);
}

const ChatReducer = createSlice({
    name: 'ChatReducer',
    initialState,
    reducers: {
        setConnected: (state, { payload }) => {
            state.connected = payload;
        },
        setConversations: (state, { payload }) => {
            state.conversations = payload || [];
        },
        setOnlineIds: (state, { payload }) => {
            state.onlineIds = payload || [];
        },
        setUserOnline: (state, { payload }) => {
            const { userId, online } = payload;
            const has = state.onlineIds.includes(userId);
            if (online && !has) state.onlineIds.push(userId);
            if (!online && has) state.onlineIds = state.onlineIds.filter(id => id !== userId);
        },
        setUnread: (state, { payload }) => {
            state.unreadByFriend = payload?.byFriend || {};
            state.totalUnread = payload?.total ?? 0;
        },
        setTyping: (state, { payload }) => {
            const { friendId, typing } = payload;
            state.typingByFriend[friendId] = typing;
        },
        setMessages: (state, { payload }) => {
            const { friendId, messages } = payload;
            state.messagesByFriend[friendId] = messages || [];
        },
        // payload: { friendId, message, incoming } — friendId là người ở phía bên kia so với current user
        addMessage: (state, { payload }) => {
            const { friendId, message, incoming } = payload;
            if (!state.messagesByFriend[friendId]) state.messagesByFriend[friendId] = [];
            // tránh trùng nếu echo về cùng id
            if (message.id && state.messagesByFriend[friendId].some(m => m.id === message.id)) return;
            state.messagesByFriend[friendId].push(message);

            const isOpen = state.openWindows.some(w => w.userId === friendId);
            if (incoming && !isOpen) {
                state.unreadByFriend[friendId] = (state.unreadByFriend[friendId] || 0) + 1;
                recomputeTotal(state);
            }
        },
        // Người bạn đã đọc tin của ta → cập nhật readAt cho các tin ta gửi cho họ
        applyReadReceipt: (state, { payload }) => {
            const { byUserId, readAt } = payload;
            const list = state.messagesByFriend[byUserId];
            if (!list) return;
            list.forEach(m => {
                if (m.recipientId === byUserId && !m.readAt) m.readAt = readAt;
            });
        },
        openWindow: (state, { payload }) => {
            if (!state.openWindows.some(w => w.userId === payload.userId)) {
                state.openWindows.push(payload);
            }
            // mở cửa sổ ⇒ coi như đã đọc
            state.unreadByFriend[payload.userId] = 0;
            recomputeTotal(state);
        },
        closeWindow: (state, { payload }) => {
            state.openWindows = state.openWindows.filter(w => w.userId !== payload);
        },
        clearUnread: (state, { payload }) => {
            state.unreadByFriend[payload] = 0;
            recomputeTotal(state);
        },
        resetChat: () => initialState,
    },
});

export const {
    setConnected,
    setConversations,
    setOnlineIds,
    setUserOnline,
    setUnread,
    setTyping,
    setMessages,
    addMessage,
    applyReadReceipt,
    openWindow,
    closeWindow,
    clearUnread,
    resetChat,
} = ChatReducer.actions;

export default ChatReducer.reducer;
