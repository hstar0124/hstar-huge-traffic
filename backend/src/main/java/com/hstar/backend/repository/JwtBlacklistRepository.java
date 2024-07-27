package com.hstar.backend.repository;

import com.hstar.backend.entity.JwtBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, Long> {

    // 토큰으로 블랙리스트 항목을 찾음
    Optional<JwtBlacklist> findByToken(String token);

//    @Query(value = "SELECT * FROM jwt_blacklist WHERE username = :username ORDER BY expiration_time LIMIT 1", nativeQuery = true)
//    Optional<JwtBlacklist> findTopByUsernameOrderByExpirationTime(@Param("username") String username);

    // 사용자 이름으로 만료 시간이 최신인 블랙리스트 항목을 찾음
    Optional<JwtBlacklist> findFirstByUsernameOrderByExpirationTimeDesc(String username);


}