import React, { useEffect, useState, useMemo, useCallback } from 'react';
import { Table, Input, Tooltip, Select } from 'antd';
import { useDispatch, useSelector } from 'react-redux';
import { NavLink } from 'react-router-dom';
import { EditOutlined, DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import Swal from 'sweetalert2';
import { debounce } from 'lodash';
import { callApiCombo, xoaComboApi } from '../../../redux/reducers/FoodComboReducer';
import { layThongTinCumRap } from '../../../services/CinemaService';

const { Search } = Input;

export default function FoodCombo() {
    const dispatch = useDispatch();
    const { arrCombo } = useSelector(state => state.FoodComboReducer);
    const [keyword, setKeyword] = useState('');
    const [cinemaFilter, setCinemaFilter] = useState(null);
    const [cinemas, setCinemas] = useState([]);

    useEffect(() => {
        dispatch(callApiCombo);
        (async () => {
            try {
                const res = await layThongTinCumRap();
                setCinemas(res.data.body || []);
            } catch {
                setCinemas([]);
            }
        })();
    }, [dispatch]);

    const searchKeyword = useCallback(
        debounce((value) => setKeyword(value), 300),
        []
    );

    const data = useMemo(() => {
        const key = keyword.toLowerCase();
        return arrCombo.filter(item => {
            const matchName = item.foodAndDrinkName?.toLowerCase().includes(key);
            const matchCinema = cinemaFilter == null || item.cinemaId === cinemaFilter;
            return matchName && matchCinema;
        });
    }, [arrCombo, keyword, cinemaFilter]);

    const columns = [
        {
            title: 'Hình ảnh',
            dataIndex: 'imageFoodAndDrink',
            render: (img, combo) => (
                img
                    ? <img src={img} alt={combo.foodAndDrinkName} style={{ width: 56, height: 56, objectFit: 'cover', borderRadius: 8 }} />
                    : <span className="text-gray-400">—</span>
            ),
            width: 90,
        },
        {
            title: 'Tên combo',
            dataIndex: 'foodAndDrinkName',
            sorter: (a, b) => a.foodAndDrinkName.localeCompare(b.foodAndDrinkName),
        },
        {
            title: 'Rạp',
            dataIndex: 'cinemaName',
            sorter: (a, b) => (a.cinemaName || '').localeCompare(b.cinemaName || ''),
        },
        {
            title: 'Giá',
            dataIndex: 'foodAndDrinkPrice',
            render: (price) => `${Number(price).toLocaleString()} VND`,
            sorter: (a, b) => a.foodAndDrinkPrice - b.foodAndDrinkPrice,
            width: 150,
        },
        {
            title: 'Hành động',
            dataIndex: 'actions',
            render: (text, combo) => (
                <>
                    <Tooltip title="Chỉnh sửa combo">
                        <NavLink to={`/admin/foodcombo/edit/${combo.foodAndDrinkId}`} className='text-blue-600 mr-3 text-xl'>
                            <EditOutlined />
                        </NavLink>
                    </Tooltip>
                    <Tooltip title="Xóa combo">
                        <button
                            onClick={() => {
                                Swal.fire({
                                    title: 'Bạn có chắc muốn xóa combo này không?',
                                    icon: 'warning',
                                    showCancelButton: true,
                                    confirmButtonText: 'Xóa',
                                    cancelButtonText: 'Hủy',
                                    confirmButtonColor: '#f87171',
                                }).then(result => {
                                    if (result.isConfirmed) {
                                        dispatch(xoaComboApi(combo.foodAndDrinkId));
                                    }
                                });
                            }}
                            className='text-red-600 text-xl hover:text-red-400'
                        >
                            <DeleteOutlined />
                        </button>
                    </Tooltip>
                </>
            ),
            width: 120,
        },
    ];

    return (
        <div className='adminFoodCombo'>
            <div className="flex items-center justify-between mb-4">
                <h2 className='text-2xl uppercase font-bold'>Quản lý Combo (Đồ ăn/Thức uống)</h2>
                <NavLink
                    to='/admin/foodcombo/add'
                    className='flex items-center gap-2 border-2 border-orange-300 px-4 py-2 rounded-md hover:border-orange-500 text-orange-500 font-semibold'
                >
                    <PlusOutlined /> Thêm combo
                </NavLink>
            </div>
            <div className="flex gap-3 mb-4">
                <Search
                    placeholder="Tìm theo tên combo"
                    enterButton='Tìm kiếm'
                    size="large"
                    allowClear
                    onChange={(e) => searchKeyword(e.target.value)}
                />
                <Select
                    size="large"
                    allowClear
                    placeholder="Lọc theo rạp"
                    style={{ minWidth: 220 }}
                    value={cinemaFilter}
                    onChange={(value) => setCinemaFilter(value ?? null)}
                    options={cinemas.map(c => ({ value: c.cinemaId, label: c.cinemaName }))}
                />
            </div>
            <Table columns={columns} dataSource={data} rowKey='foodAndDrinkId' />
        </div>
    );
}
