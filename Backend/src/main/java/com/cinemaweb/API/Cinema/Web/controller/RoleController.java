package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.RoleRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.RoleResponse;
import com.cinemaweb.API.Cinema.Web.service.RoleService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class RoleController {
    RoleService roleService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoleResponse> create(@RequestBody @Valid RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .body(roleService.create(request))
                .build();
    }

    @GetMapping("/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoleResponse> get(@PathVariable("roleName") String roleName) {
        return ApiResponse.<RoleResponse>builder()
                .body(roleService.get(roleName))
                .build();
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder()
                .body(roleService.getAll())
                .build();
    }

    @DeleteMapping("/{roleName}")
    @PostAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable("roleName") String roleName) {
        roleService.delete(roleName);
        return new ApiResponse<Void>();
    }

    @DeleteMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteAll() {
        roleService.deleteAll();
        return new ApiResponse<Void>();
    }
}
