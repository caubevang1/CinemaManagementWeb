import { createSlice } from '@reduxjs/toolkit'
import { SwalConfig } from '../../utils/config';
import { history } from '../../utils/history'
import {
    LayThongTinFoodAndDrink,
    LayThongTinFoodAndDrinkChiTiet,
    ThemFoodAndDrink,
    CapNhatFoodAndDrink,
    XoaFoodAndDrink,
} from '../../services/BookingManager';

const initialState = {
    arrCombo: [],
    comboDetail: {},
}

const FoodComboReducer = createSlice({
    name: 'FoodComboReducer',
    initialState,
    reducers: {
        getComboList: (state, { payload }) => {
            state.arrCombo = payload
        },
        getComboDetail: (state, { payload }) => {
            state.comboDetail = payload
        },
    }
});

export const { getComboList, getComboDetail } = FoodComboReducer.actions

export default FoodComboReducer.reducer

export const callApiCombo = async (dispatch) => {
    try {
        const res = await LayThongTinFoodAndDrink();
        dispatch(getComboList(res.data.body))
    } catch (error) {
        console.log(error)
    }
}

export const callApiComboDetail = (id) => async (dispatch) => {
    try {
        const res = await LayThongTinFoodAndDrinkChiTiet(id)
        dispatch(getComboDetail(res.data.body))
    } catch (error) {
        console.log(error)
    }
}

export const themComboApi = async (formData) => {
    try {
        await ThemFoodAndDrink(formData)
        SwalConfig('Thêm combo thành công', 'success', true)
        history.push('/admin/foodcombo')
    } catch (error) {
        SwalConfig(`${error?.response?.data?.message || 'Lỗi hệ thống'}`, 'error', true, 3000)
    }
}

export const capNhatComboApi = async (id, formData) => {
    try {
        await CapNhatFoodAndDrink(id, formData)
        SwalConfig('Cập nhật combo thành công', 'success', true)
        history.push('/admin/foodcombo')
    } catch (error) {
        SwalConfig(`${error?.response?.data?.message || 'Lỗi hệ thống'}`, 'error', true, 3000)
    }
}

export const xoaComboApi = (id) => async (dispatch) => {
    try {
        const result = await XoaFoodAndDrink(id)
        dispatch(callApiCombo)
        SwalConfig(result.data.message, 'success', false)
    } catch (error) {
        SwalConfig(`${error?.response?.data?.message || 'Lỗi hệ thống'}`, 'error', true, 3000)
    }
}
