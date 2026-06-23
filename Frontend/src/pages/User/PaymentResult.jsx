import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Result, Button, Spin } from 'antd';
import { XacNhanThanhToan } from '../../services/BookingManager';

// Trang VNPay redirect về sau thanh toán. Đọc nguyên query string vnp_* và gửi cho backend
// xác thực chữ ký HMAC + cập nhật trạng thái đơn, rồi hiển thị kết quả.
const PaymentResult = () => {
    const navigate = useNavigate();
    const [state, setState] = useState('loading'); // loading | success | failed
    const [message, setMessage] = useState('');
    const calledRef = useRef(false); // tránh gọi 2 lần do StrictMode

    useEffect(() => {
        if (calledRef.current) return;
        calledRef.current = true;

        const search = window.location.search;
        if (!search) {
            setState('failed');
            setMessage('Thiếu thông tin giao dịch.');
            return;
        }
        (async () => {
            try {
                const res = await XacNhanThanhToan(search);
                const body = res?.data?.body;
                setState(body?.success ? 'success' : 'failed');
                setMessage(body?.message || '');
            } catch (error) {
                setState('failed');
                setMessage(error?.response?.data?.message || 'Xác nhận thanh toán thất bại.');
            }
        })();
    }, []);

    if (state === 'loading') {
        return (
            <div style={{ marginTop: 120, textAlign: 'center' }}>
                <Spin size="large" tip="Đang xác nhận thanh toán..." />
            </div>
        );
    }

    return (
        <div style={{ marginTop: 80 }}>
            <Result
                status={state === 'success' ? 'success' : 'error'}
                title={state === 'success' ? 'Thanh toán thành công' : 'Thanh toán thất bại'}
                subTitle={message}
                extra={[
                    <Button type="primary" key="bookings" onClick={() => navigate('/inforUser')}>
                        Xem vé của tôi
                    </Button>,
                    <Button key="home" onClick={() => navigate('/')}>
                        Về trang chủ
                    </Button>,
                ]}
            />
        </div>
    );
};

export default PaymentResult;
