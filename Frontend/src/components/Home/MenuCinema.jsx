import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Tabs, Input } from 'antd';
import { debounce } from 'lodash';
import moment from 'moment';
import { useLocation } from 'react-router-dom';
import useRoute from '../../hooks/useRoute';
import { layThongTinCumRap, LayThongTinLichChieu, TimKiemCumRap } from '../../services/CinemaService';
import { LayDanhSachPhim } from '../../services/FilmService';

const { Search } = Input;

export default function MenuCinema() {
    const location = useLocation();
    const { navigate } = useRoute();

    const [cumRapList, setCumRapList] = useState([]); // toàn bộ rạp (master)
    const [cumRapHienThi, setCumRapHienThi] = useState([]); // rạp đang hiển thị (sau lọc)
    const [phimList, setPhimList] = useState([]);
    const [lichChieuList, setLichChieuList] = useState([]);
    const cumRapListRef = useRef([]); // giữ master để khôi phục khi xóa từ khóa (tránh stale closure)

    useEffect(() => {
        layThongTinCumRap().then(res => {
            const list = res.data.body || [];
            setCumRapList(list);
            setCumRapHienThi(list);
            cumRapListRef.current = list;
        }).catch(console.error);
        LayDanhSachPhim().then(res => setPhimList(res.data.body)).catch(console.error);
        LayThongTinLichChieu().then(res => setLichChieuList(res.data.body)).catch(console.error);
    }, []);

    // Tìm rạp server-side qua RediSearch; debounce để hạn chế request khi gõ.
    const runSearch = useCallback(
        debounce(async (value) => {
            const term = value.trim();
            if (term === '') {
                setCumRapHienThi(cumRapListRef.current);
                return;
            }
            try {
                const res = await TimKiemCumRap(term);
                setCumRapHienThi(res.data.body || []);
            } catch (error) {
                console.error(error);
            }
        }, 300),
        []
    );

    useEffect(() => {
        if (location.hash) {
            const elem = document.getElementById(location.hash.slice(1));
            if (elem) elem.scrollIntoView({ behavior: 'smooth' });
        } else {
            window.scrollTo({ top: 0, left: 0, behavior: 'smooth' });
        }
    }, [location]);

    const renderDanhSachPhim = (itemCumRap) => {
        const lichChieuCumRap = lichChieuList.filter(
            (schedule) => schedule.cinemaName === itemCumRap.cinemaName
        );

        const phimTabs = phimList.filter(itemPhim => {
            const lichChieuTheoPhim = lichChieuCumRap.filter(
                (lc) => lc.movieName === itemPhim.movieName
            );

            const releaseDate = moment(itemPhim.releaseDate);
            const currentDateTime = moment();

            const isPhimSapChieu = releaseDate.isAfter(currentDateTime, 'day');
            const isSameDayAndAfterTime = releaseDate.isSame(currentDateTime, 'day') && moment(itemPhim.releaseTime, 'HH:mm:ss').isAfter(currentDateTime, 'minute');
            return (lichChieuTheoPhim.length > 0 && !isPhimSapChieu && !isSameDayAndAfterTime);
        }).map((itemPhim, i) => {
            const lichChieuTheoPhim = lichChieuCumRap.filter(
                (lc) => lc.movieName === itemPhim.movieName
            );

            return {
                label: (
                    <div className="flex border-b pb-4">
                        <div
                            className="mr-4 cursor-pointer"
                            onClick={(e) => { e.stopPropagation(); navigate(`detail/${itemPhim.movieId}`); }}
                        >
                            <img alt="poster"
                                className='h-[130px] w-[100px] object-cover'
                                src={itemPhim.moviePoster}
                                onError={(e) => {
                                    e.target.onerror = null;
                                    e.target.src = 'https://tophinhanhdep.com/wp-content/uploads/2021/10/Movie-Wallpapers.jpg';
                                }}
                            />
                        </div>
                        <div>
                            <h2
                                className="font-bold text-left mb-2 text-sm uppercase cursor-pointer"
                                onClick={(e) => { e.stopPropagation(); navigate(`detail/${itemPhim.movieId}`); }}
                            >
                                <span className="bg-red-600 p-1 rounded-md text-white text-sm">
                                    {itemPhim.hot === true ? 'C18' : 'C16'}
                                </span>{' '}{itemPhim.movieName}
                            </h2>
                            <div className="grid grid-cols-2 gap-1">
                                {lichChieuTheoPhim.slice(0, 4).map((lichChieu, index) => (
                                    <button
                                        key={index}
                                        onClick={() => navigate(`booking/${lichChieu.scheduleId}`)}
                                        className="bg-gray-100 hover:bg-gray-300 border-2 text-white font-bold py-2 px-4 rounded"
                                    >
                                        <span className="text-green-500 text-sm">
                                            {moment(lichChieu.scheduleStart).format('DD-MM-YYYY ~ ')}
                                        </span>
                                        <span className="text-orange-500">
                                            {moment(lichChieu.scheduleStart).format('hh:mm A')}
                                        </span>
                                    </button>
                                ))}
                            </div>
                        </div>
                    </div>
                ),
                key: i.toString(),
                children: null,
            };
        });

        return (
            <Tabs
                tabPosition="left"
                defaultActiveKey="0"
                items={phimTabs}
            />
        );
    };

    return (
        <>
            {cumRapList.length ? (
                <div id="menuCinema" className="MenuCinemaTabs hidden lg:block my-8">
                    {/* Card rạp: ô tìm rạp (server-side RediSearch) gộp chung với danh sách rạp/phim */}
                    <div className="menuCinemaCard">
                        <div className="p-4 border-b">
                            <Search
                                placeholder="Tìm rạp theo tên hoặc địa chỉ"
                                size="large"
                                allowClear
                                onChange={(e) => runSearch(e.target.value)}
                            />
                        </div>
                        {cumRapHienThi.length ? (
                            <Tabs
                                className="pt-3"
                                tabPosition="left"
                                defaultActiveKey="0"
                                items={cumRapHienThi.map((itemCumRap, index) => ({
                                    label: (
                                        <div className="text-left border-b pb-4 whitespace-normal break-words">
                                            <h2 className="text-green-500 font-bold text-base break-words">
                                                {itemCumRap.cinemaName}
                                            </h2>
                                            <h3 className="text-gray-500 font-semibold text-sm break-words">
                                                {itemCumRap.cinemaAddress}
                                            </h3>
                                        </div>
                                    ),
                                    key: index.toString(),
                                    children: renderDanhSachPhim(itemCumRap),
                                }))}
                            />
                        ) : (
                            <h2 className="text-gray-500 text-center my-6 text-xl">Không tìm thấy rạp phù hợp</h2>
                        )}
                    </div>
                </div>
            ) : (
                <h2 className="text-white text-center my-6 text-2xl">Hiện tại không có lịch chiếu</h2>
            )}
        </>
    );
}
