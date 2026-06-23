import React, { useCallback, useEffect, useState } from 'react';
import { AutoComplete, Tabs } from 'antd';
import { debounce } from 'lodash';
import MultipleRowSlick from './MultipleRowSlick';
import BookingTicketNow from './BookingTicketNow';
import { useLocation } from 'react-router-dom';
import useRoute from '../../hooks/useRoute';
import { GoiYTenPhim, TimKiemPhim } from '../../services/FilmService';

export default function MovieList(props) {
    const { arrFilm } = props;
    const [keyword, setKeyword] = useState('');
    const [options, setOptions] = useState([]); // gợi ý autocomplete
    const [results, setResults] = useState(null); // null = chưa tìm, hiển thị toàn bộ arrFilm
    const { navigate } = useRoute();

    const location = useLocation();

    // Gọi server-side RediSearch + lấy gợi ý; debounce để hạn chế request khi gõ.
    const runSearch = useCallback(
        debounce(async (value) => {
            const term = value.trim();
            if (term === '') {
                setResults(null);
                setOptions([]);
                return;
            }
            try {
                const [resList, resSuggest] = await Promise.all([
                    TimKiemPhim(term),
                    GoiYTenPhim(term),
                ]);
                setResults(resList.data.body);
                setOptions((resSuggest.data.body || []).map((name) => ({ value: name })));
            } catch (error) {
                console.log(error);
            }
        }, 300),
        []
    );

    const onSearchChange = (value) => {
        setKeyword(value);
        runSearch(value);
    };

    const moviesToShow = results === null ? arrFilm : results;

    useEffect(() => {
        if (location.hash) {
            let elem = document.getElementById(location.hash.slice(1));
            if (elem) {
                elem.scrollIntoView({ behavior: 'smooth' });
            }
        } else {
            window.scrollTo({ top: 0, left: 0, behavior: 'smooth' });
        }
    }, [location]);

    return (
        <div id="movie-list" className="movie-list container mx-auto md:px-8 lg:px-10">

            {/* Laptop */}
            <BookingTicketNow arrFilm={arrFilm} />
            <Tabs
                className="hidden md:block"
                defaultActiveKey="1"
                items={[
                    {
                        label: 'Phim đang chiếu',
                        key: '1',
                        children: <MultipleRowSlick arrFilm={arrFilm.filter(f => f.status === 'NOW_SHOWING')} />,
                    },
                    {
                        label: 'Phim sắp chiếu',
                        key: '2',
                        children: <MultipleRowSlick arrFilm={arrFilm.filter(f => f.status === 'COMING_SOON')} />,
                    },
                ]}
            />

            {/* Mobile */}
            <div className="block mt-16 sm:mt-8 md:mt-0 md:hidden">
                <div className="relative mb-4">
                    <AutoComplete
                        className="w-full"
                        value={keyword}
                        options={options}
                        onChange={onSearchChange}
                        onSelect={onSearchChange}
                        placeholder="Nhập tên phim cần tìm"
                        size="large"
                        allowClear
                    />
                </div>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 sm:gap-4">
                    {moviesToShow
                        .map((itemFilm, index) => (
                            <div
                                key={index}
                                className="rounded-md shadow-xl bg-gray-50 text-gray-800"
                            >
                                <img
                                    src={itemFilm.moviePoster}
                                    alt={itemFilm.moviePoster}
                                    onError={(e) => {
                                        e.target.onerror = null;
                                        e.target.src = 'https://picsum.photos/75/75';
                                    }}
                                    className="object-cover object-center w-full rounded-t-md h-44 sm:h-52"
                                />
                                <div className="flex flex-col justify-between p-4">
                                    <h2 className="film-name-card-mobile font-semibold mb-2">
                                        {itemFilm.movieName.length > 22
                                            ? itemFilm.movieName.toUpperCase().slice(0, 22) + '...'
                                            : itemFilm.movieName.toUpperCase()}
                                    </h2>
                                    <button
                                        onClick={() => navigate(`detail/${itemFilm.movieId}`)}
                                        className="flex items-center justify-center w-full p-2 sm:p-3 font-semibold rounded-md bg-yellow-500 text-gray-50"
                                    >
                                        Đặt vé
                                    </button>
                                </div>
                            </div>
                        ))}
                </div>
            </div>
        </div>
    );
}
