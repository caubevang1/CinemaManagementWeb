import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Modal } from 'antd';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faKey } from '@fortawesome/free-solid-svg-icons';
import { CapNhatMaPinChuyenNhuong } from '../services/UserService';
import { callApiThongTinNguoiDung } from '../redux/reducers/UserReducer';
import { SwalConfig } from '../utils/config';

// Modal nhỏ tạo/đổi mã PIN chuyển nhượng, phong cách giống form đăng nhập.
const TransferPinModal = ({ open, onClose }) => {
    const dispatch = useDispatch();
    const hasPin = useSelector((s) => !!s.UserReducer.thongTinNguoiDung?.hasTransferPin);
    const [currentPin, setCurrentPin] = useState('');
    const [newPin, setNewPin] = useState('');
    const [saving, setSaving] = useState(false);

    const handleClose = () => {
        setCurrentPin('');
        setNewPin('');
        onClose();
    };

    const handleSave = async () => {
        if (!/^\d{6}$/.test(newPin)) {
            SwalConfig('Mã PIN mới phải gồm đúng 6 chữ số', 'error', true);
            return;
        }
        setSaving(true);
        try {
            await CapNhatMaPinChuyenNhuong({ currentPin: hasPin ? currentPin : undefined, newPin });
            SwalConfig('Đã cập nhật mã PIN chuyển nhượng', 'success', false);
            dispatch(callApiThongTinNguoiDung);
            handleClose();
        } catch (e) {
            SwalConfig(e?.response?.data?.message || 'Không thể cập nhật mã PIN', 'error', true);
        } finally {
            setSaving(false);
        }
    };

    const pinInput = (value, setter, placeholder) => (
        <input
            type="password"
            inputMode="numeric"
            maxLength={6}
            value={value}
            onChange={(e) => setter(e.target.value.replace(/\D/g, ''))}
            onKeyDown={(e) => e.key === 'Enter' && handleSave()}
            placeholder={placeholder}
            className="w-full border border-gray-300 rounded-lg px-4 py-2.5 text-center tracking-[0.5em] focus:outline-none focus:border-orange-500"
        />
    );

    return (
        <Modal open={open} onCancel={handleClose} footer={null} width={360} centered>
            <div className="text-center mb-5">
                <FontAwesomeIcon className="w-9 h-9 text-orange-500" icon={faKey} />
                <h2 className="text-xl font-bold mt-2">{hasPin ? 'Đổi mã PIN' : 'Tạo mã PIN'}</h2>
                <p className="text-sm text-gray-500 mt-1">
                    {hasPin
                        ? 'Nhập mã PIN hiện tại và mã PIN mới (6 số).'
                        : 'Đặt mã PIN 6 số để xác nhận khi chuyển nhượng vé.'}
                </p>
            </div>
            <div className="flex flex-col gap-3">
                {hasPin && pinInput(currentPin, setCurrentPin, 'Mã PIN hiện tại')}
                {pinInput(newPin, setNewPin, hasPin ? 'Mã PIN mới' : 'Mã PIN 6 số')}
                <button
                    onClick={handleSave}
                    disabled={saving}
                    className="w-full py-3 bg-orange-500 hover:bg-orange-600 disabled:opacity-60 text-white font-bold uppercase rounded-lg transition"
                >
                    {hasPin ? 'Đổi mã PIN' : 'Tạo mã PIN'}
                </button>
            </div>
        </Modal>
    );
};

export default TransferPinModal;
