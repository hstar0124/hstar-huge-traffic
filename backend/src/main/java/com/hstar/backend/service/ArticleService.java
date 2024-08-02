package com.hstar.backend.service;

import com.hstar.backend.dto.EditArticleDto;
import com.hstar.backend.dto.WriteArticleDto;
import com.hstar.backend.entity.Article;
import com.hstar.backend.entity.Board;
import com.hstar.backend.entity.User;
import com.hstar.backend.exception.ForbiddenException;
import com.hstar.backend.exception.RateLimitException;
import com.hstar.backend.exception.ResourceNotFoundException;
import com.hstar.backend.repository.ArticleRepository;
import com.hstar.backend.repository.BoardRepository;
import com.hstar.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ArticleService {
    private static final int RATE_LIMIT_MINUTES = 1;

    private final BoardRepository boardRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;


    public Article writeArticle(Long boardId, WriteArticleDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (!this.isCanWriteArticle()) {
            throw new RateLimitException("article not written by rate limit");
        }

        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Board> board = boardRepository.findById(boardId);
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

    public List<Article> firstGetArticle(Long boardId) {
        return articleRepository.findTop10ByBoardIdOrderByCreatedDateDesc(boardId);
    }

    public List<Article> getOldArticle(Long boardId, Long articleId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdLessThanOrderByCreatedDateDesc(boardId, articleId);
    }

    public List<Article> getNewArticle(Long boardId, Long articleId) {
        return articleRepository.findTop10ByBoardIdAndArticleIdGreaterThanOrderByCreatedDateDesc(boardId, articleId);
    }



    public Article editArticle(Long boardId, Long articleId, EditArticleDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Board> board = boardRepository.findById(boardId);
        if (author.isEmpty()) {
            throw new ResourceNotFoundException("author not found");
        }
        if (board.isEmpty()) {
            throw new ResourceNotFoundException("board not found");
        }
        Optional<Article> article = articleRepository.findById(articleId);
        if (article.isEmpty()) {
            throw new ResourceNotFoundException("article not found");
        }
        if (article.get().getAuthor() != author.get()) {
            throw new ForbiddenException("article author different");
        }
        if (!this.isCanEditArticle()) {
            throw new RateLimitException("article not edited by rate limit");
        }
        if (dto.getTitle().isPresent()) {
            article.get().setTitle(dto.getTitle().get());
        }
        if (dto.getContent().isPresent()) {
            article.get().setContent(dto.getContent().get());
        }
        articleRepository.save(article.get());
        return article.get();
    }

    public boolean deleteArticle(Long boardId, Long articleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Board> board = boardRepository.findById(boardId);
        if (author.isEmpty()) {
            throw new ResourceNotFoundException("author not found");
        }
        if (board.isEmpty()) {
            throw new ResourceNotFoundException("board not found");
        }
        Optional<Article> article = articleRepository.findById(articleId);
        if (article.isEmpty()) {
            throw new ResourceNotFoundException("article not found");
        }
        if (article.get().getAuthor() != author.get()) {
            throw new ForbiddenException("article author different");
        }
        if (!this.isCanEditArticle()) {
            throw new RateLimitException("article not edited by rate limit");
        }
        
        // Soft Delete 방식으로 삭제
        // Flag 방식이 편리하지만, 휴면 에러로 노출이 될 가능성도 있다.(매번 delete flag를 체크해야함)
        // 테이블을 따로 두게 되는경우 트랜젝션 처리가 필요해야함으로 더 복잡해질 수 있음
        article.get().setIsDeleted(true);
        articleRepository.save(article.get());
        return true;
    }


    private boolean isCanWriteArticle() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Article latestArticle = articleRepository.findLatestArticleByAuthorUsernameOrderByCreatedDate(userDetails.getUsername());

        if (latestArticle == null) {
            // 사용자가 아직 글을 작성한 적이 없는 경우
            return true; // 첫 번째 글 작성은 항상 허용
        }

        return this.isDifferenceMoreThanFiveMinutes(latestArticle.getCreatedDate());
    }

    private boolean isCanEditArticle() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Article latestArticle = articleRepository.findLatestArticleByAuthorUsernameOrderByUpdatedDate(userDetails.getUsername());

        if (latestArticle == null) {
            return true; // 사용자가 아직 글을 수정한 적이 없는 경우, 수정 허용
        }

        return this.isDifferenceMoreThanFiveMinutes(latestArticle.getUpdatedDate());
    }

    public boolean isDifferenceMoreThanFiveMinutes(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return true; // null인 경우 시간 제한 없이 허용
        }

        LocalDateTime dateAsLocalDateTime = new Date().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Duration duration = Duration.between(localDateTime, dateAsLocalDateTime);

        return Math.abs(duration.toMinutes()) > RATE_LIMIT_MINUTES;
    }
}