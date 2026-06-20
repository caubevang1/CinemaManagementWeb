package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.RoomRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.RoomResponse;
import com.cinemaweb.API.Cinema.Web.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;

    @GetMapping
    public ApiResponse<List<RoomResponse>> getAllRooms() {
        return ApiResponse.<List<RoomResponse>>builder()
                .body(roomService.getAllRooms())
                .build();
    }

    @GetMapping("/{roomId}")
    public ApiResponse<RoomResponse> getRoom(@PathVariable String roomId) {
        return ApiResponse.<RoomResponse>builder()
                .body(roomService.getRoom(roomId))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> createRoom(@RequestBody @Valid RoomRequest roomCreateRequest) {
        roomService.createRoom(roomCreateRequest);
        return ApiResponse.<Void>builder().message("Room has been created").build();
    }

    @PutMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateRoom(@RequestBody @Valid RoomRequest roomUpdateRequest, @PathVariable String roomId) {
        roomService.updateRoom(roomId, roomUpdateRequest);
        return ApiResponse.<Void>builder().message("Room with id " + roomId + " has been updated").build();
    }

    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteRoom(@PathVariable String roomId) {
        roomService.deleteRoom(roomId);
        return ApiResponse.<Void>builder().message("Room with id " + roomId + " has been deleted!").build();
    }
}
