import axios from "axios";
import { DOMAIN_BE, LOCALSTORAGE_USER } from "./constant";
import { getLocalStorage, setLocalStorage, removeLocalStorage } from "./config";

export const http = axios.create({
    baseURL: DOMAIN_BE,
    timeout: 10000,
    withCredentials: true
});

// Các endpoint auth công khai: KHÔNG gắn access token. Nếu gắn token đã hết hạn, Spring
// (BearerTokenAuthenticationFilter) sẽ trả 401 trước khi vào controller permitAll → refresh hỏng.
const NO_AUTH_PATHS = ['/auth/refresh-Token', '/auth/login'];

http.interceptors.request.use(config => {
    if (NO_AUTH_PATHS.some(p => config.url?.includes(p))) {
        delete config.headers.Authorization;
        return config;
    }
    const user = getLocalStorage(LOCALSTORAGE_USER);
    if (user?.accessToken) {
        config.headers.Authorization = `Bearer ${user.accessToken}`;
    }
    return config;
});

let isRefreshing = false;
let refreshSubscribers = [];

const onRefreshed = (token) => {
    refreshSubscribers.forEach(cb => cb(token));
    refreshSubscribers = [];
};

const onRefreshFailed = () => {
    refreshSubscribers.forEach(cb => cb(null));
    refreshSubscribers = [];
};

http.interceptors.response.use(
    response => {
        const data = response.data;
        if (data && typeof data === 'object' && 'code' in data && data.code !== 1000) {
            return Promise.reject({
                response: { ...response, data },
                config: response.config,
                message: data.message,
                isApiError: true,
            });
        }
        return response;
    },
    async error => {
        const original = error.config;

        // Chặn loop: nếu chính refresh endpoint bị lỗi → logout luôn
        if (original.url?.includes('/auth/refresh-Token')) {
            removeLocalStorage(LOCALSTORAGE_USER);
            onRefreshFailed();
            window.location.href = '/login';
            return Promise.reject(error);
        }

        // Login thất bại (sai mật khẩu) trả 401 → KHÔNG được kích hoạt refresh.
        // Nếu không, cookie refresh của phiên cũ sẽ "đăng nhập lén" thành công và
        // cắm access token vào localStorage dù mật khẩu sai. Để Login.jsx hiện toast lỗi.
        if (original.url?.includes('/auth/login')) {
            return Promise.reject(error);
        }

        if (error.response?.status === 401 && !original._retry) {
            original._retry = true;

            if (isRefreshing) {
                // Có refresh đang chạy → đợi kết quả, không gọi refresh thêm
                return new Promise((resolve, reject) => {
                    refreshSubscribers.push((token) => {
                        if (!token) return reject(error);
                        original.headers.Authorization = `Bearer ${token}`;
                        resolve(http(original));
                    });
                });
            }

            isRefreshing = true;
            try {
                const res = await http.post('/auth/refresh-Token');
                const newToken = res.data.body.token;
                const user = getLocalStorage(LOCALSTORAGE_USER);
                setLocalStorage(LOCALSTORAGE_USER, { ...user, accessToken: newToken });
                original.headers.Authorization = `Bearer ${newToken}`;
                onRefreshed(newToken);
                return http(original);
            } catch {
                removeLocalStorage(LOCALSTORAGE_USER);
                onRefreshFailed();
                window.location.href = '/login';
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);