package com.cinemaweb.API.Cinema.Web.repository;


import com.cinemaweb.API.Cinema.Web.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    public boolean existsByUsername(String username);

    // JOIN FETCH roles + permissions để buildScope() không sinh N+1 và không
    // phụ thuộc Open-Session-In-View khi duyệt LAZY roles/permissions.
    @Query("select u from User u left join fetch u.roles r left join fetch r.permissions where u.username = :username")
    public Optional<User> findByUsername(@Param("username") String username);
    public Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Tìm user theo username/email/họ tên, loại trừ chính mình (dùng cho kết bạn).
    @Query("select u from User u where u.ID <> :selfId and (" +
           "lower(u.username) like lower(concat('%', :q, '%')) or " +
           "lower(u.email) like lower(concat('%', :q, '%')) or " +
           "lower(u.firstName) like lower(concat('%', :q, '%')) or " +
           "lower(u.lastName) like lower(concat('%', :q, '%')))")
    List<User> searchUsers(@Param("q") String q, @Param("selfId") String selfId);
}
