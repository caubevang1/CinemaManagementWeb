package com.cinemaweb.API.Cinema.Web.controller;


import com.cinemaweb.API.Cinema.Web.dto.request.PermissionRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.PermissionResponse;
import com.cinemaweb.API.Cinema.Web.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PermissionController {

    PermissionService permissionService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest request) {
        return ApiResponse.<PermissionResponse>builder()
                .body(permissionService.create(request))
                .build();
    }

    @GetMapping("/{permissionName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PermissionResponse> get(@PathVariable("permissionName") String name) {
        return ApiResponse.<PermissionResponse>builder()
                .body(permissionService.get(name))
                .build();
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .body(permissionService.getAll())
                .build();
    }

    @DeleteMapping("/{permission}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable("permission") String permission) {
        permissionService.delete(permission);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete() {
        permissionService.deleteAll();
        return ApiResponse.<Void>builder().build();
    }
}
