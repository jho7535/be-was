package model;

import java.time.LocalDateTime;

public class Article {
    private Long id;
    private String writerId;
    private String content;
    private String imagePath;
    private int likeCount;      // 좋아요 수
    private int commentCount;   // 댓글 개수 (조인 또는 서브쿼리용)
    private LocalDateTime createdAt;

    // 새 글 작성을 위한 생성자
    public Article(String writerId, String content, String imagePath) {
        this.writerId = writerId;
        this.content = content;
        this.imagePath = imagePath;
    }

    // DB 조회 데이터를 담기 위한 생성자
    public Article(Long id, String writerId, String content, String imagePath, int likeCount, int commentCount, LocalDateTime createdAt) {
        this.id = id;
        this.writerId = writerId;
        this.content = content;
        this.imagePath = imagePath;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
    }

    // Getter
    public Long getId() {
        return id;
    }

    public String getWriterId() {
        return writerId;
    }

    public String getContent() {
        return content;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}