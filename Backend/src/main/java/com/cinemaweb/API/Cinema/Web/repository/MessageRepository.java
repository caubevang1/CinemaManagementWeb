package com.cinemaweb.API.Cinema.Web.repository;

import com.cinemaweb.API.Cinema.Web.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    // Lịch sử hội thoại giữa hai người (cả hai chiều), mới nhất trước — phân trang.
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.ID = :a AND m.recipient.ID = :b) OR (m.sender.ID = :b AND m.recipient.ID = :a) " +
            "ORDER BY m.sentAt DESC")
    List<Message> findConversation(@Param("a") String a, @Param("b") String b, Pageable pageable);

    // Đánh dấu đã đọc mọi tin chưa đọc mà current user nhận từ một người bạn.
    @Modifying
    @Query("UPDATE Message m SET m.readAt = :now " +
            "WHERE m.recipient.ID = :me AND m.sender.ID = :friend AND m.readAt IS NULL")
    int markRead(@Param("me") String me, @Param("friend") String friend, @Param("now") LocalDateTime now);

    // Tổng số tin chưa đọc của current user.
    long countByRecipient_IDAndReadAtIsNull(String recipientId);

    // Số tin chưa đọc gom theo người gửi (cho badge từng cuộc trò chuyện).
    @Query("SELECT m.sender.ID, COUNT(m) FROM Message m " +
            "WHERE m.recipient.ID = :me AND m.readAt IS NULL GROUP BY m.sender.ID")
    List<Object[]> countUnreadGroupedBySender(@Param("me") String me);

    // Danh sách id người mà current user từng nhắn (đối tác hội thoại), mới nhất trước.
    @Query("SELECT CASE WHEN m.sender.ID = :me THEN m.recipient.ID ELSE m.sender.ID END AS partner " +
            "FROM Message m WHERE m.sender.ID = :me OR m.recipient.ID = :me " +
            "GROUP BY CASE WHEN m.sender.ID = :me THEN m.recipient.ID ELSE m.sender.ID END " +
            "ORDER BY MAX(m.sentAt) DESC")
    List<String> findConversationPartnerIds(@Param("me") String me);
}
