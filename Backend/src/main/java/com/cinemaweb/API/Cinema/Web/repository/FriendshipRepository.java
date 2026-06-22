package com.cinemaweb.API.Cinema.Web.repository;

import com.cinemaweb.API.Cinema.Web.entity.Friendship;
import com.cinemaweb.API.Cinema.Web.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
    Optional<Friendship> findByRequester_IDAndAddressee_ID(String requesterId, String addresseeId);

    // Lời mời đến đang chờ xử lý
    List<Friendship> findAllByAddressee_IDAndStatus(String addresseeId, FriendshipStatus status);

    // Bạn bè đã được chấp nhận, bất kể current user là người gửi hay người nhận
    @Query("SELECT f FROM Friendship f WHERE f.status = :status " +
            "AND (f.requester.ID = :userId OR f.addressee.ID = :userId)")
    List<Friendship> findAllAcceptedOf(@Param("userId") String userId,
                                       @Param("status") FriendshipStatus status);

    // Mọi quan hệ (PENDING/ACCEPTED) liên quan tới user, bất kể chiều — dùng cho tìm bạn.
    @Query("SELECT f FROM Friendship f WHERE f.requester.ID = :userId OR f.addressee.ID = :userId")
    List<Friendship> findAllInvolving(@Param("userId") String userId);

    // Hai người đã là bạn bè (ACCEPTED), bất kể chiều gửi lời mời.
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE f.status = " +
            "com.cinemaweb.API.Cinema.Web.enums.FriendshipStatus.ACCEPTED " +
            "AND ((f.requester.ID = :a AND f.addressee.ID = :b) " +
            "OR (f.requester.ID = :b AND f.addressee.ID = :a))")
    boolean existsAcceptedBetween(@Param("a") String a, @Param("b") String b);
}
