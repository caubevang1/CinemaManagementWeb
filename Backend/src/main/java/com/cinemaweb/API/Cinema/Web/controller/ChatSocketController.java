package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.ChatMessageRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.ChatReadRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.TypingSignal;
import com.cinemaweb.API.Cinema.Web.dto.response.ChatMessageResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.ReadReceipt;
import com.cinemaweb.API.Cinema.Web.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * Xử lý các frame STOMP gửi tới prefix /app. Định danh người gửi lấy từ
 * Principal (đặt bởi StompAuthChannelInterceptor, getName() = userId).
 */
@Controller
public class ChatSocketController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void send(@Payload ChatMessageRequest request, Principal principal) {
        String senderId = principal.getName();
        ChatMessageResponse response = chatService.send(senderId, request);

        // Gửi tới người nhận và echo về người gửi (đồng bộ nhiều tab của người gửi).
        messagingTemplate.convertAndSendToUser(response.getRecipientId(), "/queue/messages", response);
        messagingTemplate.convertAndSendToUser(senderId, "/queue/messages", response);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingSignal signal, Principal principal) {
        TypingSignal forward = TypingSignal.builder()
                .fromUserId(principal.getName())
                .typing(signal.isTyping())
                .build();
        messagingTemplate.convertAndSendToUser(signal.getRecipientId(), "/queue/typing", forward);
    }

    @MessageMapping("/chat.read")
    public void read(@Payload ChatReadRequest request, Principal principal) {
        String me = principal.getName();
        LocalDateTime readAt = chatService.markConversationRead(me, request.getFriendId());
        if (readAt == null) return;

        // Báo cho người bạn (người đã gửi tin) biết tin của họ đã được đọc.
        ReadReceipt receipt = ReadReceipt.builder().byUserId(me).readAt(readAt).build();
        messagingTemplate.convertAndSendToUser(request.getFriendId(), "/queue/read", receipt);
    }
}
