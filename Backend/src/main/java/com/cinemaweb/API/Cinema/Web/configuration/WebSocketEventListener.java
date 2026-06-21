package com.cinemaweb.API.Cinema.Web.configuration;

import com.cinemaweb.API.Cinema.Web.dto.response.PresenceEvent;
import com.cinemaweb.API.Cinema.Web.service.ChatService;
import com.cinemaweb.API.Cinema.Web.service.PresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * Cập nhật trạng thái online/offline khi phiên WebSocket kết nối / ngắt, và
 * thông báo cho bạn bè của user đó.
 */
@Component
public class WebSocketEventListener {

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onConnected(SessionConnectedEvent event) {
        Principal user = event.getUser();
        if (user == null) return;
        if (presenceService.markOnline(user.getName()))
            broadcastPresence(user.getName(), true);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        if (user == null) return;
        if (presenceService.markOffline(user.getName()))
            broadcastPresence(user.getName(), false);
    }

    private void broadcastPresence(String userId, boolean online) {
        PresenceEvent payload = PresenceEvent.builder().userId(userId).online(online).build();
        for (String friendId : chatService.acceptedFriendIds(userId))
            messagingTemplate.convertAndSendToUser(friendId, "/queue/presence", payload);
    }
}
