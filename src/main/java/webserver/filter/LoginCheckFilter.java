package webserver.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.SessionManager;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.model.HttpSession;

import java.util.Set;

public class LoginCheckFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoginCheckFilter.class);

    // 누구나 접근 가능한 화이트리스트
    private static final Set<String> whiteList = Set.of(
            "/", "/index.html", "/login", "/registration", "/user/create", "/user/login"
    );

    // 로그인한 사용자는 들어올 수 없는 경로
    private static final Set<String> guestOnlyList = Set.of(
            "/login", "/registration", "/user/login", "/user/create"
    );

    private static final Set<String> authRequiredList = Set.of(
            "/article", "/mypage", "/user/logout"
    );

    public boolean doFilter(HttpRequest request, HttpResponse response) {
        String path = request.path();
        HttpSession session = SessionManager.getSession(request.getSessionId());
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

        logger.debug("Filter processing - Path: {}, LoggedIn: {}", path, isLoggedIn);

        if (isResourceOrWhiteList(path)) {
            if (isLoggedIn && guestOnlyList.contains(path)) {
                logger.info("Logged in user redirected from guest-only page: {}", path);
                response.sendRedirect("/"); // HomeServlet이 /main/index로 안내함
                return false;
            }
            return true;
        }

        if (!isLoggedIn && isAuthRequired(path)) {
            logger.warn("Unauthorized access attempt to: {}", path);
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }

    private boolean isResourceOrWhiteList(String path) {
        return whiteList.contains(path) ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.startsWith("/img/") ||
                path.equals("/favicon.ico");
    }

    private boolean isAuthRequired(String path) {
        return authRequiredList.stream().anyMatch(path::startsWith);
    }
}