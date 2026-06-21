import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import moment from 'moment';
import { Avatar } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import {
    LayBinhLuanTheoPhim,
    TaoBinhLuan,
    CapNhatBinhLuan,
    XoaBinhLuan,
} from '../../services/CommentService';
import { SwalConfig, confirmSwal } from '../../utils/config';

export default function CommentSection({ movieId }) {
    const { thongTinNguoiDung, isLogin } = useSelector((s) => s.UserReducer);

    const [comments, setComments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [newContent, setNewContent] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const [replyingTo, setReplyingTo] = useState(null);
    const [replyContent, setReplyContent] = useState('');
    const [editingId, setEditingId] = useState(null);
    const [editContent, setEditContent] = useState('');

    const currentUserId = thongTinNguoiDung?.id;
    const isAdmin = thongTinNguoiDung?.roles?.some((r) => r.name === 'ADMIN');

    const fetchComments = async () => {
        try {
            const res = await LayBinhLuanTheoPhim(movieId);
            setComments(res.data.body || []);
        } catch (e) {
            // im lặng: API public, lỗi mạng sẽ chỉ hiện danh sách rỗng
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchComments();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [movieId]);

    const canModify = (c) => isAdmin || c.authorId === currentUserId;

    const createComment = async (content, parentId = null) => {
        if (!content?.trim()) return false;
        try {
            setSubmitting(true);
            await TaoBinhLuan({ movieId: Number(movieId), content: content.trim(), parentId });
            await fetchComments();
            return true;
        } catch (e) {
            SwalConfig(e?.response?.data?.message || 'Không thể đăng bình luận', 'error', true);
            return false;
        } finally {
            setSubmitting(false);
        }
    };

    const handleSubmitRoot = async () => {
        const ok = await createComment(newContent);
        if (ok) setNewContent('');
    };

    const handleSubmitReply = async (parentId) => {
        const ok = await createComment(replyContent, parentId);
        if (ok) {
            setReplyContent('');
            setReplyingTo(null);
        }
    };

    const handleUpdate = async (c) => {
        if (!editContent?.trim()) return;
        try {
            await CapNhatBinhLuan(c.commentId, {
                movieId: Number(movieId),
                content: editContent.trim(),
                parentId: c.parentId,
            });
            setEditingId(null);
            setEditContent('');
            await fetchComments();
        } catch (e) {
            SwalConfig(e?.response?.data?.message || 'Không thể cập nhật bình luận', 'error', true);
        }
    };

    const handleDelete = async (c) => {
        const ok = await confirmSwal('Xóa bình luận?', 'Hành động này không thể hoàn tác');
        if (!ok) return;
        try {
            await XoaBinhLuan(c.commentId);
            await fetchComments();
        } catch (e) {
            SwalConfig(e?.response?.data?.message || 'Không thể xóa bình luận', 'error', true);
        }
    };

    const startEdit = (c) => {
        setEditingId(c.commentId);
        setEditContent(c.content);
        setReplyingTo(null);
    };

    const totalCount = comments.reduce(
        (sum, c) => sum + 1 + (c.replies?.length || 0),
        0
    );

    const renderComment = (c, isReply = false) => (
        <div key={c.commentId} className={`flex gap-3 ${isReply ? 'mt-4' : 'py-4 border-b border-white/10'}`}>
            <Avatar
                size={isReply ? 36 : 44}
                src={c.avatar || undefined}
                icon={<UserOutlined />}
            />
            <div className="flex-1">
                <div className="flex items-center gap-2 flex-wrap">
                    <span className="font-semibold text-white">{c.authorName}</span>
                    <span className="text-gray-400 text-sm">
                        {c.createdAt ? moment(c.createdAt, 'YYYY-MM-DD HH:mm:ss').fromNow() : ''}
                    </span>
                </div>

                {editingId === c.commentId ? (
                    <div className="mt-2">
                        <textarea
                            className="w-full rounded-lg p-2 text-gray-800 resize-none"
                            rows={2}
                            value={editContent}
                            onChange={(e) => setEditContent(e.target.value)}
                        />
                        <div className="mt-2 flex gap-2">
                            <button
                                onClick={() => handleUpdate(c)}
                                className="bg-orange-500 hover:bg-orange-600 text-white text-sm font-semibold px-4 py-1 rounded"
                            >
                                Lưu
                            </button>
                            <button
                                onClick={() => {
                                    setEditingId(null);
                                    setEditContent('');
                                }}
                                className="text-gray-300 hover:text-white text-sm px-3 py-1"
                            >
                                Hủy
                            </button>
                        </div>
                    </div>
                ) : (
                    <p className="text-gray-200 mt-1 whitespace-pre-line">{c.content}</p>
                )}

                {editingId !== c.commentId && (
                    <div className="flex items-center gap-4 mt-2 text-sm">
                        {isLogin && !isReply && (
                            <button
                                onClick={() => {
                                    setReplyingTo(replyingTo === c.commentId ? null : c.commentId);
                                    setReplyContent('');
                                }}
                                className="text-gray-400 hover:text-orange-400"
                            >
                                Trả lời
                            </button>
                        )}
                        {canModify(c) && (
                            <>
                                <button
                                    onClick={() => startEdit(c)}
                                    className="text-gray-400 hover:text-blue-400"
                                >
                                    Sửa
                                </button>
                                <button
                                    onClick={() => handleDelete(c)}
                                    className="text-gray-400 hover:text-red-400"
                                >
                                    Xóa
                                </button>
                            </>
                        )}
                    </div>
                )}

                {replyingTo === c.commentId && (
                    <div className="mt-3">
                        <textarea
                            className="w-full rounded-lg p-2 text-gray-800 resize-none"
                            rows={2}
                            placeholder="Viết câu trả lời..."
                            value={replyContent}
                            onChange={(e) => setReplyContent(e.target.value)}
                        />
                        <div className="mt-2 flex gap-2">
                            <button
                                disabled={submitting}
                                onClick={() => handleSubmitReply(c.commentId)}
                                className="bg-orange-500 hover:bg-orange-600 disabled:opacity-60 text-white text-sm font-semibold px-4 py-1 rounded"
                            >
                                Gửi
                            </button>
                            <button
                                onClick={() => {
                                    setReplyingTo(null);
                                    setReplyContent('');
                                }}
                                className="text-gray-300 hover:text-white text-sm px-3 py-1"
                            >
                                Hủy
                            </button>
                        </div>
                    </div>
                )}

                {c.replies?.length > 0 && (
                    <div className="mt-2 pl-4 border-l border-white/10">
                        {c.replies.map((r) => renderComment(r, true))}
                    </div>
                )}
            </div>
        </div>
    );

    return (
        <section className="bg-[#0b1f26] py-10">
            <div className="container mx-auto px-4 max-w-4xl">
                <h3 className="text-white font-bold text-2xl mb-6">
                    Bình luận {totalCount > 0 && <span className="text-orange-400">({totalCount})</span>}
                </h3>

                {isLogin ? (
                    <div className="flex gap-3 mb-8">
                        <Avatar size={44} src={thongTinNguoiDung?.avatar || undefined} icon={<UserOutlined />} />
                        <div className="flex-1">
                            <textarea
                                className="w-full rounded-lg p-3 text-gray-800 resize-none"
                                rows={3}
                                placeholder="Chia sẻ cảm nhận của bạn về bộ phim..."
                                value={newContent}
                                onChange={(e) => setNewContent(e.target.value)}
                            />
                            <div className="mt-2 flex justify-end">
                                <button
                                    disabled={submitting || !newContent.trim()}
                                    onClick={handleSubmitRoot}
                                    className="bg-orange-500 hover:bg-orange-600 disabled:opacity-60 text-white font-semibold px-6 py-2 rounded uppercase tracking-wide"
                                >
                                    Đăng
                                </button>
                            </div>
                        </div>
                    </div>
                ) : (
                    <p className="text-gray-300 mb-8">
                        Vui lòng <a href="/login" className="text-orange-400 font-semibold">đăng nhập</a> để bình luận.
                    </p>
                )}

                {loading ? (
                    <p className="text-gray-400">Đang tải bình luận...</p>
                ) : comments.length === 0 ? (
                    <p className="text-gray-400">Chưa có bình luận nào. Hãy là người đầu tiên!</p>
                ) : (
                    <div>{comments.map((c) => renderComment(c))}</div>
                )}
            </div>
        </section>
    );
}
