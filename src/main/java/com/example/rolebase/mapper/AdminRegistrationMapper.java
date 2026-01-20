package com.example.rolebase.mapper;

import com.example.rolebase.config.GlobalMapperConfiguration;
import com.example.rolebase.dto.request.AdminRegistrationRequest;
import com.example.rolebase.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfiguration.class)
public interface AdminRegistrationMapper {

    @Mapping(target = "roles", ignore = true)
    User toEntity(AdminRegistrationRequest request);

}
