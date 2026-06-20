package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.PermissionRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.PermissionResponse;
import com.cinemaweb.API.Cinema.Web.entity.Permission;
import com.cinemaweb.API.Cinema.Web.mapper.PermissionMapper;
import com.cinemaweb.API.Cinema.Web.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse create(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    public PermissionResponse get(String permissionName) {
        return permissionMapper.toPermissionResponse(permissionRepository.findById(permissionName)
                .orElseThrow(() -> new RuntimeException("Permission do not exists")));
    }

    public List<PermissionResponse> getAll() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public void delete(String permissionName) {
        permissionRepository.deleteById(permissionName);
    }

    public void deleteAll() {
        permissionRepository.deleteAll();
    }

}