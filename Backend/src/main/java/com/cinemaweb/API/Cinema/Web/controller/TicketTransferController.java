package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.TicketTransferRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.ChatMessageResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.TicketTransferResponse;
import com.cinemaweb.API.Cinema.Web.service.ChatService;
import com.cinemaweb.API.Cinema.Web.service.TicketTransferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ticket-transfer")
public class TicketTransferController {

    @Autowired
    private TicketTransferService transferService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ApiResponse<TicketTransferResponse> offer(@RequestBody @Valid TicketTransferRequest request) {
        String me = transferService.currentUserId();
        TicketTransferResponse response = transferService.offer(me, request);

        // Tạo tin nhắn loại TRANSFER và phát như tin nhắn bình thường cho cả hai bên.
        ChatMessageResponse msg = chatService.createTransferMessage(me, response.getToUserId(), response);
        messagingTemplate.convertAndSendToUser(response.getToUserId(), "/queue/messages", msg);
        messagingTemplate.convertAndSendToUser(me, "/queue/messages", msg);

        return ApiResponse.<TicketTransferResponse>builder()
                .message("Đã gửi lời mời chuyển nhượng vé")
                .body(response)
                .build();
    }

    @PostMapping("/{id}/accept")
    public ApiResponse<TicketTransferResponse> accept(@PathVariable int id) {
        TicketTransferResponse response = transferService.accept(id, transferService.currentUserId());
        notifyResult(response);
        return ApiResponse.<TicketTransferResponse>builder()
                .message("Đã nhận vé")
                .body(response)
                .build();
    }

    @PostMapping("/{id}/decline")
    public ApiResponse<TicketTransferResponse> decline(@PathVariable int id) {
        TicketTransferResponse response = transferService.decline(id, transferService.currentUserId());
        notifyResult(response);
        return ApiResponse.<TicketTransferResponse>builder()
                .message("Đã từ chối lời mời")
                .body(response)
                .build();
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<TicketTransferResponse> cancel(@PathVariable int id) {
        TicketTransferResponse response = transferService.cancel(id, transferService.currentUserId());
        notifyResult(response);
        return ApiResponse.<TicketTransferResponse>builder()
                .message("Đã thu hồi lời mời")
                .body(response)
                .build();
    }

    // Cập nhật trạng thái bong bóng tin nhắn ở cả hai phía.
    private void notifyResult(TicketTransferResponse response) {
        messagingTemplate.convertAndSendToUser(response.getFromUserId(), "/queue/ticket-transfer-result", response);
        messagingTemplate.convertAndSendToUser(response.getToUserId(), "/queue/ticket-transfer-result", response);
    }

    @GetMapping("/incoming")
    public ApiResponse<List<TicketTransferResponse>> incoming() {
        return ApiResponse.<List<TicketTransferResponse>>builder()
                .body(transferService.listIncoming(transferService.currentUserId()))
                .build();
    }

    @GetMapping("/outgoing")
    public ApiResponse<List<TicketTransferResponse>> outgoing() {
        return ApiResponse.<List<TicketTransferResponse>>builder()
                .body(transferService.listOutgoing(transferService.currentUserId()))
                .build();
    }
}
