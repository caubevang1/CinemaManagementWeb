package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.ChatMessageResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.ConversationSummaryResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.UnreadResponse;
import com.cinemaweb.API.Cinema.Web.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/conversations")
    public ApiResponse<List<ConversationSummaryResponse>> conversations() {
        return ApiResponse.<List<ConversationSummaryResponse>>builder()
                .body(chatService.recentConversations(chatService.currentUserId()))
                .build();
    }

    @GetMapping("/online")
    public ApiResponse<List<String>> onlineFriends() {
        String me = chatService.currentUserId();
        return ApiResponse.<List<String>>builder()
                .body(chatService.acceptedFriendIds(me).stream()
                        .filter(chatService::isOnline)
                        .toList())
                .build();
    }

    @GetMapping("/unread")
    public ApiResponse<UnreadResponse> unread() {
        String me = chatService.currentUserId();
        return ApiResponse.<UnreadResponse>builder()
                .body(UnreadResponse.builder()
                        .total(chatService.totalUnread(me))
                        .byFriend(chatService.unreadByFriend(me))
                        .build())
                .build();
    }

    @GetMapping("/{friendId}")
    public ApiResponse<List<ChatMessageResponse>> conversation(
            @PathVariable String friendId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        return ApiResponse.<List<ChatMessageResponse>>builder()
                .body(chatService.getConversation(friendId, page, size))
                .build();
    }

    @PostMapping("/{friendId}/read")
    public ApiResponse<Void> markRead(@PathVariable String friendId) {
        chatService.markConversationRead(chatService.currentUserId(), friendId);
        return ApiResponse.<Void>builder().message("Đã đánh dấu đã đọc").build();
    }
}
