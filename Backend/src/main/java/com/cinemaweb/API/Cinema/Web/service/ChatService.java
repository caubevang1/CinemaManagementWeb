package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.ChatMessageRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ChatMessageResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.ConversationSummaryResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.TicketTransferResponse;
import com.cinemaweb.API.Cinema.Web.entity.Message;
import com.cinemaweb.API.Cinema.Web.entity.User;
import com.cinemaweb.API.Cinema.Web.enums.FriendshipStatus;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.repository.FriendshipRepository;
import com.cinemaweb.API.Cinema.Web.repository.MessageRepository;
import com.cinemaweb.API.Cinema.Web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private TicketTransferService ticketTransferService;

    private static final String TYPE_TEXT = "TEXT";
    private static final String TYPE_TRANSFER = "TRANSFER";

    /** Lưu tin nhắn sau khi xác thực hai người là bạn bè. */
    @Transactional
    public ChatMessageResponse send(String senderId, ChatMessageRequest request) {
        String recipientId = request.getRecipientId();

        if (request.getContent() == null || request.getContent().isBlank())
            throw new AppException(ErrorCode.MESSAGE_EMPTY);

        verifyFriends(senderId, recipientId);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(request.getContent())
                .type(TYPE_TEXT)
                .sentAt(LocalDateTime.now())
                .build();

        return toResponse(messageRepository.save(message));
    }

    /** Tạo một tin nhắn loại TRANSFER (lời mời chuyển nhượng vé) trong cuộc trò chuyện. */
    @Transactional
    public ChatMessageResponse createTransferMessage(String senderId, String recipientId,
                                                     TicketTransferResponse transfer) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content("🎟 Lời mời chuyển nhượng vé")
                .type(TYPE_TRANSFER)
                .transferId(transfer.getId())
                .sentAt(LocalDateTime.now())
                .build();

        return toResponse(messageRepository.save(message));
    }

    /** Lịch sử hội thoại (cũ → mới) giữa current user và một người bạn. */
    public List<ChatMessageResponse> getConversation(String friendId, int page, int size) {
        String me = currentUserId();
        verifyFriends(me, friendId);

        List<Message> messages = messageRepository.findConversation(
                me, friendId, PageRequest.of(page, size));

        // Repo trả mới nhất trước; đảo lại để FE hiển thị theo thứ tự thời gian.
        List<ChatMessageResponse> result = new ArrayList<>(messages.size());
        for (int i = messages.size() - 1; i >= 0; i--)
            result.add(toResponse(messages.get(i)));
        return result;
    }

    /** Đánh dấu mọi tin chưa đọc current user nhận từ friend là đã đọc. Trả về thời điểm đọc, null nếu không có. */
    @Transactional
    public LocalDateTime markConversationRead(String me, String friendId) {
        LocalDateTime now = LocalDateTime.now();
        int updated = messageRepository.markRead(me, friendId, now);
        return updated > 0 ? now : null;
    }

    public long totalUnread(String me) {
        return messageRepository.countByRecipient_IDAndReadAtIsNull(me);
    }

    public Map<String, Long> unreadByFriend(String me) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : messageRepository.countUnreadGroupedBySender(me))
            map.put((String) row[0], (Long) row[1]);
        return map;
    }

    /** Danh sách cuộc trò chuyện gần đây kèm tin cuối, số chưa đọc và trạng thái online. */
    public List<ConversationSummaryResponse> recentConversations(String me) {
        List<String> partnerIds = messageRepository.findConversationPartnerIds(me);
        Map<String, Long> unread = unreadByFriend(me);

        List<ConversationSummaryResponse> result = new ArrayList<>(partnerIds.size());
        for (String partnerId : partnerIds) {
            User partner = userRepository.findById(partnerId).orElse(null);
            if (partner == null) continue;

            List<Message> last = messageRepository.findConversation(me, partnerId, PageRequest.of(0, 1));
            Message lastMsg = last.isEmpty() ? null : last.get(0);

            result.add(ConversationSummaryResponse.builder()
                    .userId(partnerId)
                    .username(partner.getUsername())
                    .avatar(partner.getAvatar())
                    .lastMessage(lastMsg != null ? lastMsg.getContent() : null)
                    .lastMessageAt(lastMsg != null ? lastMsg.getSentAt() : null)
                    .unreadCount(unread.getOrDefault(partnerId, 0L))
                    .online(presenceService.isOnline(partnerId))
                    .build());
        }
        return result;
    }

    private void verifyFriends(String a, String b) {
        if (a.equals(b) || !friendshipRepository.existsAcceptedBetween(a, b))
            throw new AppException(ErrorCode.CHAT_NOT_FRIENDS);
    }

    private ChatMessageResponse toResponse(Message m) {
        String type = m.getType() != null ? m.getType() : TYPE_TEXT;
        TicketTransferResponse transfer = null;
        if (TYPE_TRANSFER.equals(type) && m.getTransferId() != null)
            transfer = ticketTransferService.getResponse(m.getTransferId());

        return ChatMessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSender().getID())
                .recipientId(m.getRecipient().getID())
                .content(m.getContent())
                .sentAt(m.getSentAt())
                .readAt(m.getReadAt())
                .type(type)
                .transfer(transfer)
                .build();
    }

    public String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public boolean isOnline(String userId) {
        return presenceService.isOnline(userId);
    }

    // Cần cho việc lấy danh sách bạn bè đã chấp nhận (dùng ở presence broadcast).
    public List<String> acceptedFriendIds(String userId) {
        return friendshipRepository.findAllAcceptedOf(userId, FriendshipStatus.ACCEPTED).stream()
                .map(f -> f.getRequester().getID().equals(userId)
                        ? f.getAddressee().getID() : f.getRequester().getID())
                .toList();
    }
}
