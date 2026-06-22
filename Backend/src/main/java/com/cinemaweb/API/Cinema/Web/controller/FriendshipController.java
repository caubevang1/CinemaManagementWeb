package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.FriendRequestDto;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.FriendshipResponse;
import com.cinemaweb.API.Cinema.Web.service.FriendshipService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friend")
public class FriendshipController {
    @Autowired
    private FriendshipService friendshipService;

    @GetMapping
    public ApiResponse<List<FriendshipResponse>> listFriends() {
        return ApiResponse.<List<FriendshipResponse>>builder()
                .body(friendshipService.listFriends())
                .build();
    }

    @GetMapping("/requests")
    public ApiResponse<List<FriendshipResponse>> listPendingRequests() {
        return ApiResponse.<List<FriendshipResponse>>builder()
                .body(friendshipService.listPendingRequests())
                .build();
    }

    @PostMapping("/request")
    public ApiResponse<FriendshipResponse> sendRequest(@RequestBody @Valid FriendRequestDto request) {
        return ApiResponse.<FriendshipResponse>builder()
                .message("Friend request has been sent")
                .body(friendshipService.sendRequest(request.getAddresseeId()))
                .build();
    }

    @PostMapping("/{id}/accept")
    public ApiResponse<FriendshipResponse> accept(@PathVariable int id) {
        return ApiResponse.<FriendshipResponse>builder()
                .message("Friend request accepted")
                .body(friendshipService.acceptRequest(id))
                .build();
    }

    @PostMapping("/{id}/decline")
    public ApiResponse<Void> decline(@PathVariable int id) {
        friendshipService.declineRequest(id);
        return ApiResponse.<Void>builder()
                .message("Friend request declined")
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> unfriend(@PathVariable int id) {
        friendshipService.unfriend(id);
        return ApiResponse.<Void>builder().message("Friendship with id " + id + " has been removed").build();
    }
}
