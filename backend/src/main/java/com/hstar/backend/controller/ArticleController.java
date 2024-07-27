package com.hstar.backend.controller;

import com.hstar.backend.dto.ApiResponse;
import com.hstar.backend.dto.WriteArticleDto;
import com.hstar.backend.entity.Article;
import com.hstar.backend.service.ArticleService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/boards")
public class ArticleController {
    private final ArticleService articleService;


    @PostMapping("/{boardId}/articles")
    public ResponseEntity<ApiResponse<Article>> writeArticle(
            @RequestBody WriteArticleDto writeArticleDto) {
        Article article = articleService.writeArticle(writeArticleDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(article, "Article created successfully"));
    }
}