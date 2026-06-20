package com.cinemaweb.API.Cinema.Web.repository;


import com.cinemaweb.API.Cinema.Web.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    public boolean existsByUsername(String username);
    public Optional<User> findByUsername(String username);
    public Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
