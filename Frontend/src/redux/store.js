import { configureStore } from "@reduxjs/toolkit";
import BannerReducer from './reducers/BannerReducer'
import UserReducer from './reducers/UserReducer'
import FilmReducer from "./reducers/FilmReducer";
import CinemaReducer from "./reducers/CinemaReducer";
import BookingReducer from "./reducers/BookingReducer";
import RoomReducer from "./reducers/RoomReducer";
import ScheduleReducer from "./reducers/ScheduleReducer";
import ChatReducer from "./reducers/ChatReducer";
import FoodComboReducer from "./reducers/FoodComboReducer";

export const store = configureStore({
    reducer: {
        BannerReducer,
        UserReducer,
        FilmReducer,
        CinemaReducer,
        BookingReducer,
        RoomReducer,
        ScheduleReducer,
        ChatReducer,
        FoodComboReducer,
    }
})

