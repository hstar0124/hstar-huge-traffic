package com.hstar.backend.jwt;

import com.hstar.backend.service.JwtBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;

import java.io.IOException;

@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 요청에서 JWT 토큰을 추출
        String token = resolveToken(request);

        // 토큰이 유효하고 블랙리스트에 없는 경우
        if (token != null && jwtUtil.validateToken(token) && !jwtBlacklistService.isTokenBlacklisted(token)) {
            // 토큰에서 사용자 이름을 추출
            String username = jwtUtil.getUsernameFromToken(token);

            // 사용자 정보를 로드
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // 인증 세부 정보를 설정
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // SecurityContextHolder에 인증 객체 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 필터 체인 계속 진행
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 요청 헤더에서 토큰을 추출
        String bearerToken = request.getHeader("Authorization");

        // 토큰이 존재하고 Bearer로 시작하는 경우
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }

        // 쿠키에서 토큰을 찾는 경우
        if (bearerToken == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
        }

        // 토큰이 없는 경우 null 반환
        return null;
    }
}
