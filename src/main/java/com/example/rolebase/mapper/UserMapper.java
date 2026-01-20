package com.example.rolebase.mapper;

import com.example.rolebase.config.GlobalMapperConfiguration;
import com.example.rolebase.dto.request.RegistrationRequest;
import com.example.rolebase.dto.response.UserResponse;
import com.example.rolebase.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = GlobalMapperConfiguration.class)
public interface UserMapper {

    User toEntity(RegistrationRequest request);

    UserResponse toResponse(User response);

    List<UserResponse> toResponseList(List<User> users);

}
