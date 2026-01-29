package com.example.rolebase.repository;

import com.example.rolebase.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u " + "LEFT JOIN FETCH u.roles ur " +
            "LEFT JOIN FETCH ur.role " + "WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.enabled = :enabled " +
            "WHERE LOWER(u.username) = LOWER(:username)")
    int updateUserEnabledStatus(@Param("username") String username, @Param("enabled") boolean enabled);
}