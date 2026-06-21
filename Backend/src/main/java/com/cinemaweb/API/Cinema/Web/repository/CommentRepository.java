package com.cinemaweb.API.Cinema.Web.repository;

import com.cinemaweb.API.Cinema.Web.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    // Bình luận gốc của một phim (không phải reply)
    List<Comment> findAllByMovie_MovieIdAndParentIsNullOrderByCreatedAtAsc(int movieId);

    // Các reply của một bình luận
    List<Comment> findAllByParent_CommentIdOrderByCreatedAtAsc(int parentId);
}
