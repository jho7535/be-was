package repository;

import db.ConnectionManager;
import model.Article;

import java.sql.*;
import java.util.Optional;

public class ArticleRepository {
    private static final ArticleRepository instance = new ArticleRepository();

    private ArticleRepository() {
    }

    public static ArticleRepository getInstance() {
        return instance;
    }

    /**
     * 게시글 저장
     */
    public void save(Article article) {
        String sql = "INSERT INTO articles (writerId, content, imagePath, likeCount) VALUES (?, ?, ?, 0)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, article.getWriterId());
            pstmt.setString(2, article.getContent());
            pstmt.setString(3, article.getImagePath());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("게시글 저장 실패", e);
        }
    }

    /**
     * 최신 게시글 하나를 조회 (메인 페이지용)
     */
    public Optional<Article> findLatest() {
        String sql = "SELECT * FROM articles ORDER BY id DESC LIMIT 1";
        return findOne(sql);
    }

    /**
     * 특정 ID의 게시글 조회
     */
    public Optional<Article> findById(Long id) {
        String sql = "SELECT * FROM articles WHERE id = " + id;
        return findOne(sql);
    }

    /**
     * 이전 글 조회 (현재 ID보다 작은 ID 중 가장 큰 것)
     */
    public Optional<Article> findPrevious(Long currentId) {
        String sql = "SELECT * FROM articles WHERE id < ? ORDER BY id DESC LIMIT 1";
        return findOne(sql, currentId);
    }

    /**
     * 다음 글 조회 (현재 ID보다 큰 ID 중 가장 작은 것)
     */
    public Optional<Article> findNext(Long currentId) {
        String sql = "SELECT * FROM articles WHERE id > ? ORDER BY id ASC LIMIT 1";
        return findOne(sql, currentId);
    }

    /**
     * 좋아요 카운트 증가 (+1)
     */
    public void increaseLikeCount(Long id) {
        String sql = "UPDATE articles SET likeCount = likeCount + 1 WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("좋아요 업데이트 실패", e);
        }
    }

    // 공통 조회 로직 분리
    private Optional<Article> findOne(String sql, Object... params) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToArticle(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("데이터 조회 오류", e);
        }
        return Optional.empty();
    }

    private Article mapToArticle(ResultSet rs) throws SQLException {
        return new Article(
                rs.getLong("id"),
                rs.getString("writerId"),
                rs.getString("content"),
                rs.getString("imagePath"),
                rs.getInt("likeCount"),
                0, // 댓글 수는 나중에 CommentRepository 연동 후 처리
                rs.getTimestamp("createdAt").toLocalDateTime()
        );
    }
}