package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.PointUpdateRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.UserCreationRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.UserUpdateRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.PointUpdateResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.UserResponse;
import com.cinemaweb.API.Cinema.Web.entity.User;
import com.cinemaweb.API.Cinema.Web.enums.Roles;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.mapper.UserMapper;
import com.cinemaweb.API.Cinema.Web.repository.PasswordOtpRepository;
import com.cinemaweb.API.Cinema.Web.repository.RoleRepository;
import com.cinemaweb.API.Cinema.Web.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    EmailService emailService;
    PasswordOtpRepository passwordTokenRepository;

    public UserResponse getById(String id) {
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS)));
    }

    public UserResponse getByEmail(String email) {
        return userMapper.toUserResponse(userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS)));
    }

    public List<UserResponse> getAll() {
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::toUserResponse).toList();
    }


    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String id = context.getAuthentication().getName();
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        return userMapper.toUserResponse(user);
    }

    public UserResponse create(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        var roles = roleRepository.findAllById(List.of(Roles.USER.name()));
        user.setRoles(new HashSet<>(roles));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse update(UserUpdateRequest request, String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        if (request.getPassword() != null)
            request.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.UpdateUser(request, user);
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            var roles = roleRepository.findAllById(request.getRoles());
            if (roles.isEmpty())
                throw new AppException(ErrorCode.INVALID_ROLE);
            user.setRoles(new HashSet<>(roles));
        }
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void delete(String id) {
        if (!userRepository.existsById(id))
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        userRepository.deleteById(id);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }


    public User resetPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }


    public PointUpdateResponse updatePoint(PointUpdateRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        if (user.getPoint() == null) {
            user.setPoint(request.getPoint());
        } else user.getBonus(request.getPoint());
        return userMapper.toPointResponse(userRepository.save(user));
    }
}
