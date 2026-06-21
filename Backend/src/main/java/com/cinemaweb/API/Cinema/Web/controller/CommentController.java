package com.cinemaweb.API.Cinema.Web.controller;

import com.cinemaweb.API.Cinema.Web.dto.request.CommentRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.ApiResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.CommentResponse;
import com.cinemaweb.API.Cinema.Web.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @GetMapping("/movie/{movieId}")
    public ApiResponse<List<CommentResponse>> getCommentsByMovie(@PathVariable int movieId) {
        return ApiResponse.<List<CommentResponse>>builder()
                .body(commentService.getCommentsByMovie(movieId))
                .build();
    }

    @PostMapping
    public ApiResponse<CommentResponse> createComment(@RequestBody @Valid CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .message("Comment has been created")
                .body(commentService.createComment(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CommentResponse> updateComment(@PathVariable int id,
                                                      @RequestBody @Valid CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .message("Comment with id " + id + " has been updated")
                .body(commentService.updateComment(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable int id) {
        commentService.deleteComment(id);
        return ApiResponse.<Void>builder().message("Comment with id " + id + " has been deleted").build();
    }
}
