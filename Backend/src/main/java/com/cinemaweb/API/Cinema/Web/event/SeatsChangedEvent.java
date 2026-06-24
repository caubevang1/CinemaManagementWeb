package com.cinemaweb.API.Cinema.Web.event;

/**
 * Phát ra khi trạng thái ghế của một suất chiếu thay đổi (giữ/nhả/đặt/huỷ).
 * Publish NGAY trong transaction; {@code SeatsChangedBridge} (@TransactionalEventListener AFTER_COMMIT)
 * mới broadcast STOMP tới /topic/seats/{scheduleId} khi DB đã commit -> client refetch ra trạng thái mới.
 */
public record SeatsChangedEvent(int scheduleId) {
}
