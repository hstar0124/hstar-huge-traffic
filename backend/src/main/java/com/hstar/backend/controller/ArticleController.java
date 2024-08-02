package com.hstar.backend.controller;

import com.hstar.backend.dto.ApiResponse;
import com.hstar.backend.dto.EditArticleDto;
import com.hstar.backend.dto.WriteArticleDto;
import com.hstar.backend.entity.Article;
import com.hstar.backend.service.ArticleService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/boards")
public class ArticleController {
    private final ArticleService articleService;


    @PostMapping("/{boardId}/articles")
    public ResponseEntity<ApiResponse<Article>> writeArticle(
            @Parameter(description = "ID of the board", required = true)
            @PathVariable("boardId") Long boardId,
            @RequestBody WriteArticleDto writeArticleDto) {
        Article article = articleService.writeArticle(boardId, writeArticleDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(article, "Article created successfully"));
    }

    @GetMapping("/{boardId}/articles")
    public ResponseEntity<ApiResponse<List<Article>>> getArticle(
            @Parameter(description = "ID of the board", required = true)
            @PathVariable("boardId") Long boardId,
            @RequestParam(value = "lastId", required = false) Long lastId,
            @RequestParam(value = "firstId", required = false) Long firstId) {
        // Parameter로 lastId, firstId 를 받는데, 해당 값을 기준으로 이전 이후 값을 가져올 수 있도록 하기 위해서 받는다.
        // .../articles?lastId=10

        List<Article> articles = new ArrayList<>();
        String message = "Articles Not Found!";

        if (lastId != null) {
            articles = articleService.getOldArticle(boardId, lastId);
            message = "Older articles retrieved successfully";
        }
        if (firstId != null) {
            articles = articleService.getNewArticle(boardId, firstId);
            message = "Newer articles retrieved successfully";
        }

        return ResponseEntity.ok(new ApiResponse<>(articles, message));
    }

    @PutMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<ApiResponse<Article>> editArticle(
            @Parameter(description = "ID of the board", required = true)
            @PathVariable("boardId") Long boardId,
            @Parameter(description = "ID of the article", required = true)
            @PathVariable("articleId") Long articleId,
            @RequestBody EditArticleDto editArticleDto) {

        Article editedArticle = articleService.editArticle(boardId, articleId, editArticleDto);
        return ResponseEntity.ok(new ApiResponse<>(editedArticle, "Article edited successfully"));

    }
}