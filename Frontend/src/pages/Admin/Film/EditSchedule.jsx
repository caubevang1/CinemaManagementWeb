import React, { useEffect, useState } from 'react';
import { Form, Input, Button, DatePicker, Select, Spin, message } from 'antd';
import { useDispatch, useSelector } from 'react-redux';
import { useParams, useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import {
    fetchSchedules,
    updateSchedule,
} from '../../../redux/reducers/ScheduleReducer';

const { Option } = Select;

export default function EditSchedule() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const { scheduleId } = useParams();
    const { schedules, loading } = useSelector((state) => state.ScheduleReducer);
    const [form] = Form.useForm();
    const [currentSchedule, setCurrentSchedule] = useState(null);

    useEffect(() => {
        if (!schedules || schedules.length === 0) {
            dispatch(fetchSchedules());
        }
    }, [dispatch, schedules]);

    useEffect(() => {
        const found = schedules.find((s) => s.scheduleId === parseInt(scheduleId));
        if (found) {
            setCurrentSchedule(found);
            form.setFieldsValue({
                movieId: found.movieId,
                cinemaId: found.cinemaId,
                roomId: found.roomId,
                scheduleStart: dayjs(found.scheduleStart),
                scheduleEnd: dayjs(found.scheduleEnd),
                format: found.format || '2D',
                audioType: found.audioType || 'SUBTITLE',
            });
        }
    }, [schedules, scheduleId, form]);

    const onFinish = (values) => {
        // cinemaId không gửi — rạp suy ra từ phòng ở backend.
        const data = {
            movieId: values.movieId,
            roomId: values.roomId,
            scheduleStart: values.scheduleStart.format('YYYY-MM-DDTHH:mm:ss'),
            scheduleEnd: values.scheduleEnd.format('YYYY-MM-DDTHH:mm:ss'),
            format: values.format,
            audioType: values.audioType,
        };

        dispatch(updateSchedule({ data, scheduleId }))
            .then(() => {
                message.success('Cập nhật lịch chiếu thành công!');
                navigate('/admin/schedule');
            })
            .catch(() => {
                message.error('Cập nhật thất bại!');
            });
    };

    if (loading || !currentSchedule) {
        return (
            <div className="flex justify-center mt-10">
                <Spin size="large" />
            </div>
        );
    }

    return (
        <div className="max-w-2xl mx-auto mt-8 p-4 bg-white shadow rounded">
            <h2 className="text-2xl font-bold mb-6 text-center">Chỉnh sửa lịch chiếu</h2>
            <Form
                form={form}
                layout="vertical"
                onFinish={onFinish}
            >
                <Form.Item label="Mã phim" name="movieId" rules={[{ required: true, message: 'Vui lòng nhập mã phim' }]}>
                    <Input />
                </Form.Item>

                <Form.Item label="Rạp chiếu (suy ra từ phòng)" name="cinemaId">
                    <Input disabled />
                </Form.Item>

                <Form.Item label="Phòng chiếu (roomId)" name="roomId" rules={[{ required: true, message: 'Vui lòng nhập mã phòng' }]}>
                    <Input />
                </Form.Item>

                <Form.Item label="Bắt đầu (ngày + giờ)" name="scheduleStart" rules={[{ required: true, message: 'Vui lòng chọn thời điểm bắt đầu' }]}>
                    <DatePicker showTime format="YYYY-MM-DD HH:mm" className="w-full" />
                </Form.Item>

                <Form.Item label="Kết thúc (ngày + giờ)" name="scheduleEnd" rules={[{ required: true, message: 'Vui lòng chọn thời điểm kết thúc' }]}>
                    <DatePicker showTime format="YYYY-MM-DD HH:mm" className="w-full" />
                </Form.Item>

                <Form.Item label="Định dạng" name="format">
                    <Select>
                        <Option value="2D">2D</Option>
                        <Option value="3D">3D</Option>
                        <Option value="IMAX">IMAX</Option>
                    </Select>
                </Form.Item>

                <Form.Item label="Âm thanh" name="audioType">
                    <Select>
                        <Option value="SUBTITLE">Phụ đề</Option>
                        <Option value="DUB">Lồng tiếng</Option>
                    </Select>
                </Form.Item>

                <Form.Item>
                    <div className="flex justify-between">
                        <Button type="primary" htmlType="submit">
                            Cập nhật
                        </Button>
                        <Button onClick={() => navigate('/admin/schedule')}>
                            Quay lại
                        </Button>
                    </div>
                </Form.Item>
            </Form>
        </div>
    );
}
