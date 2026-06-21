package com.cinemaweb.API.Cinema.Web.mapper;

import com.cinemaweb.API.Cinema.Web.dto.response.CommentResponse;
import com.cinemaweb.API.Cinema.Web.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(source = "user.ID", target = "authorId")
    @Mapping(source = "user.username", target = "authorName")
    @Mapping(source = "user.avatar", target = "avatar")
    @Mapping(source = "parent.commentId", target = "parentId")
    @Mapping(target = "replies", ignore = true) // cây reply được dựng ở service
    CommentResponse toCommentResponse(Comment comment);
}
