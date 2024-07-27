package com.hstar.backend.service;

import com.hstar.backend.entity.JwtBlacklist;
import com.hstar.backend.jwt.JwtUtil;
import com.hstar.backend.repository.JwtBlacklistRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@AllArgsConstructor
public class JwtBlacklistService {

    private final JwtBlacklistRepository jwtBlacklistRepository;
    private final JwtUtil jwtUtil;


    public void blacklistToken(String token, LocalDateTime expirationTime, String username) {
        JwtBlacklist jwtBlacklist = new JwtBlacklist();
        jwtBlacklist.setToken(token); // 블랙리스트에 저장할 토큰
        jwtBlacklist.setExpirationTime(expirationTime); // 토큰의 만료 시간
        jwtBlacklist.setUsername(username); // 사용자 이름
        jwtBlacklistRepository.save(jwtBlacklist); // 블랙리스트 저장소에 저장
    }


    public boolean isTokenBlacklisted(String currentToken) {
        String username = jwtUtil.getUsernameFromToken(currentToken); // 토큰에서 사용자 이름 추출
        Optional<JwtBlacklist> blacklistedToken = jwtBlacklistRepository.findFirstByUsernameOrderByExpirationTimeDesc(username);
        if (blacklistedToken.isEmpty()) {
            return false; // 블랙리스트에 토큰이 없으면 false 반환
        }
        Instant instant = jwtUtil.getExpirationDateFromToken(currentToken).toInstant(); // 현재 토큰의 만료 시간
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()); // 만료 시간을 LocalDateTime으로 변환
        // 블랙리스트에서 가장 최근의 만료 시간과 비교
        return blacklistedToken.get().getExpirationTime().isAfter(localDateTime.minusMinutes(60));
    }
}
