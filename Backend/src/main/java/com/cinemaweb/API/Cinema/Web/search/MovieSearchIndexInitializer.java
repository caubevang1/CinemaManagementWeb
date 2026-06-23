package com.cinemaweb.API.Cinema.Web.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Nạp chỉ mục RediSearch từ MySQL khi ứng dụng khởi động xong. Nếu Redis Stack chưa sẵn sàng,
 * chỉ log cảnh báo để không chặn việc khởi động app (search sẽ rỗng cho tới lần đồng bộ sau).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MovieSearchIndexInitializer {

    private final MovieSearchService movieSearchService;

    @EventListener(ApplicationReadyEvent.class)
    public void hydrate() {
        try {
            movieSearchService.reindexAll();
        } catch (Exception e) {
            log.warn("RediSearch: bỏ qua hydrate index lúc khởi động (Redis Stack chưa sẵn sàng?): {}",
                    e.getMessage());
        }
    }
}
