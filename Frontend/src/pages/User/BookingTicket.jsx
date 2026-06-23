import React, { useEffect, useState, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Card, InputNumber, Row, Col } from 'antd';
import moment from 'moment';
import _ from 'lodash';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faXmark } from '@fortawesome/free-solid-svg-icons';
import useRoute from '../../hooks/useRoute';
import { LOCALSTORAGE_USER } from '../../utils/constant';
import { getLocalStorage, SwalConfig } from '../../utils/config';
import LoadingPage from '../LoadingPage';
import { LayDanhSachPhongVeService, DatVe, LayDanhSachGheTheoSuat, LayThongTinFoodAndDrink, GiuGhe, NhaGhe } from '../../services/BookingManager';
import { datGhe, layDanhSachPhongVe, xoaDanhSachGheDangDat } from '../../redux/reducers/BookingReducer';
import { callApiThongTinNguoiDung } from '../../redux/reducers/UserReducer';
import { layThongTinPhong } from '../../services/CinemaService';

// Chuẩn hoá 1 ghế-suất từ API về dạng dùng trong redux.
const mapSeat = (s) => ({
    seatScheduleId: s.seatScheduleId,
    seatId: s.seatId,
    seatType: s.seatType,
    seatRow: s.seatRow,
    seatNumber: s.seatNumber,
    seatPrice: s.seatPrice,
    seatState: s.seatState,   // AVAILABLE / HELD / BOOKED
    heldByMe: s.heldByMe,     // ghế đang do chính user này giữ
    heldUntil: s.heldUntil,
    username: ''
});

// Lấy mốc hết hạn sớm nhất (deadline duy nhất) trong danh sách response giữ ghế.
const earliestHeldUntil = (list) =>
    (list || []).map(s => s.heldUntil).filter(Boolean).sort()[0] || null;

const getErrMsg = (e) => e?.response?.data?.message;

const BookingTicketPage = () => {
    const dispatch = useDispatch();
    const { thongTinPhim, danhSachGhe } = useSelector(state => state.BookingReducer.chiTietPhongVe);
    const { danhSachGheDangDat } = useSelector(state => state.BookingReducer);
    const { thongTinNguoiDung } = useSelector(state => state.UserReducer);

    const { param, navigate } = useRoute();
    const [isLoading, setIsLoading] = useState(true);
    const [seatConfig, setSeatConfig] = useState({ maxSeatNumber: 9, maxSeatRow: 5 });
    const [view, setView] = useState('seat'); // 'seat' | 'combo'
    const [foodList, setFoodList] = useState([]);
    const [quantities, setQuantities] = useState({});
    const [scheduleId, setScheduleId] = useState(null);
    const [heldUntil, setHeldUntil] = useState(null);   // ISO string | null
    const [remainingMs, setRemainingMs] = useState(0);

    // Tải lại chỉ danh sách ghế (giữ nguyên thông tin phim) để cập nhật trạng thái HELD/BOOKED.
    const reloadSeatsOnly = useCallback(async () => {
        if (!scheduleId) return;
        try {
            const seatsRaw = (await LayDanhSachGheTheoSuat(scheduleId)).data.body;
            dispatch(layDanhSachPhongVe({ thongTinPhim, danhSachGhe: seatsRaw.map(mapSeat) }));
        } catch (e) {
            console.error('Lỗi tải lại ghế:', e);
        }
    }, [scheduleId, thongTinPhim, dispatch]);

    // Hết thời gian giữ ghế: nhả đồng hồ, xoá lựa chọn, quay về sơ đồ ghế, tải lại trạng thái.
    const handleExpire = useCallback(() => {
        setHeldUntil(null);
        setView('seat');
        setQuantities({});
        dispatch(xoaDanhSachGheDangDat());
        SwalConfig('Hết thời gian giữ ghế, vui lòng chọn lại', 'warning', true);
        reloadSeatsOnly();
    }, [dispatch, reloadSeatsOnly]);

    // Tải dữ liệu lần đầu + khôi phục lựa chọn/đồng hồ nếu user đang giữ ghế (sau khi F5).
    useEffect(() => {
        if (!getLocalStorage(LOCALSTORAGE_USER)) return navigate('/login');
        dispatch(callApiThongTinNguoiDung);
        (async () => {
            try {
                const sch = (await LayDanhSachPhongVeService(param.id)).data.body;
                setScheduleId(sch.scheduleId);
                const seatsRaw = (await LayDanhSachGheTheoSuat(sch.scheduleId)).data.body;
                const rooms = (await layThongTinPhong()).data.body;
                const room = rooms.find(r => r.roomId === sch.roomId);
                if (room) {
                    setSeatConfig({ maxSeatNumber: room.numCol, maxSeatRow: room.numRow });
                }

                const phim = {
                    movieId: sch.movieId,
                    movieName: sch.movieName,
                    cinemaName: sch.cinemaName,
                    roomName: room?.roomName,
                    scheduleDate: moment(sch.scheduleStart).format('DD-MM-YYYY'),
                    scheduleStart: moment(sch.scheduleStart).format('HH:mm'),
                    scheduleEnd: moment(sch.scheduleEnd).format('HH:mm')
                };

                const seats = seatsRaw.map(mapSeat);
                dispatch(layDanhSachPhongVe({ thongTinPhim: phim, danhSachGhe: seats }));

                // Khôi phục: nếu có ghế đang do mình giữ (Redis), dựng lại lựa chọn + đồng hồ.
                const mine = seats.filter(s => s.heldByMe);
                if (mine.length) {
                    mine.forEach(s => dispatch(datGhe({ ...s, username: thongTinNguoiDung?.username || '' })));
                    setHeldUntil(earliestHeldUntil(mine));
                }

                try {
                    const foods = (await LayThongTinFoodAndDrink()).data.body || [];
                    setFoodList(foods.filter(item => item.cinemaName === sch.cinemaName));
                } catch (foodErr) {
                    console.error('Lỗi khi lấy combo:', foodErr);
                }
            } catch (e) {
                console.error(e);
            } finally {
                setIsLoading(false);
            }
        })();
        return () => dispatch(xoaDanhSachGheDangDat());
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [dispatch, navigate, param.id]);

    // Đồng hồ đếm ngược: chạy khi đang giữ ghế (heldUntil != null), đồng nhất ở cả combo & thanh toán.
    useEffect(() => {
        if (!heldUntil) { setRemainingMs(0); return; }
        const tick = () => {
            const ms = new Date(heldUntil).getTime() - Date.now();
            setRemainingMs(ms);
            if (ms <= 0) handleExpire();
        };
        tick();
        const timer = setInterval(tick, 1000);
        return () => clearInterval(timer);
    }, [heldUntil, handleExpire]);

    const handleQuantityChange = (id, value) => {
        setQuantities(prev => ({ ...prev, [id]: value }));
    };

    const seatTypeColor = (seatType) => {
        const t = seatType?.toLowerCase();
        if (t === 'vip') return '#ffd700';
        if (t === 'couple') return '#ff69b4';
        return '#008000';
    };

    const formatTime = (ms) => {
        const s = Math.max(0, Math.floor(ms / 1000));
        return `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`;
    };

    const renderCombos = () => {
        if (foodList.length === 0) {
            return (
                <p style={{ textAlign: 'center', color: '#888', padding: '40px 0', fontSize: 18 }}>
                    Rạp này hiện chưa có combo nào.
                </p>
            );
        }
        return (
            <Row gutter={[24, 24]}>
                {foodList.map(item => (
                    <Col key={item.foodAndDrinkId} xs={24} sm={12} md={12} lg={8}>
                        <Card
                            title={item.foodAndDrinkName}
                            bordered={false}
                            style={{ borderRadius: 12, boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                            cover={
                                <img
                                    alt={item.foodAndDrinkName}
                                    src={item.imageFoodAndDrink}
                                    style={{
                                        height: 260,
                                        width: '100%',
                                        objectFit: 'contain',
                                        background: '#f5f5f5',
                                        borderTopLeftRadius: 12,
                                        borderTopRightRadius: 12
                                    }}
                                />
                            }
                            extra={
                                <span style={{ fontSize: 16, fontWeight: 600 }}>
                                    {item.foodAndDrinkPrice.toLocaleString()}₫
                                </span>
                            }
                        >
                            <div style={{ display: 'flex', alignItems: 'center' }}>
                                <span style={{ marginRight: 8 }}>Số lượng:</span>
                                <InputNumber
                                    min={0}
                                    value={quantities[item.foodAndDrinkId] || 0}
                                    onChange={value => handleQuantityChange(item.foodAndDrinkId, value)}
                                />
                            </div>
                        </Card>
                    </Col>
                ))}
            </Row>
        );
    };

    const renderSeats = () => {
        const { maxSeatNumber, maxSeatRow } = seatConfig;
        const seatRows = Array.from({ length: maxSeatRow }, (_, i) => String.fromCharCode(65 + i));
        const groupedSeats = _.groupBy(danhSachGhe, 'seatRow');

        return (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                {seatRows.map((row) => {
                    const rowSeats = Array.from({ length: maxSeatNumber }, (_, index) => {
                        return (groupedSeats[row] || []).find(s => Number(s.seatNumber) === index + 1);
                    });

                    return (
                        <div key={row} style={{ display: 'flex', alignItems: 'center', margin: '4px 0' }}>
                            <div style={{ width: 20, marginRight: 8, fontWeight: 'bold' }}>{row}</div>
                            {rowSeats.map((ghe, i) => {
                                if (!ghe) {
                                    return <div key={i} style={{ width: 40, height: 40, margin: 4 }} />;
                                }

                                // Đã đặt, hoặc đang được NGƯỜI KHÁC giữ -> không chọn được.
                                const heldByOther = ghe.seatState === 'HELD' && !ghe.heldByMe;
                                const unavailable = ghe.seatState === 'BOOKED' || heldByOther;
                                const selecting = danhSachGheDangDat.some(d => d.seatId === ghe.seatId);

                                let background = seatTypeColor(ghe.seatType);
                                if (unavailable) background = '#ccc';
                                else if (selecting) background = '#F97316';

                                return (
                                    <button
                                        key={ghe.seatId}
                                        disabled={unavailable}
                                        onClick={() => dispatch(datGhe({ ...ghe, username: thongTinNguoiDung?.username || '' }))}
                                        style={{
                                            width: 40,
                                            height: 40,
                                            margin: 4,
                                            borderRadius: 8,
                                            fontWeight: 'bold',
                                            background,
                                            border: 'none',
                                            color: unavailable ? '#fff' : '#000',
                                            cursor: unavailable ? 'not-allowed' : 'pointer',
                                            display: 'flex',
                                            justifyContent: 'center',
                                            alignItems: 'center',
                                            transition: 'transform 0.2s ease',
                                        }}
                                    >
                                        {unavailable ? <FontAwesomeIcon icon={faXmark} /> : ghe.seatNumber}
                                    </button>
                                );
                            })}
                        </div>
                    );
                })}

                {/* Legend */}
                <div style={{ marginTop: 80, display: 'flex', gap: 16, flexWrap: 'wrap', justifyContent: 'center' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <div style={{ width: 24, height: 24, background: '#FF69B4', borderRadius: 4 }}></div>
                        <span>Ghế tình yêu</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <div style={{ width: 24, height: 24, background: '#008000', borderRadius: 4 }}></div>
                        <span>Ghế thường</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <div style={{ width: 24, height: 24, background: '#ffd700', borderRadius: 4 }}></div>
                        <span>Ghế VIP</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <div style={{ width: 24, height: 24, background: '#F97316', borderRadius: 4 }}></div>
                        <span>Đang chọn</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <div style={{ width: 24, height: 24, background: '#ccc', borderRadius: 4 }}></div>
                        <span>Đã đặt / đang giữ</span>
                    </div>
                </div>
            </div>
        );
    };

    const handleDatVe = async () => {
        if (!danhSachGheDangDat.length) {
            return SwalConfig('Vui lòng chọn ghế', 'warning', true);
        }

        setIsLoading(true);
        try {
            const seatScheduleIds = danhSachGheDangDat.map(g => g.seatScheduleId);

            // Đảm bảo đã giữ ghế (trường hợp bấm ĐẶT VÉ thẳng từ sơ đồ ghế, chưa qua combo).
            if (!heldUntil) {
                const hold = await GiuGhe(seatScheduleIds);
                setHeldUntil(earliestHeldUntil(hold.data.body));
            }

            // Check lại lần nữa: ghế có bị người khác đặt/giữ trong lúc chọn combo không.
            const fresh = (await LayDanhSachGheTheoSuat(scheduleId)).data.body;
            const conflict = seatScheduleIds.some(id => {
                const s = fresh.find(x => x.seatScheduleId === id);
                return !s || s.seatState === 'BOOKED' || (s.seatState === 'HELD' && !s.heldByMe);
            });
            if (conflict) {
                SwalConfig('Một số ghế vừa bị người khác giữ hoặc đặt, vui lòng chọn lại', 'error');
                dispatch(xoaDanhSachGheDangDat());
                setHeldUntil(null);
                setView('seat');
                await reloadSeatsOnly();
                return;
            }

            const foodAndDrinks = Object.entries(quantities)
                .filter(([, qty]) => qty > 0)
                .map(([id, quantity]) => ({ foodAndDrinkId: Number(id), quantity }));

            const payload = {
                scheduleId: parseInt(param.id),
                userId: thongTinNguoiDung?.id || '',
                seats: seatScheduleIds.map(seatScheduleId => ({ seatScheduleId })),
                foodAndDrinks
            };

            // Tạo đơn PENDING + chuyển sang hard hold, nhận URL VNPay rồi redirect sang thanh toán.
            const res = await DatVe(payload);
            const paymentUrl = res?.data?.body?.paymentUrl;
            if (paymentUrl) {
                setHeldUntil(null);
                dispatch(xoaDanhSachGheDangDat());
                setQuantities({});
                window.location.href = paymentUrl;
                return;
            }
            SwalConfig('Không nhận được liên kết thanh toán', 'error');
        } catch (error) {
            console.error('Lỗi đặt vé:', error);
            SwalConfig(getErrMsg(error) || 'Đặt vé thất bại', 'error');
            dispatch(xoaDanhSachGheDangDat());
            setHeldUntil(null);
            setView('seat');
            await reloadSeatsOnly();
        } finally {
            setIsLoading(false);
        }
    };

    const toggleView = async () => {
        if (view === 'seat') {
            // Sang chọn combo: giữ ghế (Redis) + bật đồng hồ đếm ngược.
            if (danhSachGheDangDat.length === 0) {
                SwalConfig('Vui lòng chọn ít nhất 1 ghế để chọn combo', 'warning', true);
                return;
            }
            setIsLoading(true);
            try {
                const ids = danhSachGheDangDat.map(g => g.seatScheduleId);
                const hold = await GiuGhe(ids);
                setHeldUntil(earliestHeldUntil(hold.data.body));
                setView('combo');
            } catch (e) {
                console.error('Lỗi giữ ghế:', e);
                SwalConfig(getErrMsg(e) || 'Không giữ được ghế, vui lòng chọn lại', 'error');
                dispatch(xoaDanhSachGheDangDat());
                setHeldUntil(null);
                await reloadSeatsOnly();
            } finally {
                setIsLoading(false);
            }
        } else {
            // Quay lại chọn ghế: nhả ghế + xoá đồng hồ, đưa ghế về AVAILABLE.
            setIsLoading(true);
            try {
                const ids = danhSachGheDangDat.map(g => g.seatScheduleId);
                await NhaGhe(ids);
            } catch (e) {
                console.error('Lỗi nhả ghế:', e);
            }
            dispatch(xoaDanhSachGheDangDat());
            setHeldUntil(null);
            setQuantities({});
            await reloadSeatsOnly();
            setView('seat');
            setIsLoading(false);
        }
    };

    if (isLoading) return <LoadingPage />;

    const seatTotal = _.sumBy(danhSachGheDangDat, 'seatPrice');
    const comboTotal = foodList.reduce(
        (sum, item) => sum + (quantities[item.foodAndDrinkId] || 0) * item.foodAndDrinkPrice,
        0
    );
    const tongTien = seatTotal + comboTotal;

    return (
        <div style={{ marginTop: 80, padding: '0 16px' }}>
            {heldUntil && (
                <div style={{
                    textAlign: 'center', background: '#fff7ed', border: '1px solid #f97316',
                    color: '#c2410c', padding: '10px 16px', borderRadius: 8, marginBottom: 12,
                    fontWeight: 600, fontSize: 18
                }}>
                    ⏳ Thời gian giữ ghế còn lại: <span style={{ color: '#dc2626' }}>{formatTime(remainingMs)}</span>
                </div>
            )}
            <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 24, padding: 16 }}>
                <div>
                    {view === 'seat' ? (
                        <>
                            <div style={{ position: 'relative', textAlign: 'center', marginBottom: 16 }}>
                                <div style={{ width: '100%', height: 0, borderBottom: '40px solid #ccc', borderLeft: '60px solid transparent', borderRight: '60px solid transparent' }} />
                                <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', color: '#fff', fontWeight: '600' }}>SCREEN</div>
                            </div>
                            {renderSeats()}
                        </>
                    ) : (
                        <div>
                            <h3 style={{ fontSize: 24, fontWeight: 700, marginBottom: 16, color: '#f97316' }}>Chọn đồ ăn và thức uống</h3>
                            {renderCombos()}
                        </div>
                    )}
                </div>
                <div style={{ background: '#fff', padding: 16, borderRadius: 12, boxShadow: '0 2px 8px rgba(0,0,0,0.1)', fontSize: 20, fontWeight: '500', textAlign: 'left' }}>
                    <h3 style={{ fontSize: 30, fontWeight: '700', textAlign: 'center', color: '#f97316', marginBottom: 16, marginTop: 10, borderBottom: '2px solid #f97316' }}>
                        {tongTien.toLocaleString()} VND
                    </h3>
                    <div style={{ marginBottom: 16, marginTop: 10, fontSize: 20, fontWeight: '350', textAlign: 'left' }}>
                        <h4 style={{ fontSize: 25, fontWeight: '600', textAlign: 'center' }}>{thongTinPhim.movieName}</h4>
                        <p style={{ marginBottom: 16, marginTop: 10, color: 'grey' }}><strong style={{ color: 'black' }}>Cụm rạp:</strong> {thongTinPhim.cinemaName}</p>
                        <p style={{ marginBottom: 16, marginTop: 10, color: 'grey' }}><strong style={{ color: 'black' }}>Phòng chiếu:</strong> {thongTinPhim.roomName}</p>
                        <p style={{ marginBottom: 16, marginTop: 10, color: 'grey' }}><strong style={{ color: 'black' }}>Ngày chiếu:</strong> {thongTinPhim.scheduleDate}</p>
                        <p style={{ marginBottom: 16, marginTop: 10, color: 'grey' }}><strong style={{ color: 'black' }}>Suất chiếu:</strong> {thongTinPhim.scheduleStart} ~ {thongTinPhim.scheduleEnd}</p>
                    </div>
                    <div>
                        <strong style={{ color: 'black' }}>Ghế đã chọn:</strong>
                        <div style={{ display: 'flex', flexWrap: 'wrap', marginTop: 8 }}>
                            {_.sortBy(danhSachGheDangDat, 'seatNumber').map(g => (
                                <span key={g.seatId} style={{
                                    width: 36, height: 36, margin: 4,
                                    display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                                    flex: '0 0 auto',
                                    background: seatTypeColor(g.seatType),
                                    color: g.seatType?.toLowerCase() === 'vip' ? '#000' : '#fff',
                                    borderRadius: 4, fontSize: 14, fontWeight: 600,
                                }}>
                                    {g.seatRow}{g.seatNumber}
                                </span>
                            ))}
                        </div>
                    </div>
                    <div style={{ marginTop: 16 }}>
                        <strong style={{ color: 'black' }}>Đồ ăn/thức uống:</strong>
                        {foodList.filter(f => (quantities[f.foodAndDrinkId] || 0) > 0).length === 0 ? (
                            <div style={{ color: 'grey', fontSize: 16, marginTop: 8 }}>Chưa chọn</div>
                        ) : (
                            <div style={{ marginTop: 8, fontSize: 16 }}>
                                {foodList.filter(f => (quantities[f.foodAndDrinkId] || 0) > 0).map(f => {
                                    const qty = quantities[f.foodAndDrinkId];
                                    return (
                                        <div key={f.foodAndDrinkId} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                                            <span>{f.foodAndDrinkName} × {qty}</span>
                                            <span style={{ color: '#f97316' }}>{(f.foodAndDrinkPrice * qty).toLocaleString()}₫</span>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </div>
                    <div style={{ margin: '16px 0', fontWeight: '350' }}>
                        <p style={{ marginBottom: 16, marginTop: 10, color: 'grey' }}><strong style={{ color: 'black' }}>Email:</strong> {thongTinNguoiDung.email}</p>
                        <p style={{ marginBottom: 50, marginTop: 10, color: 'grey' }}><strong style={{ color: 'black' }}>Phone:</strong> {thongTinNguoiDung.phoneNumber}</p>
                    </div>
                    <button
                        onClick={toggleView}
                        style={{
                            width: '100%',
                            padding: '12px 0',
                            background: '#10b981',
                            border: 'none',
                            borderRadius: 8,
                            color: '#fff',
                            fontWeight: '700',
                            cursor: 'pointer',
                            marginBottom: 12,
                        }}
                    >
                        {view === 'seat' ? 'CHỌN COMBO' : 'CHỌN GHẾ'}
                    </button>
                    <button className='dat_ve_button' onClick={handleDatVe} style={{ width: '100%', padding: '12px 0', background: '#f97316', border: 'none', borderRadius: 8, color: '#fff', fontWeight: '700', cursor: 'pointer' }}>
                        ĐẶT VÉ
                    </button>
                </div>
            </div>
        </div>
    );
};

export default BookingTicketPage;
