package webserver.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpSession {
    private final String id;
    private final long createdAt;
    private final Map<String, Object> attributes = new HashMap<>();

    public HttpSession() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
    }

    // 세션에 데이터 저장 (로그인 유저 정보 등)
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    // 세션에서 데이터 조회
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    // 세션 데이터 삭제
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    // 세션 ID 반환
    public String getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}