import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import moment from 'moment';
import { ChapNhanChuyenVe, TuChoiChuyenVe, HuyChuyenVe } from '../../services/TicketTransferService';
import { updateTransferInMessages } from '../../redux/reducers/ChatReducer';
import { SwalConfig } from '../../utils/config';

// Bong bóng tin nhắn loại TRANSFER trong dòng hội thoại.
const TicketOfferCard = ({ transfer, myId }) => {
    const dispatch = useDispatch();
    const [busy, setBusy] = useState(false);

    if (!transfer) return null;

    const isRecipient = transfer.toUserId === myId;
    const pending = transfer.status === 'PENDING';

    const run = async (fn) => {
        setBusy(true);
        try {
            const res = await fn();
            if (res?.data?.body) dispatch(updateTransferInMessages(res.data.body));
            window.dispatchEvent(new CustomEvent('ticket-transfer-updated'));
        } catch (e) {
            SwalConfig(e?.response?.data?.message || 'Thao tác thất bại', 'error', true);
        } finally {
            setBusy(false);
        }
    };

    let footer;
    if (pending && isRecipient) {
        footer = (
            <div className="ticket-offer__actions">
                <button className="ticket-offer__accept" disabled={busy}
                    onClick={() => run(() => ChapNhanChuyenVe(transfer.id))}>Chấp nhận</button>
                <button className="ticket-offer__decline" disabled={busy}
                    onClick={() => run(() => TuChoiChuyenVe(transfer.id))}>Từ chối</button>
            </div>
        );
    } else if (pending) {
        footer = (
            <div className="ticket-offer__status">
                <span>Đang chờ {transfer.toUsername} xác nhận</span>
                <button className="ticket-offer__cancel" disabled={busy}
                    onClick={() => run(() => HuyChuyenVe(transfer.id))}>Thu hồi</button>
            </div>
        );
    } else if (transfer.status === 'ACCEPTED') {
        footer = <div className="ticket-offer__resolved ok">✓ {transfer.toUsername} đã nhận vé</div>;
    } else if (transfer.status === 'DECLINED') {
        footer = <div className="ticket-offer__resolved no">✗ {transfer.toUsername} đã từ chối</div>;
    } else if (transfer.status === 'CANCELLED') {
        footer = <div className="ticket-offer__resolved">Đã thu hồi lời mời</div>;
    }

    return (
        <div className="ticket-offer">
            <div className="ticket-offer__title">🎟 Lời mời chuyển nhượng vé</div>
            <div className="ticket-offer__movie">{transfer.movieName || 'Phim'}</div>
            <div className="ticket-offer__line">{transfer.cinemaName}</div>
            <div className="ticket-offer__line">
                {transfer.scheduleStart ? moment(transfer.scheduleStart).format('DD/MM/YYYY HH:mm') : ''}
            </div>
            {transfer.seats?.length > 0 && (
                <div className="ticket-offer__seats">
                    {transfer.seats.map((s, i) => <span key={i} className="ticket-offer__seat">{s}</span>)}
                </div>
            )}
            {footer}
        </div>
    );
};

export default TicketOfferCard;
