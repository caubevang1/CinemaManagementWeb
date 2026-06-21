import React, { useState, useEffect } from 'react';
import { DatePicker, TimePicker, Select, Button, Form, Input } from 'antd';
import { layThongTinCumRap, layThongTinPhong } from '../../../services/CinemaService';
import { useFormik } from 'formik';
import useRoute from '../../../hooks/useRoute';
import dayjs from 'dayjs';
import { TaoLichChieu } from '../../../services/BookingManager';
import { SwalConfig } from '../../../utils/config';
import { LayThongTinPhimChiTiet } from '../../../services/FilmService';

export default function Showtime() {
    const { param, navigate } = useRoute();
    const [state, setState] = useState({
        cumRapChieu: [],
        phongChieu: [],
    });
    const [loading, setLoading] = useState(false);
    const [movieLength, setMovieLength] = useState(0);

    const formik = useFormik({
        initialValues: {
            movieId: param.id,
            roomId: '',
            cinemaId: '',
            scheduleDate: '',
            scheduleStart: '',
            scheduleEnd: '',
            format: '2D',
            audioType: 'SUBTITLE',
        },

        onSubmit: async (values) => {
            try {
                // Gộp ngày + giờ thành DATETIME; end tự cuộn sang ngày sau nếu qua nửa đêm.
                const datePart = dayjs(values.scheduleDate);
                const timePart = dayjs(values.scheduleStart);
                const start = datePart
                    .hour(timePart.hour())
                    .minute(timePart.minute())
                    .second(0);
                const end = start.add(movieLength + 10, 'minute');
                const roundedEnd = end.minute(Math.round(end.minute() / 5) * 5).second(0);

                const requestData = {
                    movieId: values.movieId,
                    roomId: values.roomId,
                    // cinemaId không gửi nữa — rạp suy ra từ phòng ở backend.
                    scheduleStart: start.format('YYYY-MM-DDTHH:mm:ss'),
                    scheduleEnd: roundedEnd.format('YYYY-MM-DDTHH:mm:ss'),
                    format: values.format,
                    audioType: values.audioType,
                };

                const result = await TaoLichChieu(requestData);
                SwalConfig(result.data.message, 'success', true);
                navigate('/admin/film');
            } catch (error) {
                SwalConfig(error.response?.data?.message || 'Đã có lỗi xảy ra', 'error', true, 3000);
            }
        }
    });

    useEffect(() => {
        const fetchMovieDetails = async () => {
            try {
                setLoading(true);
                const result = await LayThongTinPhimChiTiet(param.id);
                if (result?.data?.body?.movieLength) {
                    setMovieLength(result.data.body.movieLength);
                } else {
                    console.error('Không lấy được độ dài phim');
                }
            } catch (error) {
                console.error('Lỗi khi lấy thông tin phim:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchMovieDetails();
    }, [param.id]);

    useEffect(() => {
        const callApiCumRap = async () => {
            try {
                setLoading(true);
                const result = await layThongTinCumRap();
                if (Array.isArray(result.data.body)) {
                    setState(prev => ({
                        ...prev,
                        cumRapChieu: result.data.body,
                    }));
                } else {
                    console.error('Dữ liệu cụm rạp không hợp lệ:', result.data.body);
                }
            } catch (error) {
                console.error('Lỗi lấy cụm rạp:', error);
            } finally {
                setLoading(false);
            }
        };

        callApiCumRap();
    }, []);

    const handleChangeCumRap = async (cinemaId) => {
        formik.setFieldValue('cinemaId', cinemaId);
        formik.setFieldValue('roomId', '');

        try {
            setLoading(true);
            const result = await layThongTinPhong();
            const rooms = Array.isArray(result.data.body) ? result.data.body : [];
            const filteredRooms = rooms.filter(room => room.cinemaId === cinemaId);
            setState(prev => ({
                ...prev,
                phongChieu: filteredRooms,
            }));
        } catch (error) {
            console.error('Lỗi lấy phòng chiếu:', error);
        } finally {
            setLoading(false);
        }
    };

    const convertSelectCr = () => {
        return state.cumRapChieu.map(cumRap => ({
            label: cumRap.cinemaName,
            value: cumRap.cinemaId,
        }));
    };

    const convertSelectRoom = () => {
        return state.phongChieu.map(room => ({
            label: room.roomName,
            value: room.roomId,
        }));
    };

    const handleScheduleStartChange = (value) => {
        formik.setFieldValue('scheduleStart', value);
        if (value) {
            const start = dayjs(value);
            const end = start.add(movieLength + 10, 'minute');
            const roundedEnd = end.minute(Math.round(end.minute() / 5) * 5);
            formik.setFieldValue('scheduleEnd', roundedEnd);
        }
    };

    return (
        <div className="container">
            <Form
                onSubmitCapture={formik.handleSubmit}
                labelCol={{ span: 8 }}
                wrapperCol={{ span: 8 }}
                autoComplete="off"
            >
                <h3 className="text-2xl uppercase font-bold mb-4">Tạo lịch chiếu</h3>
                <Form.Item label="Tên phim">
                    <Input value={param.movieName} readOnly />
                </Form.Item>
                <Form.Item label="Mã phim">
                    <Input value={param.id} readOnly />
                </Form.Item>

                <Form.Item label="Chọn cụm rạp">
                    <Select
                        options={convertSelectCr()}
                        onChange={handleChangeCumRap}
                        placeholder="Vui lòng chọn cụm rạp"
                    />
                </Form.Item>

                <Form.Item label="Chọn phòng">
                    <Select
                        options={convertSelectRoom()}
                        onChange={(value) => formik.setFieldValue('roomId', value)}
                        placeholder="Vui lòng chọn phòng"
                    />
                </Form.Item>

                <Form.Item label="Ngày chiếu">
                    <DatePicker
                        format="YYYY-MM-DD"
                        onChange={(value) => formik.setFieldValue('scheduleDate', value)}
                        placeholder="Chọn ngày chiếu"
                    />
                </Form.Item>

                <Form.Item label="Giờ chiếu bắt đầu">
                    <TimePicker
                        format="HH:mm:ss"
                        onChange={handleScheduleStartChange}
                        placeholder="Chọn giờ bắt đầu"
                    />
                </Form.Item>

                <Form.Item label="Giờ chiếu kết thúc">
                    <TimePicker
                        format="HH:mm:ss"
                        value={formik.values.scheduleEnd ? dayjs(formik.values.scheduleEnd, 'HH:mm:ss') : null}
                        onChange={(value) => formik.setFieldValue('scheduleEnd', value)}
                        placeholder="Giờ kết thúc sẽ tự động tính toán"
                        disabled
                    />
                </Form.Item>

                <Form.Item label="Định dạng">
                    <Select
                        value={formik.values.format}
                        options={[
                            { label: '2D', value: '2D' },
                            { label: '3D', value: '3D' },
                            { label: 'IMAX', value: 'IMAX' },
                        ]}
                        onChange={(value) => formik.setFieldValue('format', value)}
                    />
                </Form.Item>

                <Form.Item label="Âm thanh">
                    <Select
                        value={formik.values.audioType}
                        options={[
                            { label: 'Phụ đề', value: 'SUBTITLE' },
                            { label: 'Lồng tiếng', value: 'DUB' },
                        ]}
                        onChange={(value) => formik.setFieldValue('audioType', value)}
                    />
                </Form.Item>

                <Form.Item label="Tác vụ">
                    <Button htmlType="submit" loading={loading}>
                        Tạo lịch chiếu
                    </Button>
                </Form.Item>
            </Form>
        </div>
    );
}
