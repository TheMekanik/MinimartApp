package com.minimart.api.repository;

import com.minimart.api.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);
    Optional<Users> findByUsername(String username);
}
