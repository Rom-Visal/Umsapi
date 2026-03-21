package com.example.ums.mapper;

import com.example.ums.config.GlobalMapperConfiguration;
import com.example.ums.dto.request.RegistrationRequest;
import com.example.ums.dto.response.UserResponse;
import com.example.ums.entity.User;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfiguration.class)
public interface UserMapper {

    User toEntity(RegistrationRequest request);

    UserResponse toResponse(User response);
}
