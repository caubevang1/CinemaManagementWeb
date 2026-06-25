package com.cinemaweb.API.Cinema.Web.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Nạp các chỉ mục RediSearch (phim, user, rạp) từ MySQL khi ứng dụng khởi động xong.
 * Nếu Redis Stack chưa sẵn sàng, mỗi chỉ mục chỉ log cảnh báo riêng để không chặn việc
 * khởi động app (search tương ứng sẽ rỗng cho tới lần đồng bộ sau).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MovieSearchIndexInitializer {

    private final MovieSearchService movieSearchService;
    private final UserSearchService userSearchService;
    private final CinemaSearchService cinemaSearchService;

    @EventListener(ApplicationReadyEvent.class)
    public void hydrate() {
        reindex("movie", movieSearchService::reindexAll);
        reindex("user", userSearchService::reindexAll);
        reindex("cinema", cinemaSearchService::reindexAll);
    }

    private void reindex(String name, Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            log.warn("RediSearch: bỏ qua hydrate index '{}' lúc khởi động (Redis Stack chưa sẵn sàng?): {}",
                    name, e.getMessage());
        }
    }
}
