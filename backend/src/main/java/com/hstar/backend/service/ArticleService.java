package com.hstar.backend.service;

import com.hstar.backend.dto.WriteArticleDto;
import com.hstar.backend.entity.Article;
import com.hstar.backend.entity.Board;
import com.hstar.backend.entity.User;
import com.hstar.backend.exception.ResourceNotFoundException;
import com.hstar.backend.repository.ArticleRepository;
import com.hstar.backend.repository.BoardRepository;
import com.hstar.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class ArticleService {
    private final BoardRepository boardRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;


    public Article writeArticle(WriteArticleDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Board> board = boardRepository.findById(dto.getBoardId());
        if (author.isEmpty()) {
            throw new ResourceNotFoundException("author not found");
        }
        if (board.isEmpty()) {
            throw new ResourceNotFoundException("board not found");
        }
        Article article = new Article();
        article.setBoard(board.get());
        article.setAuthor(author.get());
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        articleRepository.save(article);
        return article;
    }
}