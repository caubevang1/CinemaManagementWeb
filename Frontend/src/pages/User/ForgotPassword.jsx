import React, { useEffect, useState } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCircleUser } from '@fortawesome/free-solid-svg-icons';
import { sendOtpEmail } from '../../services/UserService';
import { SwalConfig } from '../../utils/config';
import useRoute from '../../hooks/useRoute';

export default function ForgotPassword() {
    const [email, setEmail] = useState('');
    const [error, setError] = useState('');
    const [cooldown, setCooldown] = useState(0);
    const [sent, setSent] = useState(false);
    const { navigate } = useRoute();

    // Đếm ngược thời gian được phép gửi lại OTP
    useEffect(() => {
        if (cooldown <= 0) return;
        const id = setInterval(() => setCooldown((c) => c - 1), 1000);
        return () => clearInterval(id);
    }, [cooldown]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (cooldown > 0) return;

        if (email.trim() === '') {
            setError('Email không được để trống');
            return;
        }

        try {
            const res = await sendOtpEmail({ email });
            const cd = res?.data?.body ?? 90;
            setSent(true);
            setCooldown(cd);
            SwalConfig('Đã gửi mã OTP đến email của bạn. Vui lòng kiểm tra email để đặt lại mật khẩu.', 'success', false);
        } catch (err) {
            const message =
                err?.response?.data?.message ||
                err?.message ||
                'Lỗi gửi OTP';
            SwalConfig(message, 'error', true, 3000);
        }
    };

    return (
        <div className='login'>
            <div className='login__overlay'></div>
            <form onSubmit={handleSubmit} className="form rounded-lg bg-white p-2 sm:p-4 md:p-8">
                <div className='text-center mb-6'>
                    <FontAwesomeIcon className='w-10 h-10 text-orange-500' icon={faCircleUser} />
                    <h2 className='text-xl font-bold'>Quên Mật Khẩu</h2>
                </div>

                <div className="form-control">
                    <input
                        placeholder="none"
                        title='Email'
                        name="email"
                        type="email"
                        className="form-input"
                        autoComplete='off'
                        value={email}
                        onChange={(e) => {
                            setEmail(e.target.value);
                            setError('');
                        }}
                        required
                    />
                    <label className="form-label bg-white">Email</label>
                </div>
                {error && <p className='form-err font-medium mb-4 mt-1'>{error}</p>}

                {sent && (
                    <p className='text-sm text-gray-600 mt-1 mb-2'>
                        Không nhận được mail? Kiểm tra hộp thư rác, hoặc gửi lại khi hết đếm ngược.
                    </p>
                )}

                <div className="my-2 mt-4">
                    <button
                        type="submit"
                        disabled={cooldown > 0}
                        className="w-full py-4 bg-red-600 text-white font-bold text-sm leading-tight uppercase rounded shadow-md hover:bg-red-700 hover:shadow-lg
                         focus:bg-red-700 focus:shadow-lg focus:outline-none focus:ring-0 active:bg-red-800 active:shadow-lg transition duration-150 ease-in-out
                         disabled:bg-gray-400 disabled:cursor-not-allowed">
                        {cooldown > 0 ? `Gửi lại sau ${cooldown}s` : (sent ? 'Gửi lại OTP' : 'Gửi OTP')}
                    </button>
                </div>

                <div className='text-right mt-2'>
                    <span onClick={() => navigate('/login')} className='text-sm text-blue-600 cursor-pointer hover:underline'>
                        Quay lại đăng nhập
                    </span>
                </div>
            </form>
        </div>
    );
}
