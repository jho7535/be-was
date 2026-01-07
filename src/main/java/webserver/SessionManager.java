package webserver;

import webserver.model.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    // 멀티스레드 환경에서 안전한 Map 사용
    private static final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

    // 세션 생성 및 저장
    public static HttpSession createSession() {
        HttpSession session = new HttpSession();
        sessions.put(session.getId(), session);
        return session;
    }

    // 세션 조회
    public static HttpSession getSession(String sessionId) {
        if (sessionId == null) return null;
        return sessions.get(sessionId);
    }

    // 세션 삭제 (로그아웃 시 호출)
    public static void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }
}