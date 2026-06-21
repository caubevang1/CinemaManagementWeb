package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    Integer commentId;
    String content;
    String imageUrl;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Thông tin tác giả
    String authorId;
    String authorName;
    String avatar;

    Integer parentId;
    List<CommentResponse> replies;
}
