package webserver.servlet.impl;

import model.Article;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.ArticleRepository;
import webserver.SessionManager;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.model.HttpSession;
import webserver.model.ModelAndView;
import webserver.servlet.HttpServlet;

public class ArticleCreateServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ArticleCreateServlet.class);
    private final ArticleRepository articleRepository = ArticleRepository.getInstance();

    @Override
    protected ModelAndView doPost(HttpRequest request, HttpResponse response) {
        // 1. 세션을 통해 로그인 여부 확인
        HttpSession session = SessionManager.getSession(request.getSessionId());
        User loginUser = (session != null) ? (User) session.getAttribute("user") : null;

        // 2. 로그인하지 않은 경우 로그인 페이지로 이동
        if (loginUser == null) {
            logger.warn("Unauthorized attempt to write article.");
            return new ModelAndView("redirect:/login");
        }

        // 3. 파라미터에서 글 내용 추출
        String content = request.getParameter("content");

        // 4. Article 객체 생성 및 저장
        // imagePath는 null 또는 빈 값으로 처리
        Article article = new Article(loginUser.getUserId(), content, null);
        articleRepository.save(article);

        logger.info("Article created by user {}: {}", loginUser.getUserId(), content);

        // 5. 작성 완료 후 메인 페이지로 이동
        return new ModelAndView("redirect:/");
    }
}