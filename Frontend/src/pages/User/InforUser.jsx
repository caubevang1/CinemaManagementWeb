import React, { useEffect, useState, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { callApiThongTinNguoiDung, capNhatNguoiDung } from '../../redux/reducers/UserReducer';
import NotFound from '../NotFound';
import { Tabs } from 'antd';
import moment from 'moment';
import { CloseOutlined, EditOutlined, SaveOutlined, UploadOutlined } from '@ant-design/icons';
import ThongTinBooking from './BookingInfo';
import UserAvatar from '../../components/UserAvatar';
import { UploadImage } from '../../services/UploadService';
import { SwalConfig } from '../../utils/config';

const MAX_AVATAR_SIZE = 5 * 1024 * 1024;

const ThongTinNguoiDung = ({ thongTinNguoiDung }) => {
    const [isEditing, setIsEditing] = useState({
        username: false,
        email: false,
        phoneNumber: false,
        gender: false,
        dateOfBirth: false,
        avatar: false,
        name: false,
    });

    const [editData, setEditData] = useState({
        firstName: '',
        lastName: '',
        username: '',
        email: '',
        phoneNumber: '',
        gender: 2,
        dateOfBirth: '',
    });

    const [avatarUrl, setAvatarUrl] = useState('');
    const [avatarFile, setAvatarFile] = useState(null);
    const [avatarPreview, setAvatarPreview] = useState('');
    const [avatarUploading, setAvatarUploading] = useState(false);
    const dispatch = useDispatch();

    const refs = {
        username: useRef(null),
        email: useRef(null),
        phoneNumber: useRef(null),
        gender: useRef(null),
        dateOfBirth: useRef(null),
        avatar: useRef(null),
        save: useRef(null),
        firstName: useRef(null),
        lastName: useRef(null),
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setEditData(prev => ({
            ...prev,
            [name]: name === "gender" ? parseInt(value, 10) : value,
        }));
    };

    const handleAvatarFileChange = (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        if (!file.type.startsWith('image/')) {
            SwalConfig('Vui lòng chọn file ảnh', 'error', true, 2500);
            e.target.value = '';
            return;
        }

        if (file.size > MAX_AVATAR_SIZE) {
            SwalConfig('Ảnh đại diện không được vượt quá 5MB', 'error', true, 2500);
            e.target.value = '';
            return;
        }

        setAvatarFile(file);
        setAvatarPreview(URL.createObjectURL(file));
    };

    const handleCancelAvatarEdit = () => {
        setAvatarFile(null);
        setAvatarPreview('');
        setIsEditing(prev => ({ ...prev, avatar: false }));
    };

    const handleSave = async () => {
        let nextAvatarUrl = avatarUrl;

        try {
            if (isEditing.avatar && avatarFile) {
                setAvatarUploading(true);
                const uploadResult = await UploadImage(avatarFile, 'cinemaweb/avatar');
                nextAvatarUrl = uploadResult.data.body.url;
            }

            const updatedData = {
                ...editData,
                avatar: nextAvatarUrl || thongTinNguoiDung?.avatar,
                id: thongTinNguoiDung?.id,
            };
            await dispatch(capNhatNguoiDung(updatedData));
            setAvatarUrl(nextAvatarUrl || '');
            setAvatarFile(null);
            setAvatarPreview('');
            setIsEditing({
                firstName: false,
                lastName: false,
                username: false,
                email: false,
                phoneNumber: false,
                gender: false,
                dateOfBirth: false,
                avatar: false,
                name: false,
            });
        } catch (error) {
            SwalConfig(error?.response?.data?.message || 'Không thể tải ảnh đại diện', 'error', true, 3000);
        } finally {
            setAvatarUploading(false);
        }
    };

    const handleEdit = (field) => {
        setIsEditing(prev => ({
            ...prev,
            [field]: true,
        }));
    };

    const GENDER = editData.gender === 0 ? 'Nữ' : editData.gender === 1 ? 'Nam' : 'Chưa xác định';

    useEffect(() => {
        dispatch(callApiThongTinNguoiDung);
    }, [dispatch]);

    useEffect(() => {
        if (thongTinNguoiDung) {
            setEditData({
                firstName: thongTinNguoiDung.firstName || '',
                lastName: thongTinNguoiDung.lastName || '',
                username: thongTinNguoiDung.username || '',
                email: thongTinNguoiDung.email || '',
                phoneNumber: thongTinNguoiDung.phoneNumber || '',
                gender: thongTinNguoiDung.gender ?? '',
                dateOfBirth: thongTinNguoiDung.dateOfBirth || '',
            });
            setAvatarUrl(thongTinNguoiDung.avatar || '');
            setAvatarFile(null);
            setAvatarPreview('');
        }
    }, [thongTinNguoiDung]);

    useEffect(() => {
        return () => {
            if (avatarPreview) {
                URL.revokeObjectURL(avatarPreview);
            }
        };
    }, [avatarPreview]);

    useEffect(() => {
        const handleClickOutside = (e) => {
            for (const field in isEditing) {
                if (isEditing[field]) {
                    if (field === "name") {
                        const clickedInsideFirst = refs.firstName?.current?.contains(e.target);
                        const clickedInsideLast = refs.lastName?.current?.contains(e.target);
                        const clickedSave = refs.save?.current?.contains(e.target);
                        if (!clickedInsideFirst && !clickedInsideLast && !clickedSave) {
                            setIsEditing(prev => ({ ...prev, name: false }));
                        }
                    } else {
                        if (
                            refs[field]?.current &&
                            !refs[field].current.contains(e.target) &&
                            !(refs.save?.current?.contains(e.target))
                        ) {
                            if (field === "avatar") {
                                setAvatarFile(null);
                                setAvatarPreview('');
                            }
                            setIsEditing(prev => ({ ...prev, [field]: false }));
                        }
                    }
                }
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [isEditing, refs]);

    const displayAvatar = isEditing.avatar && avatarPreview ? avatarPreview : avatarUrl;

    return (
        <div className="profile-page theme-purple min-h-screen py-[6rem]">
            <div className="bg">
                <div></div><span></span><span></span><span></span><span></span><span></span><span></span><span></span>
            </div>
            <div className="content relative max-w-5xl mx-auto px-6 py-6 bg-white rounded-2xl shadow-lg">
                <div className="absolute top-4 right-4 flex items-center gap-3 text-white text-sm">
                    <span style={{ padding: '6px 12px', fontSize: '25px', marginRight: '10px', marginTop: '15px' }}>
                        <span style={{ color: '#000' }}>Hello, </span>
                        <span style={{ color: '#3258F4' }}>{editData.username}</span>
                        <span style={{ color: '#000' }}> !</span>
                    </span>
                    <div className="bg-orange-500 px-3 py-[9px] rounded-full shadow mt-4 mr-2 font-bold text-[14px]">
                        🌟 {thongTinNguoiDung?.point || 0} ĐIỂM
                    </div>
                </div>

                <div className="content__cover">
                    <div className="content__bull">
                        <span></span><span></span><span></span><span></span><span></span>
                    </div>
                    <div
                        className="content__avatar"
                        onClick={() => handleEdit('avatar')}
                        style={{ cursor: 'pointer' }}
                        ref={refs.avatar}
                    >
                        <UserAvatar
                            avatar={displayAvatar}
                            firstName={editData.firstName}
                            lastName={editData.lastName}
                            username={editData.username}
                            email={editData.email}
                            size="100%"
                            className="w-full h-full"
                        />
                        {isEditing.avatar && (
                            <div
                                className="absolute left-1/2 top-[calc(100%+12px)] z-20 w-[260px] -translate-x-1/2 rounded-lg border border-gray-200 bg-white p-3 shadow-xl"
                                onClick={(e) => e.stopPropagation()}
                            >
                                <label className="flex cursor-pointer items-center justify-center gap-2 rounded-md border border-dashed border-orange-400 px-3 py-2 text-sm font-semibold text-orange-500 hover:bg-orange-50">
                                    <UploadOutlined />
                                    Chọn ảnh
                                    <input
                                        type="file"
                                        accept="image/*"
                                        onChange={handleAvatarFileChange}
                                        className="hidden"
                                    />
                                </label>
                                {avatarFile && (
                                    <p className="mt-2 truncate text-center text-xs text-gray-500">
                                        {avatarFile.name}
                                    </p>
                                )}
                                <div className="mt-3 flex items-center justify-center gap-3">
                                    <button
                                        ref={refs.save}
                                        type="button"
                                        onClick={handleSave}
                                        disabled={!avatarFile || avatarUploading}
                                        className="text-green-500 disabled:cursor-not-allowed disabled:text-gray-300"
                                        title="Lưu ảnh đại diện"
                                    >
                                        <SaveOutlined />
                                    </button>
                                    <button
                                        type="button"
                                        onClick={handleCancelAvatarEdit}
                                        className="text-gray-500 hover:text-red-500"
                                        title="Hủy"
                                    >
                                        <CloseOutlined />
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                <div className="content__title">
                    <h1>
                        {isEditing.name ? (
                            <div className="flex gap-2 justify-center items-center">
                                <input
                                    type="text"
                                    name="firstName"
                                    value={editData.firstName}
                                    onChange={handleInputChange}
                                    className="border-b border-gray-400"
                                    ref={refs.firstName}
                                />
                                <input
                                    type="text"
                                    name="lastName"
                                    value={editData.lastName}
                                    onChange={handleInputChange}
                                    className="border-b border-gray-400"
                                    ref={refs.lastName}
                                />
                                <button
                                    onClick={handleSave}
                                    className="ml-2 text-green-500"
                                    ref={refs.save}
                                >
                                    <SaveOutlined />
                                </button>
                            </div>
                        ) : (
                            `${editData.firstName} ${editData.lastName}`
                        )}
                        {!isEditing.name && (
                            <button onClick={() => handleEdit('name')} className="ml-3 text-blue-500">
                                <EditOutlined />
                            </button>
                        )}
                    </h1>
                </div>

                {/* Tách layout thành 2 cột */}
                <div className="flex justify-between gap-8 content__list mt-4">
                    {/* Cột trái */}
                    <ul className="flex-1 space-y-12 ">
                        <li>
                            <div className="flex items-center gap-2 border-b border-gray-200 pb-10">
                                <strong className="text-[20px]">Email:</strong>
                                <span className="flex-1">
                                    {isEditing.email ? (
                                        <input
                                            type="email"
                                            name="email"
                                            value={editData.email}
                                            onChange={handleInputChange}
                                            ref={refs.email}
                                            className="border-b border-gray-400 w-full text-gray-600"
                                        />
                                    ) : (
                                        <span className="text-gray-600">{editData.email}</span>
                                    )}
                                </span>
                                {!isEditing.email && (
                                    <button onClick={() => handleEdit('email')} className="text-blue-500">
                                        <EditOutlined />
                                    </button>
                                )}
                                {isEditing.email && (
                                    <button ref={refs.save} onClick={handleSave} className="text-green-500">
                                        <SaveOutlined />
                                    </button>
                                )}
                            </div>
                        </li>

                        <li>
                            <div className="flex items-center gap-2">
                                <strong className="text-[20px]">Giới tính:</strong>
                                <span className="flex-1">
                                    {isEditing.gender ? (
                                        <select
                                            name="gender"
                                            value={editData.gender}
                                            onChange={handleInputChange}
                                            ref={refs.gender}
                                            className="border-b border-gray-400 w-full text-gray-600"
                                        >
                                            <option value={0}>Nữ</option>
                                            <option value={1}>Nam</option>
                                        </select>
                                    ) : (
                                        <span className="text-gray-600">{GENDER}</span>
                                    )}
                                </span>
                                {!isEditing.gender && (
                                    <button onClick={() => handleEdit('gender')} className="text-blue-500">
                                        <EditOutlined />
                                    </button>
                                )}
                                {isEditing.gender && (
                                    <button ref={refs.save} onClick={handleSave} className="text-green-500">
                                        <SaveOutlined />
                                    </button>
                                )}
                            </div>
                        </li>
                    </ul>

                    {/* Cột phải */}
                    <ul className="flex-1 space-y-12">
                        <li>
                            <div className="flex items-center gap-2 border-b border-gray-200 pb-10">
                                <strong className="text-[20px]">Điện thoại:</strong>
                                <span className="flex-1">
                                    {isEditing.phoneNumber ? (
                                        <input
                                            type="text"
                                            name="phoneNumber"
                                            value={editData.phoneNumber}
                                            onChange={handleInputChange}
                                            ref={refs.phoneNumber}
                                            className="border-b border-gray-400 w-full text-gray-600"
                                        />
                                    ) : (
                                        <span className="text-gray-600">{editData.phoneNumber}</span>
                                    )}
                                </span>
                                {!isEditing.phoneNumber && (
                                    <button onClick={() => handleEdit('phoneNumber')} className="text-blue-500">
                                        <EditOutlined />
                                    </button>
                                )}
                                {isEditing.phoneNumber && (
                                    <button ref={refs.save} onClick={handleSave} className="text-green-500">
                                        <SaveOutlined />
                                    </button>
                                )}
                            </div>
                        </li>

                        <li>
                            <div className="flex items-center gap-2">
                                <strong className="text-[20px]">Ngày sinh:</strong>
                                <span className="flex-1">
                                    {isEditing.dateOfBirth ? (
                                        <input
                                            type="date"
                                            name="dateOfBirth"
                                            value={editData.dateOfBirth ? moment(editData.dateOfBirth).format('YYYY-MM-DD') : ''}
                                            onChange={handleInputChange}
                                            ref={refs.dateOfBirth}
                                            className="border-b border-gray-400 w-full text-gray-600"
                                        />
                                    ) : (
                                        <span className="text-gray-600">
                                            {editData.dateOfBirth ? moment(editData.dateOfBirth).format('DD/MM/YYYY') : ''}
                                        </span>
                                    )}
                                </span>
                                {!isEditing.dateOfBirth && (
                                    <button onClick={() => handleEdit('dateOfBirth')} className="text-blue-500">
                                        <EditOutlined />
                                    </button>
                                )}
                                {isEditing.dateOfBirth && (
                                    <button ref={refs.save} onClick={handleSave} className="text-green-500">
                                        <SaveOutlined />
                                    </button>
                                )}
                            </div>
                        </li>
                    </ul>
                </div>

                <div className="content__actions mt-[5px]">
                    <span
                        style={{
                            fontSize: '14px',
                            display: 'inline-block',
                            padding: '8px 16px',
                            backgroundColor: '#28a745',
                            color: 'white',
                            borderRadius: '25px',
                            fontWeight: 'bold',
                            textAlign: 'center',
                            transition: 'background-color 0.3s ease, transform 0.2s ease',
                        }}
                        onMouseOver={(e) => e.target.style.backgroundColor = '#218838'}
                        onMouseOut={(e) => e.target.style.backgroundColor = '#28a745'}
                    >
                        {thongTinNguoiDung?.roles?.[0]?.name}
                    </span>
                </div>
            </div>
        </div>
    );
};

const InforUser = () => {
    const { thongTinNguoiDung, isLogin } = useSelector(state => state.UserReducer);
    const dispatch = useDispatch();

    useEffect(() => {
        dispatch(callApiThongTinNguoiDung);
    }, []);

    const items = [
        {
            label: <span className="text-[15px] sm:text-[20px] font-bold ml-2">Thông tin tài khoản</span>,
            key: 1,
            children: <ThongTinNguoiDung thongTinNguoiDung={thongTinNguoiDung} />,
        },
        {
            label: <span className="text-[15px] sm:text-[20px] font-bold ml-2">Thông tin booking</span>,
            key: 2,
            children: <ThongTinBooking />, // Thêm tab cho thông tin booking
        },
    ];

    return isLogin ? (
        <Tabs className="pt-[6rem] min-h-[100vh] booking" items={items} />
    ) : (
        <NotFound />
    );
};

export default InforUser;
