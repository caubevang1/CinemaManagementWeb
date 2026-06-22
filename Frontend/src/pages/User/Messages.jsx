import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { NavLink } from 'react-router-dom';
import FriendsSidebar from '../../components/Chat/FriendsSidebar';
import ConversationPane from '../../components/Chat/ConversationPane';
import { setActiveConversation, clearUnread } from '../../redux/reducers/ChatReducer';

const Messages = () => {
    const dispatch = useDispatch();
    const { isLogin, thongTinNguoiDung } = useSelector((s) => s.UserReducer);
    const myId = thongTinNguoiDung?.id || thongTinNguoiDung?.ID;

    const [selectedFriend, setSelectedFriend] = useState(null);

    // Rời trang ⇒ không còn cuộc trò chuyện nào đang mở (để đếm tin chưa đọc đúng).
    useEffect(() => () => { dispatch(setActiveConversation(null)); }, [dispatch]);

    const handleSelect = (friend) => {
        setSelectedFriend(friend);
        dispatch(setActiveConversation(friend.userId));
        dispatch(clearUnread(friend.userId));
    };

    if (!isLogin) {
        return (
            <div className="messages-page messages-page--guest">
                <p>Vui lòng <NavLink to="/login" className="text-orange-500 font-semibold">đăng nhập</NavLink> để trò chuyện với bạn bè.</p>
            </div>
        );
    }

    return (
        <div className="messages-page">
            <FriendsSidebar onSelectFriend={handleSelect} selectedId={selectedFriend?.userId} />
            <div className="messages-page__main">
                {selectedFriend ? (
                    <ConversationPane friend={selectedFriend} myId={myId} />
                ) : (
                    <div className="messages-page__empty">
                        <p>Chọn một người bạn để bắt đầu trò chuyện</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Messages;
