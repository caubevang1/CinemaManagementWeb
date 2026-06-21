import React, { useEffect, useState } from 'react';
import { useDispatch } from 'react-redux';
import { Tabs, Badge } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { openWindow } from '../../redux/reducers/ChatReducer';
import {
    LayDanhSachBanBe,
    LayLoiMoiKetBan,
    GuiLoiMoiKetBan,
    ChapNhanLoiMoi,
    TuChoiLoiMoi,
    HuyKetBan,
} from '../../services/FriendService';
import { TimKiemNguoiDung } from '../../services/UserService';
import { SwalConfig, confirmSwal } from '../../utils/config';
import UserAvatar from '../../components/UserAvatar';

const FriendInfo = () => {
    const dispatch = useDispatch();
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
        } catch (e) {
            setFriends([]);
        }
    };

    const loadRequests = async () => {
        try {
            const res = await LayLoiMoiKetBan();
            setRequests(res.data.body || []);
        } catch (e) {
            setRequests([]);
        }
    };

    useEffect(() => {
        loadFriends();
        loadRequests();
    }, []);

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
        } catch (e) {
            SwalConfig(e?.response?.data?.message || 'Không thể gửi lời mời', 'error', true);
        }
    };

    const handleAccept = async (id) => {
        try {
            await ChapNhanLoiMoi(id);
            SwalConfig('Đã chấp nhận lời mời', 'success', false);
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

    const handleUnfriend = async (id) => {
        const ok = await confirmSwal('Hủy kết bạn?', 'Bạn sẽ không còn là bạn bè với người này.');
        if (!ok) return;
        try {
            await HuyKetBan(id);
            await loadFriends();
        } catch (e) {
            SwalConfig(e?.response?.data?.message || 'Không thể hủy kết bạn', 'error', true);
        }
    };

    const Row = ({ avatar, title, subtitle, actions }) => (
        <div className="flex items-center gap-3 p-3 border-b border-gray-100 hover:bg-gray-50 rounded-lg">
            <UserAvatar size={48} avatar={avatar} name={title} />
            <div className="flex-1 min-w-0">
                <p className="font-semibold text-gray-800 truncate">{title}</p>
                {subtitle && <p className="text-gray-500 text-sm truncate">{subtitle}</p>}
            </div>
            <div className="flex gap-2 shrink-0">{actions}</div>
        </div>
    );

    const btn = (label, onClick, color) => (
        <button
            onClick={onClick}
            className={`${color} text-white text-sm font-semibold px-4 py-1.5 rounded transition`}
        >
            {label}
        </button>
    );

    const findTab = (
        <div className="max-w-2xl mx-auto">
            <div className="flex gap-2 mb-4">
                <input
                    type="text"
                    value={keyword}
                    onChange={(e) => setKeyword(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                    placeholder="Tìm theo tên, username hoặc email..."
                    className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:border-orange-400"
                />
                <button
                    onClick={handleSearch}
                    disabled={searching}
                    className="bg-orange-500 hover:bg-orange-600 disabled:opacity-60 text-white font-semibold px-5 py-2 rounded-lg flex items-center gap-2"
                >
                    <SearchOutlined /> Tìm
                </button>
            </div>
            {results.map((u) => (
                <Row
                    key={u.id}
                    avatar={u.avatar}
                    title={`${u.firstName || ''} ${u.lastName || ''}`.trim() || u.username}
                    subtitle={`@${u.username}`}
                    actions={btn('Kết bạn', () => handleSendRequest(u.id), 'bg-blue-500 hover:bg-blue-600')}
                />
            ))}
            {searched && results.length === 0 && (
                <p className="text-gray-500 text-center py-6">Không tìm thấy người dùng nào.</p>
            )}
        </div>
    );

    const requestsTab = (
        <div className="max-w-2xl mx-auto">
            {requests.length === 0 ? (
                <p className="text-gray-500 text-center py-6">Không có lời mời nào.</p>
            ) : (
                requests.map((r) => (
                    <Row
                        key={r.friendshipId}
                        avatar={r.otherAvatar}
                        title={r.otherUsername}
                        subtitle="Đã gửi lời mời kết bạn"
                        actions={
                            <>
                                {btn('Chấp nhận', () => handleAccept(r.friendshipId), 'bg-green-500 hover:bg-green-600')}
                                {btn('Từ chối', () => handleDecline(r.friendshipId), 'bg-gray-400 hover:bg-gray-500')}
                            </>
                        }
                    />
                ))
            )}
        </div>
    );

    const friendsTab = (
        <div className="max-w-2xl mx-auto">
            {friends.length === 0 ? (
                <p className="text-gray-500 text-center py-6">Bạn chưa có người bạn nào.</p>
            ) : (
                friends.map((f) => (
                    <Row
                        key={f.friendshipId}
                        avatar={f.otherAvatar}
                        title={f.otherUsername}
                        actions={
                            <>
                                {btn('Nhắn tin', () => dispatch(openWindow({
                                    userId: f.otherUserId,
                                    username: f.otherUsername,
                                    avatar: f.otherAvatar,
                                })), 'bg-orange-500 hover:bg-orange-600')}
                                {btn('Hủy kết bạn', () => handleUnfriend(f.friendshipId), 'bg-red-500 hover:bg-red-600')}
                            </>
                        }
                    />
                ))
            )}
        </div>
    );

    const items = [
        {
            key: 'friends',
            label: <span className="font-semibold">Bạn bè ({friends.length})</span>,
            children: friendsTab,
        },
        {
            key: 'requests',
            label: (
                <Badge count={requests.length} size="small" offset={[8, 0]}>
                    <span className="font-semibold">Lời mời</span>
                </Badge>
            ),
            children: requestsTab,
        },
        {
            key: 'find',
            label: <span className="font-semibold">Tìm bạn</span>,
            children: findTab,
        },
    ];

    return (
        <div className="max-w-4xl mx-auto px-4 py-8">
            <Tabs defaultActiveKey="friends" items={items} />
        </div>
    );
};

export default FriendInfo;
