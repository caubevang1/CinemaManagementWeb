package com.cinemaweb.API.Cinema.Web.messaging;

import com.cinemaweb.API.Cinema.Web.event.SeatsChangedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Broadcast realtime sơ đồ ghế: service publish SeatsChangedEvent trong transaction, bridge này
 * chỉ gửi tới /topic/seats/{scheduleId} SAU KHI commit (AFTER_COMMIT) -> client (đang xem suất đó)
 * gọi lại API danh sách ghế và thấy trạng thái mới ngay, không cần F5.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SeatsChangedBridge {

    SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSeatsChanged(SeatsChangedEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/seats/" + event.scheduleId(),
                Map.of("scheduleId", event.scheduleId()));
    }
}
