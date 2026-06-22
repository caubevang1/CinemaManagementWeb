import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { Tabs, Badge } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import {
    LayDanhSachBanBe, LayLoiMoiKetBan, GuiLoiMoiKetBan, ChapNhanLoiMoi, TuChoiLoiMoi,
} from '../../services/FriendService';
import { TimKiemNguoiDung } from '../../services/UserService';
import { SwalConfig } from '../../utils/config';
import UserAvatar from '../UserAvatar';

const fullName = (u) => `${u.firstName || ''} ${u.lastName || ''}`.trim() || u.username;

const FriendsSidebar = ({ onSelectFriend, selectedId }) => {
    const onlineIds = useSelector((s) => s.ChatReducer.onlineIds);
    const unreadByFriend = useSelector((s) => s.ChatReducer.unreadByFriend);

    const [friends, setFriends] = useState([]);
    const [requests, setRequests] = useState([]);
    const [keyword, setKeyword] = useState('');
    const [results, setResults] = useState([]);
    const [searching, setSearching] = useState(false);
    const [searched, setSearched] = useState(false);

    const loadFriends = async () => {
        try {
            const res = await LayDanhSachBanBe();
            setFriends(res.data.body || []);
        } catch { setFriends([]); }
    };

    const loadRequests = async () => {
        try {
            const res = await LayLoiMoiKetBan();
            setRequests(res.data.body || []);
        } catch { setRequests([]); }
    };

    useEffect(() => {
        loadFriends();
        loadRequests();
    }, []);

    const handleKeywordChange = (e) => {
        const v = e.target.value;
        setKeyword(v);
        if (!v.trim()) {
            setSearched(false);
            setResults([]);
        }
    };

    const handleSearch = async () => {
        if (!keyword.trim()) return;
        try {
            setSearching(true);
            const res = await TimKiemNguoiDung(keyword.trim());
            setResults(res.data.body || []);
            setSearched(true);
        } catch (e) {
            SwalConfig(e?.response?.data?.message || 'Không thể tìm kiếm', 'error', true);
        } finally {
            setSearching(false);
        }
    };

    const handleSendRequest = async (addresseeId) => {
        try {
            await GuiLoiMoiKetBan(addresseeId);
            SwalConfig('Đã gửi lời mời kết bạn', 'success', false);
            // Đổi ngay nút sang trạng thái chờ.
            setResults((prev) => prev.map((u) =>
                u.id === addresseeId ? { ...u, friendshipStatus: 'PENDING' } : u));
        } catch (e) {
            SwalConfig(e?.response?.data?.message || 'Không thể gửi lời mời', 'error', true);
        }
    };

    const handleAccept = async (id) => {
        try {
            await ChapNhanLoiMoi(id);
            await Promise.all([loadFriends(), loadRequests()]);
        } catch (e) {
            SwalConfig(e?.response?.data?.message || 'Không thể chấp nhận', 'error', true);
        }
    };

    const handleDecline = async (id) => {
        try {
            await TuChoiLoiMoi(id);
            await loadRequests();
        } catch (e) {
            SwalConfig(e?.response?.data?.message || 'Không thể từ chối', 'error', true);
        }
    };

    // Hàng bạn bè (có thể bấm để mở cuộc trò chuyện)
    const friendRow = ({ userId, username, avatar }) => {
        const online = onlineIds.includes(userId);
        const unread = unreadByFriend[userId] || 0;
        const active = selectedId === userId;
        return (
            <button
                key={userId}
                className={`messages-sidebar__item ${active ? 'is-active' : ''}`}
                onClick={() => onSelectFriend({ userId, username, avatar })}
            >
                <div className="chat-window__avatar-wrap">
                    <UserAvatar size={44} avatar={avatar} name={username} />
                    <span className={`chat-dot ${online ? 'chat-dot--on' : ''}`} />
                </div>
                <span className="messages-sidebar__name">{username}</span>
                {unread > 0 && <Badge count={unread} />}
            </button>
        );
    };

    // Hàng kết quả tìm kiếm, tùy theo trạng thái quan hệ
    const renderSearchRow = (u) => {
        if (u.friendshipStatus === 'ACCEPTED') {
            return friendRow({ userId: u.id, username: u.username, avatar: u.avatar });
        }
        return (
            <div key={u.id} className="messages-sidebar__item messages-sidebar__item--static">
                <UserAvatar size={44} avatar={u.avatar} name={fullName(u)} />
                <div className="messages-sidebar__name-wrap">
                    <span className="messages-sidebar__name">{fullName(u)}</span>
                    <span className="messages-sidebar__sub">@{u.username}</span>
                </div>
                {u.friendshipStatus === 'PENDING' ? (
                    <button className="msg-btn msg-btn--pending" disabled>Đang chờ kết bạn</button>
                ) : (
                    <button className="msg-btn msg-btn--accept" onClick={() => handleSendRequest(u.id)}>Kết bạn</button>
                )}
            </div>
        );
    };

    const conversationTab = (
        <div className="messages-sidebar__pane">
            <div className="messages-sidebar__search">
                <input
                    type="text"
                    value={keyword}
                    onChange={handleKeywordChange}
                    onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                    placeholder="Tìm bạn theo tên, username hoặc email..."
                />
                <button onClick={handleSearch} disabled={searching}><SearchOutlined /></button>
            </div>
            <div className="messages-sidebar__list">
                {searched ? (
                    results.length === 0 ? (
                        <p className="messages-sidebar__empty">Không tìm thấy người dùng nào.</p>
                    ) : (
                        results.map((u) => renderSearchRow(u))
                    )
                ) : (
                    friends.length === 0 ? (
                        <p className="messages-sidebar__empty">Bạn chưa có người bạn nào.</p>
                    ) : (
                        friends.map((f) => friendRow({
                            userId: f.otherUserId, username: f.otherUsername, avatar: f.otherAvatar,
                        }))
                    )
                )}
            </div>
        </div>
    );

    const requestsTab = (
        <div className="messages-sidebar__pane">
            <div className="messages-sidebar__list">
                {requests.length === 0 ? (
                    <p className="messages-sidebar__empty">Không có lời mời nào.</p>
                ) : (
                    requests.map((r) => (
                        <div key={r.friendshipId} className="messages-sidebar__item messages-sidebar__item--static">
                            <UserAvatar size={44} avatar={r.otherAvatar} name={r.otherUsername} />
                            <span className="messages-sidebar__name">{r.otherUsername}</span>
                            <div className="messages-sidebar__actions">
                                <button className="msg-btn msg-btn--accept" onClick={() => handleAccept(r.friendshipId)}>Chấp nhận</button>
                                <button className="msg-btn msg-btn--decline" onClick={() => handleDecline(r.friendshipId)}>Từ chối</button>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );

    const items = [
        { key: 'chat', label: 'Trò chuyện', children: conversationTab },
        {
            key: 'requests',
            label: <Badge count={requests.length} size="small" offset={[10, 0]}>Lời mời</Badge>,
            children: requestsTab,
        },
    ];

    return (
        <div className="messages-sidebar">
            <Tabs defaultActiveKey="chat" items={items} className="messages-sidebar__tabs" />
        </div>
    );
};

export default FriendsSidebar;
