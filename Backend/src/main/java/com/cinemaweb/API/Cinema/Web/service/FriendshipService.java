package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.response.FriendshipResponse;
import com.cinemaweb.API.Cinema.Web.entity.Friendship;
import com.cinemaweb.API.Cinema.Web.entity.User;
import com.cinemaweb.API.Cinema.Web.enums.FriendshipStatus;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.repository.FriendshipRepository;
import com.cinemaweb.API.Cinema.Web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FriendshipService {
    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    public FriendshipResponse sendRequest(String addresseeId) {
        String requesterId = currentUserId();

        if (requesterId.equals(addresseeId))
            throw new AppException(ErrorCode.FRIEND_SELF);

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        // Chặn trùng lời mời / quan hệ ở cả hai chiều
        if (friendshipRepository.findByRequester_IDAndAddressee_ID(requesterId, addresseeId).isPresent()
                || friendshipRepository.findByRequester_IDAndAddressee_ID(addresseeId, requesterId).isPresent())
            throw new AppException(ErrorCode.FRIEND_REQUEST_EXISTS);

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .status(FriendshipStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(friendshipRepository.save(friendship), requesterId);
    }

    public FriendshipResponse acceptRequest(int friendshipId) {
        Friendship friendship = getPendingForAddressee(friendshipId);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setRespondedAt(LocalDateTime.now());
        return toResponse(friendshipRepository.save(friendship), currentUserId());
    }

    public FriendshipResponse declineRequest(int friendshipId) {
        Friendship friendship = getPendingForAddressee(friendshipId);
        friendship.setStatus(FriendshipStatus.DECLINED);
        friendship.setRespondedAt(LocalDateTime.now());
        return toResponse(friendshipRepository.save(friendship), currentUserId());
    }

    public void unfriend(int friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_EXISTS));

        String me = currentUserId();
        if (!friendship.getRequester().getID().equals(me) && !friendship.getAddressee().getID().equals(me))
            throw new AppException(ErrorCode.FRIEND_NOT_AUTHORIZED);

        friendshipRepository.delete(friendship);
    }

    public List<FriendshipResponse> listFriends() {
        String me = currentUserId();
        return friendshipRepository.findAllAcceptedOf(me, FriendshipStatus.ACCEPTED)
                .stream()
                .map(f -> toResponse(f, me))
                .toList();
    }

    public List<FriendshipResponse> listPendingRequests() {
        String me = currentUserId();
        return friendshipRepository.findAllByAddressee_IDAndStatus(me, FriendshipStatus.PENDING)
                .stream()
                .map(f -> toResponse(f, me))
                .toList();
    }

    // Lấy lời mời PENDING và xác nhận current user chính là người nhận
    private Friendship getPendingForAddressee(int friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIEND_REQUEST_NOT_EXISTS));

        if (friendship.getStatus() != FriendshipStatus.PENDING)
            throw new AppException(ErrorCode.FRIEND_REQUEST_NOT_EXISTS);

        if (!friendship.getAddressee().getID().equals(currentUserId()))
            throw new AppException(ErrorCode.FRIEND_NOT_AUTHORIZED);

        return friendship;
    }

    private FriendshipResponse toResponse(Friendship f, String viewerId) {
        User other = f.getRequester().getID().equals(viewerId) ? f.getAddressee() : f.getRequester();
        return FriendshipResponse.builder()
                .friendshipId(f.getFriendshipId())
                .status(f.getStatus())
                .createdAt(f.getCreatedAt())
                .otherUserId(other.getID())
                .otherUsername(other.getUsername())
                .otherAvatar(other.getAvatar())
                .build();
    }

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
