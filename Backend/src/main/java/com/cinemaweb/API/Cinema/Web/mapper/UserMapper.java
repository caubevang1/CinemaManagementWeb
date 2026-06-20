package com.cinemaweb.API.Cinema.Web.mapper;

import com.cinemaweb.API.Cinema.Web.dto.request.UserCreationRequest;
import com.cinemaweb.API.Cinema.Web.dto.request.UserUpdateRequest;
import com.cinemaweb.API.Cinema.Web.dto.response.PointUpdateResponse;
import com.cinemaweb.API.Cinema.Web.dto.response.UserResponse;
import com.cinemaweb.API.Cinema.Web.entity.User;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void UpdateUser(UserUpdateRequest request, @MappingTarget User user);

    PointUpdateResponse toPointResponse(User user);

}
