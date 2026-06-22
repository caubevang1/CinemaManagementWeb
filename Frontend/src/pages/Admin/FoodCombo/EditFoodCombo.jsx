import React, { useEffect, useState } from 'react';
import { Form, Input, InputNumber, Select } from 'antd';
import { CloseOutlined, UploadOutlined } from '@ant-design/icons';
import { useFormik } from 'formik';
import { useDispatch, useSelector } from 'react-redux';
import useRoute from '../../../hooks/useRoute';
import { callApiComboDetail, capNhatComboApi } from '../../../redux/reducers/FoodComboReducer';
import { layThongTinCumRap } from '../../../services/CinemaService';
import { UploadImage } from '../../../services/UploadService';
import { SwalConfig } from '../../../utils/config';

const MAX_IMAGE_SIZE = 5 * 1024 * 1024;

export default function EditFoodCombo() {
    const dispatch = useDispatch();
    const { param } = useRoute();
    const { comboDetail } = useSelector(state => state.FoodComboReducer);
    const [cinemas, setCinemas] = useState([]);
    const [imageFile, setImageFile] = useState(null);
    const [imagePreview, setImagePreview] = useState('');

    useEffect(() => {
        dispatch(callApiComboDetail(param.id));
        (async () => {
            try {
                const res = await layThongTinCumRap();
                setCinemas(res.data.body || []);
            } catch {
                setCinemas([]);
            }
        })();
    }, [dispatch, param.id]);

    useEffect(() => {
        return () => { if (imagePreview) URL.revokeObjectURL(imagePreview); };
    }, [imagePreview]);

    const handleImageFileChange = (e) => {
        const file = e.target.files?.[0];
        if (!file) return;
        if (!file.type.startsWith('image/')) {
            SwalConfig('Vui lòng chọn file ảnh', 'error', true, 2500);
            e.target.value = '';
            return;
        }
        if (file.size > MAX_IMAGE_SIZE) {
            SwalConfig('Ảnh không được vượt quá 5MB', 'error', true, 2500);
            e.target.value = '';
            return;
        }
        setImageFile(file);
        setImagePreview(URL.createObjectURL(file));
    };

    const clearImageFile = () => {
        setImageFile(null);
        setImagePreview('');
    };

    const formik = useFormik({
        enableReinitialize: true,
        initialValues: {
            foodAndDrinkName: comboDetail?.foodAndDrinkName || '',
            cinemaId: comboDetail?.cinemaId || null,
            foodAndDrinkPrice: comboDetail?.foodAndDrinkPrice || 0,
            imageFoodAndDrink: comboDetail?.imageFoodAndDrink || '',
        },
        onSubmit: async (values, { setSubmitting }) => {
            if (!values.foodAndDrinkName || !values.cinemaId || !values.foodAndDrinkPrice) {
                SwalConfig('Vui lòng điền đầy đủ thông tin', 'error', true);
                setSubmitting(false);
                return;
            }
            try {
                let imageFoodAndDrink = values.imageFoodAndDrink;
                if (imageFile) {
                    const uploadResult = await UploadImage(imageFile, 'cinemaweb/foodanddrink');
                    imageFoodAndDrink = uploadResult.data.body.url;
                }
                const payload = {
                    foodAndDrinkName: values.foodAndDrinkName,
                    cinemaId: String(values.cinemaId),
                    foodAndDrinkPrice: values.foodAndDrinkPrice,
                    imageFoodAndDrink,
                };
                await capNhatComboApi(param.id, payload);
                clearImageFile();
            } catch (error) {
                SwalConfig(error?.response?.data?.message || 'Không thể tải ảnh combo', 'error', true, 3000);
            } finally {
                setSubmitting(false);
            }
        }
    });

    return (
        <div className='editFoodComboAdmin'>
            <h2 className='text-xl uppercase font-bold mb-4'>Chỉnh sửa combo</h2>
            <Form
                onSubmitCapture={formik.handleSubmit}
                labelCol={{ span: 6 }}
                wrapperCol={{ span: 12 }}
            >
                <Form.Item label="Tên combo">
                    <Input name='foodAndDrinkName' value={formik.values.foodAndDrinkName} onChange={formik.handleChange} />
                </Form.Item>
                <Form.Item label="Rạp">
                    <Select
                        placeholder="Chọn rạp"
                        value={formik.values.cinemaId}
                        onChange={(value) => formik.setFieldValue('cinemaId', value)}
                        options={cinemas.map(c => ({ value: c.cinemaId, label: c.cinemaName }))}
                    />
                </Form.Item>
                <Form.Item label="Giá (VND)">
                    <InputNumber
                        className='w-full'
                        min={0}
                        step={1000}
                        value={formik.values.foodAndDrinkPrice}
                        onChange={(value) => formik.setFieldValue('foodAndDrinkPrice', value)}
                        formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                        parser={(v) => v.replace(/,/g, '')}
                    />
                </Form.Item>
                <Form.Item label="Hình ảnh">
                    <div className="flex items-center gap-4">
                        {(imagePreview || formik.values.imageFoodAndDrink)
                            ? <img src={imagePreview || formik.values.imageFoodAndDrink} alt="preview" style={{ width: 80, height: 80, objectFit: 'cover', borderRadius: 8 }} />
                            : <div style={{ width: 80, height: 80, borderRadius: 8 }} className="bg-gray-100 flex items-center justify-center text-gray-300 text-xs">No image</div>}
                        <div className="flex items-center gap-2">
                            <label className="flex cursor-pointer items-center gap-2 rounded-md border border-dashed border-orange-400 px-4 py-2 text-sm font-semibold text-orange-500 hover:bg-orange-50">
                                <UploadOutlined />
                                Đổi ảnh
                                <input type="file" accept="image/*" onChange={handleImageFileChange} className="hidden" />
                            </label>
                            {imageFile && (
                                <button type="button" onClick={clearImageFile} className="text-gray-500 hover:text-red-500" title="Hủy ảnh mới">
                                    <CloseOutlined />
                                </button>
                            )}
                        </div>
                    </div>
                    {imageFile && <p className="mt-2 max-w-[280px] truncate text-xs text-gray-500">{imageFile.name}</p>}
                </Form.Item>
                <Form.Item label="Tác vụ">
                    <button
                        type='submit'
                        disabled={formik.isSubmitting}
                        className='border-2 border-orange-300 px-4 py-2 rounded-md hover:border-orange-500'
                    >
                        Cập nhật combo
                    </button>
                </Form.Item>
            </Form>
        </div>
    );
}
