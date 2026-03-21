package com.example.ums.mapper;

import com.example.ums.config.GlobalMapperConfiguration;
import com.example.ums.dto.request.UpdateUserRequest;
import com.example.ums.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = GlobalMapperConfiguration.class)
public interface UpdateUserRequestMapper {

    void updateUserFromRequest(UpdateUserRequest request, @MappingTarget User user);
}
