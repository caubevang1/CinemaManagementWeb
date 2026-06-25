package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.PointUpdateRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.SetTransferPinRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.UserCreationRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.UserUpdateRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.FriendSearchResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.PointUpdateResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.UserResponse;
import com.cinemaweb.API.Cinema.Web.entity.Friendship;
import com.cinemaweb.API.Cinema.Web.entity.User;
import com.cinemaweb.API.Cinema.Web.enums.FriendshipStatus;
import com.cinemaweb.API.Cinema.Web.enums.Roles;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.mapper.UserMapper;
import com.cinemaweb.API.Cinema.Web.repository.FriendshipRepository;
import com.cinemaweb.API.Cinema.Web.repository.RoleRepository;
import com.cinemaweb.API.Cinema.Web.repository.UserRepository;
import com.cinemaweb.API.Cinema.Web.search.UserSearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
    CloudinaryService cloudinaryService;
    FriendshipRepository friendshipRepository;
    UserSearchService userSearchService;

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

    public List<FriendSearchResponse> searchUsers(String q) {
        if (q == null || q.isBlank())
            return List.of();
        String selfId = SecurityContextHolder.getContext().getAuthentication().getName();

        // Trạng thái quan hệ với từng người (other user id -> status).
        Map<String, FriendshipStatus> statusByUser = new HashMap<>();
        for (Friendship f : friendshipRepository.findAllInvolving(selfId)) {
            String otherId = f.getRequester().getID().equals(selfId)
                    ? f.getAddressee().getID() : f.getRequester().getID();
            statusByUser.put(otherId, f.getStatus());
        }

        // Ứng viên từ RediSearch; nếu Redis Stack lỗi thì rơi về SQL LIKE để không mất chức năng.
        List<FriendSearchResponse> candidates;
        try {
            candidates = userSearchService.search(q.trim());
        } catch (Exception e) {
            log.warn("RediSearch user search thất bại, fallback sang SQL: {}", e.getMessage());
            candidates = userRepository.searchUsers(q.trim(), selfId).stream()
                    .map(u -> FriendSearchResponse.builder()
                            .id(u.getID())
                            .username(u.getUsername())
                            .firstName(u.getFirstName())
                            .lastName(u.getLastName())
                            .avatar(u.getAvatar())
                            .email(u.getEmail())
                            .build())
                    .collect(java.util.stream.Collectors.toList());
        }

        List<FriendSearchResponse> result = new ArrayList<>();
        for (FriendSearchResponse r : candidates) {
            if (selfId.equals(r.getId())) // loại chính mình khỏi kết quả
                continue;
            FriendshipStatus status = statusByUser.get(r.getId());
            r.setFriendshipStatus(status == FriendshipStatus.ACCEPTED ? "ACCEPTED"
                    : status == FriendshipStatus.PENDING ? "PENDING" : "NONE");
            result.add(r);
        }
        // Sắp xếp: bạn bè (ACCEPTED) trước, rồi PENDING, cuối là NONE.
        result.sort(Comparator.comparingInt(r -> switch (r.getFriendshipStatus()) {
            case "ACCEPTED" -> 0;
            case "PENDING" -> 1;
            default -> 2;
        }));
        return result.stream().limit(20).toList();
    }


    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String id = context.getAuthentication().getName();
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        UserResponse response = userMapper.toUserResponse(user);
        response.setHasTransferPin(user.getTransferPin() != null);
        return response;
    }

    /** Đặt hoặc đổi mã PIN chuyển nhượng (kiểu ngân hàng). */
    public void setTransferPin(SetTransferPinRequest request) {
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        // Đã có PIN -> phải nhập đúng PIN hiện tại mới được đổi.
        if (user.getTransferPin() != null) {
            if (request.getCurrentPin() == null
                    || !passwordEncoder.matches(request.getCurrentPin(), user.getTransferPin()))
                throw new AppException(ErrorCode.TICKET_PIN_WRONG_CURRENT);
        }

        user.setTransferPin(passwordEncoder.encode(request.getNewPin()));
        userRepository.save(user);
    }

    public UserResponse create(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        var roles = roleRepository.findAllById(List.of(Roles.USER.name()));
        user.setRoles(new HashSet<>(roles));
        User saved = userRepository.save(user);
        userSearchService.upsert(saved); // đồng bộ chỉ mục tìm kiếm
        return userMapper.toUserResponse(saved);
    }

    public UserResponse update(UserUpdateRequest request, String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        String oldAvatar = user.getAvatar();
        if (request.getPassword() != null)
            request.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.UpdateUser(request, user);
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            var roles = roleRepository.findAllById(request.getRoles());
            if (roles.isEmpty())
                throw new AppException(ErrorCode.INVALID_ROLE);
            user.setRoles(new HashSet<>(roles));
        }
        User saved = userRepository.save(user);
        userSearchService.upsert(saved); // đồng bộ chỉ mục tìm kiếm
        UserResponse response = userMapper.toUserResponse(saved);
        // Avatar đã đổi sang URL mới -> xóa ảnh cũ trên Cloudinary (best-effort)
        if (request.getAvatar() != null && !request.getAvatar().equals(oldAvatar))
            cloudinaryService.deleteByUrl(oldAvatar);
        return response;
    }

    public void delete(String id) {
        if (!userRepository.existsById(id))
            throw new AppException(ErrorCode.USER_NOT_EXISTS);
        userRepository.deleteById(id);
        userSearchService.remove(id); // gỡ khỏi chỉ mục tìm kiếm
    }

    public void deleteAll() {
        userRepository.deleteAll();
        userSearchService.reindexAll(); // làm rỗng chỉ mục tìm kiếm
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
