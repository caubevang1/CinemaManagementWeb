package com.cinemaweb.API.Cinema.Web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentRequest {
    int movieId;

    @NotBlank(message = "Nội dung bình luận không được để trống!")
    String content;

    // URL ảnh đính kèm (optional), lấy từ POST /upload/image
    String imageUrl;

    // null nếu là bình luận gốc; có giá trị nếu là trả lời một bình luận khác
    Integer parentId;
}
