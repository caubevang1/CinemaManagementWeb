import { Form, Input, InputNumber, Tabs, Spin, DatePicker, Select } from 'antd';
import { SearchOutlined, FormOutlined, StarFilled, PictureOutlined } from '@ant-design/icons';
import React, { useState, useMemo, useEffect } from 'react';
import dayjs from 'dayjs';
import { useFormik } from 'formik';
import { themPhimApi } from '../../../redux/reducers/FilmReducer';
import { useDispatch } from 'react-redux';
import { SwalConfig } from '../../../utils/config';
import { searchTmdb, importFromTmdb, getTmdbDetail } from '../../../services/TmdbService';
import debounce from 'lodash.debounce';

const ManualForm = ({ formik, imgSrc, handleChangeImageURL }) => (
    <Form
        onSubmitCapture={formik.handleSubmit}
        labelCol={{ span: 8 }}
        wrapperCol={{ span: 10 }}
    >
        <Form.Item label="Tên phim">
            <Input name='movieName' value={formik.values.movieName} onChange={formik.handleChange} />
        </Form.Item>
        <Form.Item label="Mô tả">
            <Input.TextArea name='movieDescription' value={formik.values.movieDescription} onChange={formik.handleChange} rows={4} />
        </Form.Item>
        <Form.Item label="Thời lượng (phút)">
            <InputNumber value={formik.values.movieLength} onChange={value => formik.setFieldValue('movieLength', value)} min={1} />
        </Form.Item>
        <Form.Item label="Số sao (1-5)">
            <InputNumber value={formik.values.movieReview} onChange={value => formik.setFieldValue('movieReview', value)} min={1} max={5} step={0.1} />
        </Form.Item>
        <Form.Item label="Thể loại phim">
            <Input name='movieGenre' value={formik.values.movieGenre} onChange={formik.handleChange} />
        </Form.Item>
        <Form.Item label="Ngày phát hành">
            <DatePicker
                value={formik.values.releaseDate ? dayjs(formik.values.releaseDate) : null}
                onChange={(d) => formik.setFieldValue('releaseDate', d ? d.format('YYYY-MM-DD') : null)}
                format="YYYY-MM-DD"
            />
        </Form.Item>
        <Form.Item label="Trạng thái">
            <Select
                value={formik.values.status}
                onChange={(value) => formik.setFieldValue('status', value)}
                options={[
                    { value: 'NOW_SHOWING', label: 'Đang chiếu' },
                    { value: 'COMING_SOON', label: 'Sắp chiếu' },
                    { value: 'ENDED', label: 'Ngừng chiếu' },
                ]}
            />
        </Form.Item>
        <Form.Item label="Trailer (URL)">
            <Input name='trailerUrl' value={formik.values.trailerUrl} onChange={formik.handleChange} placeholder="https://youtube.com/..." />
        </Form.Item>
        <Form.Item label="Hình ảnh (URL)">
            <Input name="moviePoster" value={formik.values.moviePoster} onChange={handleChangeImageURL} placeholder="Nhập URL hình ảnh" />
            {imgSrc && <img src={imgSrc} alt="poster preview" className="mt-2" style={{ width: 120, height: 180, objectFit: 'cover' }} />}
        </Form.Item>
        <Form.Item label="Tác vụ">
            <button type='submit' className='border-2 border-orange-300 px-4 py-2 rounded-md hover:border-orange-500'>
                Thêm phim
            </button>
        </Form.Item>
    </Form>
);

export default function AddNewFilm() {
    const [imgSrc, setImgSrc] = useState(null);
    const [activeTab, setActiveTab] = useState('tmdb');
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [searching, setSearching] = useState(false);
    const [importing, setImporting] = useState(false);
    const dispatch = useDispatch();

    const formik = useFormik({
        initialValues: {
            movieName: '',
            movieDescription: '',
            movieLength: 0,
            movieReview: 0,
            moviePoster: '',
            movieGenre: '',
            tmdbId: null,
            trailerUrl: '',
            releaseDate: null,
            status: 'NOW_SHOWING',
        },
        onSubmit: (value) => {
            const { movieName, movieDescription, movieLength, movieReview, moviePoster, movieGenre, releaseDate } = value;
            if (movieName && movieDescription && movieLength && movieReview && moviePoster && movieGenre && releaseDate) {
                dispatch(themPhimApi(value));
                setImgSrc('');
            } else {
                SwalConfig('Vui lòng điền đầy đủ thông tin', 'error', true);
            }
        }
    });

    const handleChangeImageURL = (e) => {
        const url = e.target.value;
        formik.setFieldValue('moviePoster', url);
        setImgSrc(url);
    };

    const doSearch = useMemo(
        () => debounce(async (q) => {
            if (!q.trim()) { setSearchResults([]); return; }
            setSearching(true);
            try {
                const res = await searchTmdb(q);
                setSearchResults(res.data.body || []);
            } catch {
                SwalConfig('Không thể kết nối TMDB', 'error', true, 2000);
            } finally {
                setSearching(false);
            }
        }, 500),
        []
    );

    // Hủy timer debounce còn treo khi unmount
    useEffect(() => () => doSearch.cancel(), [doSearch]);

    const handleSearchChange = (e) => {
        const q = e.target.value;
        setSearchQuery(q);
        doSearch(q);
    };

    const handleImport = async (tmdbId) => {
        setImporting(true);
        try {
            await importFromTmdb(tmdbId);
            SwalConfig('Import phim thành công!', 'success', true);
            setSearchResults([]);
            setSearchQuery('');
        } catch (err) {
            SwalConfig(err?.response?.data?.message || 'Import thất bại', 'error', true, 3000);
        } finally {
            setImporting(false);
        }
    };

    const handleSelectForForm = async (movie) => {
        setImporting(true);
        try {
            const res = await getTmdbDetail(movie.tmdbId);
            const detail = res.data.body;
            formik.setValues({
                ...formik.values,
                movieName: detail.title || '',
                movieDescription: detail.overview || '',
                moviePoster: detail.posterUrl || '',
                movieGenre: detail.genreNames || '',
                movieLength: detail.runtime || 0,
                movieReview: detail.voteAverage ? parseFloat((detail.voteAverage / 2).toFixed(1)) : 0,
                tmdbId: detail.tmdbId || null,
                trailerUrl: detail.trailerUrl || '',
                releaseDate: detail.releaseDate || null,
            });
            setImgSrc(detail.posterUrl || '');
            setSearchResults([]);
            setSearchQuery('');
            setActiveTab('manual');
        } catch (err) {
            console.error('getTmdbDetail error:', err);
            const msg = err?.response?.data?.message || err?.message || 'Không thể lấy chi tiết phim';
            SwalConfig(msg, 'error', true, 3000);
        } finally {
            setImporting(false);
        }
    };

    const TmdbTab = (
        <div>
            <p className="mb-3 text-gray-500 text-sm">
                Tìm phim từ TMDB → <strong>Import thẳng</strong> vào DB, hoặc <strong>Điền vào form</strong> để chỉnh trước khi lưu.
            </p>
            <Input
                placeholder="Nhập tên phim để tìm kiếm..."
                value={searchQuery}
                onChange={handleSearchChange}
                size="large"
                prefix={<SearchOutlined />}
            />

            {searching && <div className="mt-4 text-center"><Spin /></div>}

            {searchResults.length > 0 && (
                <div className="mt-4 space-y-3 max-h-[600px] overflow-y-auto">
                    {searchResults.map((movie) => (
                        <div key={movie.tmdbId} className="flex gap-3 border rounded-lg p-3 hover:bg-gray-50">
                            {movie.posterUrl ? (
                                <img src={movie.posterUrl} alt={movie.title} className="w-16 h-24 object-cover rounded flex-shrink-0" />
                            ) : (
                                <div className="w-16 h-24 bg-gray-200 rounded flex-shrink-0 flex items-center justify-center text-gray-400"><PictureOutlined style={{ fontSize: 20 }} /></div>
                            )}
                            <div className="flex-1 min-w-0">
                                <p className="font-semibold text-gray-800 truncate">{movie.title}</p>
                                <p className="text-xs text-gray-500 mt-1">
                                    {movie.releaseDate?.substring(0, 4)} · <StarFilled style={{ color: '#fadb14' }} /> {movie.voteAverage?.toFixed(1)}
                                </p>
                                <p className="text-xs text-gray-400 mt-1 line-clamp-2">{movie.overview}</p>
                                <div className="flex gap-2 mt-2">
                                    <button
                                        onClick={() => handleImport(movie.tmdbId)}
                                        disabled={importing}
                                        className="text-xs bg-orange-400 hover:bg-orange-500 text-white px-3 py-1 rounded"
                                    >
                                        {importing ? 'Đang import...' : 'Import thẳng'}
                                    </button>
                                    <button
                                        onClick={() => handleSelectForForm(movie)}
                                        className="text-xs border border-orange-300 hover:border-orange-500 text-orange-500 px-3 py-1 rounded"
                                    >
                                        Điền vào form
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );

    const tabItems = [
        {
            key: 'tmdb',
            label: <span><SearchOutlined /> Tìm từ TMDB</span>,
            children: TmdbTab,
        },
        {
            key: 'manual',
            label: <span><FormOutlined /> Nhập tay</span>,
            children: <ManualForm formik={formik} imgSrc={imgSrc} handleChangeImageURL={handleChangeImageURL} />,
        },
    ];

    return (
        <div className='addFilmAdmin'>
            <h2 className='text-xl uppercase font-bold mb-4'>Thêm Phim Mới</h2>
            <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />
        </div>
    );
}
