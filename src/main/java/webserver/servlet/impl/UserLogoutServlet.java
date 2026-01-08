package webserver.servlet.impl;

import webserver.SessionManager;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.model.ModelAndView;
import webserver.servlet.HttpServlet;

public class UserLogoutServlet extends HttpServlet {

    @Override
    protected ModelAndView doPost(HttpRequest request, HttpResponse response) {
        // 요청 헤더의 쿠키에서 세션 ID(sid)를 추출
        String sessionId = request.getSessionId();

        if (sessionId != null) {
            // 서버 메모리(SessionManager)에서 세션 객체 삭제
            SessionManager.removeSession(sessionId);
        }

        // 브라우저의 쿠키를 만료시키기 위해 Max-Age를 0으로 설정하여 응답
        // Path를 생성 시와 동일하게 /로 맞춰야 정확히 삭제됨
        response.addHeader("Set-Cookie", "sid=" + sessionId + "; Path=/; Max-Age=0");

        // 로그아웃 후 메인 페이지로 리다이렉트
        response.sendRedirect("/index.html");
        
        return null;
    }
}