package com.example.ums.mapper;

import com.example.ums.config.GlobalMapperConfiguration;
import com.example.ums.dto.request.AdminRegistrationRequest;
import com.example.ums.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfiguration.class)
public interface AdminRegistrationMapper {

    @Mapping(target = "roles", ignore = true)
    User toEntity(AdminRegistrationRequest request);

}
