package com.example.rolebase.config;

import com.example.rolebase.mapper.support.RoleMapperUtils;
import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@MapperConfig(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = RoleMapperUtils.class)
public interface GlobalMapperConfiguration {}
