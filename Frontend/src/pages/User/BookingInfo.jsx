import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Modal } from 'antd';
import { callApiThongTinBooking } from '../../redux/reducers/UserReducer';
import moment from 'moment';
import { LayThongTinFoodAndDrinkChiTiet, LayDanhSachGheSchedule } from '../../services/BookingManager';
import { LayDanhSachBanBe } from '../../services/FriendService';
import { GuiLoiMoiChuyenVe } from '../../services/TicketTransferService';
import { SwalConfig } from '../../utils/config';
import UserAvatar from '../../components/UserAvatar';

const ThongTinBooking = () => {
    const dispatch = useDispatch();
    const bookings = useSelector(state => state.UserReducer.bookings);
    const thongTinNguoiDung = useSelector(state => state.UserReducer.thongTinNguoiDung);
    const hasTransferPin = !!thongTinNguoiDung?.hasTransferPin;
    const [foodDetails, setFoodDetails] = useState({});
    const [seatDetails, setSeatDetails] = useState({});
    const [pickerOpen, setPickerOpen] = useState(false);
    const [pickerBookingId, setPickerBookingId] = useState(null);
    const [friends, setFriends] = useState([]);
    const [sending, setSending] = useState(false);
    const [pinModalOpen, setPinModalOpen] = useState(false);
    const [selectedFriendId, setSelectedFriendId] = useState(null);
    const [pin, setPin] = useState('');

    useEffect(() => {
        dispatch(callApiThongTinBooking);
    }, [dispatch]);

    // Nạp lại danh sách vé khi có chuyển nhượng thành công (đổi quyền sở hữu).
    useEffect(() => {
        const reload = () => dispatch(callApiThongTinBooking);
        window.addEventListener('ticket-transfer-updated', reload);
        return () => window.removeEventListener('ticket-transfer-updated', reload);
    }, [dispatch]);

    const openPicker = async (bookingId) => {
        setPickerBookingId(bookingId);
        setPickerOpen(true);
        try {
            const res = await LayDanhSachBanBe();
            setFriends(res.data.body || []);
        } catch {
            setFriends([]);
        }
    };

    // Chọn bạn xong → chuyển sang bước nhập mã PIN.
    const startTransfer = (friendId) => {
        if (!hasTransferPin) {
            SwalConfig('Bạn chưa thiết lập mã PIN chuyển nhượng. Vào "Thông tin tài khoản" để đặt mã PIN.', 'warning', true, 3500);
            return;
        }
        setSelectedFriendId(friendId);
        setPickerOpen(false);
        setPin('');
        setPinModalOpen(true);
    };

    const handleTransfer = async () => {
        if (!/^\d{6}$/.test(pin)) {
            SwalConfig('Mã PIN phải gồm đúng 6 chữ số', 'error', true);
            return;
        }
        setSending(true);
        try {
            await GuiLoiMoiChuyenVe(pickerBookingId, selectedFriendId, pin);
            SwalConfig('Đã gửi lời mời chuyển nhượng vé', 'success', false);
            setPinModalOpen(false);
        } catch (e) {
            SwalConfig(e?.response?.data?.message || 'Không thể chuyển nhượng vé', 'error', true);
        } finally {
            setSending(false);
        }
    };

    useEffect(() => {
        const fetchFoodDetails = async () => {
            const foodAndDrinkIds = new Set();
            bookings.forEach(booking => {
                booking.foodAndDrinks.forEach(item => {
                    foodAndDrinkIds.add(item.foodAndDrinkId);
                });
            });

            const foodDetailsMap = {};
            for (let id of foodAndDrinkIds) {
                try {
                    const response = await LayThongTinFoodAndDrinkChiTiet(id);
                    foodDetailsMap[id] = response.data.body;
                } catch (error) {
                    console.error("Lỗi khi lấy thông tin món ăn/thức uống:", error);
                }
            }

            setFoodDetails(foodDetailsMap);
        };

        fetchFoodDetails();
    }, [bookings]);

    useEffect(() => {
        const fetchSeatDetails = async () => {
            try {
                const response = await LayDanhSachGheSchedule();
                const seatDetailsMap = {};
                response.data.body.forEach(seat => {
                    seatDetailsMap[seat.seatScheduleId] = `${seat.seatNumber}${seat.seatRow}`;
                });

                setSeatDetails(seatDetailsMap);
            } catch (error) {
                console.error("Lỗi khi lấy thông tin ghế:", error);
            }
        };

        fetchSeatDetails();
    }, []);

    const styles = {
        container: {
            padding: '30px',
            maxWidth: '900px',
            margin: '0 auto',
            fontFamily: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
            color: '#2c3e50',
            backgroundColor: '#f4f6f8'
        },
        bookingItem: {
            border: '1px solid #e0e0e0',
            borderRadius: '12px',
            padding: '20px',
            marginBottom: '25px',
            backgroundColor: '#ffffff',
            boxShadow: '0 4px 10px rgba(0,0,0,0.05)'
        },
        movieName: {
            fontSize: '22px',
            fontWeight: 700,
            marginBottom: '15px',
            color: '#37474f'
        },
        section: {
            marginBottom: '12px',
            display: 'flex',
            flexWrap: 'wrap',
            fontSize: '15px',
            lineHeight: '1.6'
        },
        label: {
            fontWeight: 'bold',
            marginRight: '6px',
            minWidth: '120px'
        },
        value: {
            flex: 1
        },
        seat: {
            display: 'inline-block',
            backgroundColor: '#e3f2fd',
            color: '#1565c0',
            padding: '6px 12px',
            marginRight: '10px',
            marginBottom: '6px',
            borderRadius: '8px',
            fontSize: '14px',
            border: '1px solid #90caf9'
        },
        foodItem: {
            fontSize: '14px',
            marginBottom: '6px',
            paddingLeft: '10px'
        },
        totalPrice: {
            marginTop: '15px',
            fontWeight: 'bold',
            color: '#d32f2f',
            fontSize: '16px',
            borderTop: '1px dashed #ccc',
            paddingTop: '10px'
        },
        transferBtn: {
            marginTop: '15px',
            backgroundColor: '#f97316',
            color: '#fff',
            border: 'none',
            borderRadius: '8px',
            padding: '10px 16px',
            fontWeight: 600,
            cursor: 'pointer'
        },
        pickBtn: {
            backgroundColor: '#f97316',
            color: '#fff',
            border: 'none',
            borderRadius: '6px',
            padding: '6px 14px',
            fontWeight: 600,
            cursor: 'pointer'
        },
        transferredTag: {
            marginTop: '15px',
            display: 'inline-block',
            backgroundColor: '#fff7ed',
            color: '#c2410c',
            border: '1px solid #fed7aa',
            borderRadius: '8px',
            padding: '8px 14px',
            fontWeight: 600
        }
    };

    return (
        <div style={styles.container}>
            {bookings.length === 0 ? (
                <p style={styles.noBookingMessage}>Không có thông tin booking nào</p>
            ) : (
                [...bookings]
                    .sort((a, b) => new Date(b.bookingDay) - new Date(a.bookingDay))
                    .map((booking, index) => (
                    <div key={index} style={styles.bookingItem}>
                        <h3 style={styles.movieName}>{booking.movieName}</h3>

                        <div style={styles.section}>
                            <span style={styles.label}>Rạp:</span>
                            <span style={styles.value}>{booking.cinemaName}</span>
                        </div>
                        <div style={styles.section}>
                            <span style={styles.label}>Phòng chiếu:</span>
                            <span style={styles.value}>{booking.roomName}</span>
                        </div>
                        <div style={styles.section}>
                            <span style={styles.label}>Ngày đặt:</span>
                            <span style={styles.value}>{moment(booking.bookingDay).format('DD/MM/YYYY HH:mm')}</span>
                        </div>

                        <div style={styles.section}>
                            <span style={styles.label}>Ghế:</span>
                            <span style={styles.value}>
                                {booking.seats.map((seat, idx) => (
                                    <div key={idx} style={{ marginBottom: '5px' }}>
                                        <span style={{ ...styles.seat }}>
                                            {seatDetails[seat.seatScheduleId] || 'Không có thông tin ghế'}
                                        </span>
                                        <span> - Giá: {seat.price.toLocaleString()} VND</span>
                                    </div>
                                ))}
                            </span>
                        </div>

                        <div style={styles.section}>
                            <span style={styles.label}>Đồ ăn/Thức uống:</span>
                            <span style={styles.value}>
                                {booking.foodAndDrinks.length === 0 ? (
                                    <span>Không có</span>
                                ) : (
                                    booking.foodAndDrinks.map((item, idx) => (
                                        <div key={idx} style={styles.foodItem}>
                                            <span>{item.quantity} x {foodDetails[item.foodAndDrinkId]?.foodAndDrinkName || 'Không có thông tin'} ({item.price.toLocaleString()} VND)</span>
                                        </div>
                                    ))
                                )}
                            </span>
                        </div>

                        <div style={styles.totalPrice}>
                            Tổng cộng: {booking.price.toLocaleString()} VND
                        </div>

                        {booking.transferredToUsername ? (
                            <div style={styles.transferredTag}>
                                Đã chuyển nhượng cho <b>{booking.transferredToUsername}</b>
                            </div>
                        ) : (
                            <button
                                style={styles.transferBtn}
                                onClick={() => openPicker(booking.bookingId)}
                            >
                                Chuyển nhượng cho bạn bè
                            </button>
                        )}
                    </div>
                ))
            )}

            <Modal
                title="Chọn bạn để chuyển nhượng vé"
                open={pickerOpen}
                onCancel={() => setPickerOpen(false)}
                footer={null}
                centered
                width={420}
            >
                {friends.length === 0 ? (
                    <p style={{ textAlign: 'center', color: '#888', padding: '16px 0' }}>
                        Bạn chưa có người bạn nào.
                    </p>
                ) : (
                    friends.map((f) => (
                        <div
                            key={f.friendshipId}
                            style={{
                                display: 'flex', alignItems: 'center', gap: 12,
                                padding: '10px 6px', borderBottom: '1px solid #f1f1f1',
                            }}
                        >
                            <UserAvatar size={40} avatar={f.otherAvatar} name={f.otherUsername} />
                            <span style={{ flex: 1, fontWeight: 600 }}>{f.otherUsername}</span>
                            <button
                                style={styles.pickBtn}
                                onClick={() => startTransfer(f.otherUserId)}
                            >
                                Chuyển
                            </button>
                        </div>
                    ))
                )}
            </Modal>

            <Modal
                title="Nhập mã PIN chuyển nhượng"
                open={pinModalOpen}
                onCancel={() => setPinModalOpen(false)}
                onOk={handleTransfer}
                okText="Xác nhận"
                cancelText="Hủy"
                confirmLoading={sending}
                centered
                width={360}
            >
                <p style={{ marginBottom: 12, color: '#555' }}>
                    Nhập mã PIN 6 số của bạn để xác nhận chuyển nhượng vé.
                </p>
                <input
                    type="password"
                    inputMode="numeric"
                    maxLength={6}
                    value={pin}
                    onChange={(e) => setPin(e.target.value.replace(/\D/g, ''))}
                    onKeyDown={(e) => e.key === 'Enter' && handleTransfer()}
                    placeholder="••••••"
                    style={{
                        width: '100%', padding: '10px 14px', fontSize: 20, letterSpacing: 8,
                        textAlign: 'center', border: '1px solid #e5e7eb', borderRadius: 8, outline: 'none',
                    }}
                />
            </Modal>
        </div>
    );
};

export default ThongTinBooking;
