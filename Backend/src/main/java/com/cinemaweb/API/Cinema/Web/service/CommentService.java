package com.cinemaweb.API.Cinema.Web.service;

import com.cinemaweb.API.Cinema.Web.dto.request.CommentRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.CommentResponse;
import com.cinemaweb.API.Cinema.Web.entity.Comment;
import com.cinemaweb.API.Cinema.Web.entity.Movie;
import com.cinemaweb.API.Cinema.Web.entity.User;
import com.cinemaweb.API.Cinema.Web.exception.AppException;
import com.cinemaweb.API.Cinema.Web.exception.ErrorCode;
import com.cinemaweb.API.Cinema.Web.mapper.CommentMapper;
import com.cinemaweb.API.Cinema.Web.repository.CommentRepository;
import com.cinemaweb.API.Cinema.Web.repository.MovieRepository;
import com.cinemaweb.API.Cinema.Web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public CommentResponse createComment(CommentRequest request) {
        User user = userRepository.findById(currentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        Movie movie = movieRepository.findById(Integer.toString(request.getMovieId()))
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_EXISTS));

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTS));
        }

        Comment comment = Comment.builder()
                .user(user)
                .movie(movie)
                .parent(parent)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .createdAt(LocalDateTime.now())
                .build();

        return commentMapper.toCommentResponse(commentRepository.save(comment));
    }

    // Trả về cây bình luận: bình luận gốc kèm danh sách reply (1 cấp)
    public List<CommentResponse> getCommentsByMovie(int movieId) {
        List<Comment> roots =
                commentRepository.findAllByMovie_MovieIdAndParentIsNullOrderByCreatedAtAsc(movieId);

        return roots.stream().map(this::toResponseWithReplies).toList();
    }

    public CommentResponse updateComment(int commentId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTS));

        if (!comment.getUser().getID().equals(currentUserId()))
            throw new AppException(ErrorCode.COMMENT_NOT_OWNER);

        String oldImage = comment.getImageUrl();
        comment.setContent(request.getContent());
        comment.setImageUrl(request.getImageUrl());
        comment.setUpdatedAt(LocalDateTime.now());
        CommentResponse response = commentMapper.toCommentResponse(commentRepository.save(comment));
        // Ảnh đính kèm đã đổi -> xóa ảnh cũ (best-effort)
        if (oldImage != null && !oldImage.equals(request.getImageUrl()))
            cloudinaryService.deleteByUrl(oldImage);
        return response;
    }

    public void deleteComment(int commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTS));

        if (!comment.getUser().getID().equals(currentUserId()) && !isAdmin())
            throw new AppException(ErrorCode.COMMENT_NOT_OWNER);

        cloudinaryService.deleteByUrl(comment.getImageUrl());
        commentRepository.delete(comment);
    }

    private CommentResponse toResponseWithReplies(Comment root) {
        CommentResponse response = commentMapper.toCommentResponse(root);
        List<CommentResponse> replies =
                commentRepository.findAllByParent_CommentIdOrderByCreatedAtAsc(root.getCommentId())
                        .stream()
                        .map(commentMapper::toCommentResponse)
                        .toList();
        response.setReplies(replies);
        return response;
    }

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
