import React, { useEffect, useState } from 'react';
import { Form, Input, InputNumber, Select } from 'antd';
import { CloseOutlined, UploadOutlined } from '@ant-design/icons';
import { useFormik } from 'formik';
import { useDispatch } from 'react-redux';
import { themComboApi } from '../../../redux/reducers/FoodComboReducer';
import { layThongTinCumRap } from '../../../services/CinemaService';
import { UploadImage } from '../../../services/UploadService';
import { SwalConfig } from '../../../utils/config';

const MAX_IMAGE_SIZE = 5 * 1024 * 1024;

export default function AddFoodCombo() {
    const dispatch = useDispatch();
    const [cinemas, setCinemas] = useState([]);
    const [imageFile, setImageFile] = useState(null);
    const [imagePreview, setImagePreview] = useState('');

    useEffect(() => {
        (async () => {
            try {
                const res = await layThongTinCumRap();
                setCinemas(res.data.body || []);
            } catch {
                setCinemas([]);
            }
        })();
    }, []);

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
        initialValues: {
            foodAndDrinkName: '',
            cinemaId: null,
            foodAndDrinkPrice: 0,
        },
        onSubmit: async (values, { setSubmitting }) => {
            if (!values.foodAndDrinkName || !values.cinemaId || !values.foodAndDrinkPrice) {
                SwalConfig('Vui lòng điền đầy đủ thông tin', 'error', true);
                setSubmitting(false);
                return;
            }
            try {
                let imageFoodAndDrink = '';
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
                await themComboApi(payload);
            } catch (error) {
                SwalConfig(error?.response?.data?.message || 'Không thể tải ảnh combo', 'error', true, 3000);
            } finally {
                setSubmitting(false);
            }
        }
    });

    return (
        <div className='addFoodComboAdmin'>
            <h2 className='text-xl uppercase font-bold mb-4'>Thêm combo</h2>
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
                        {imagePreview
                            ? <img src={imagePreview} alt="preview" style={{ width: 80, height: 80, objectFit: 'cover', borderRadius: 8 }} />
                            : <div style={{ width: 80, height: 80, borderRadius: 8 }} className="bg-gray-100 flex items-center justify-center text-gray-300 text-xs">No image</div>}
                        <div className="flex items-center gap-2">
                            <label className="flex cursor-pointer items-center gap-2 rounded-md border border-dashed border-orange-400 px-4 py-2 text-sm font-semibold text-orange-500 hover:bg-orange-50">
                                <UploadOutlined />
                                Chọn ảnh
                                <input type="file" accept="image/*" onChange={handleImageFileChange} className="hidden" />
                            </label>
                            {imageFile && (
                                <button type="button" onClick={clearImageFile} className="text-gray-500 hover:text-red-500" title="Hủy ảnh">
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
                        Thêm combo
                    </button>
                </Form.Item>
            </Form>
        </div>
    );
}
