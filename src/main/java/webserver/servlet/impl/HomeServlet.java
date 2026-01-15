package webserver.servlet.impl;

import model.Article;
import model.User;
import repository.ArticleRepository;
import webserver.SessionManager;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.model.HttpSession;
import webserver.model.ModelAndView;
import webserver.servlet.HttpServlet;

import java.util.Optional;

public class HomeServlet extends HttpServlet {
    private final ArticleRepository articleRepository = ArticleRepository.getInstance();

    @Override
    protected ModelAndView doGet(HttpRequest request, HttpResponse response) {
        // 1. 사용자 로그인 상태 확인 및 기본 뷰 결정
        HttpSession session = SessionManager.getSession(request.getSessionId());
        User loginUser = (session != null) ? (User) session.getAttribute("user") : null;

        // 로그인 여부에 따라 index.html 또는 main/index.html 선택
        ModelAndView mav = new ModelAndView(loginUser != null ? "main/index" : "index");
        mav.addObject("isLoggedIn", loginUser != null);

        if (loginUser != null) {
            mav.addObject("userName", loginUser.getName());
        }

        // 2. 게시글 조회 로직 (ID 파라미터 유무에 따른 분기)
        String idParam = request.getParameter("id");
        Optional<Article> articleOpt;

        if (idParam == null || idParam.isEmpty()) {
            // 파라미터가 없으면 가장 최신글 조회
            articleOpt = articleRepository.findLatest();
        } else {
            // 특정 ID가 있으면 해당 게시글 조회
            articleOpt = articleRepository.findById(Long.parseLong(idParam));
        }

        // 3. 게시글 데이터 및 내비게이션 상태(이전/다음) 설정
        if (articleOpt.isPresent()) {
            Article article = articleOpt.get();
            mav.addObject("hasArticle", true)
                    .addObject("articleId", article.getId())
                    .addObject("writerId", article.getWriterId())
                    .addObject("articleContent", article.getContent())
                    .addObject("articleImage", article.getImagePath())
                    .addObject("likeCount", article.getLikeCount());

            // 이전 글과 다음 글 존재 여부 및 ID 조회
            Optional<Article> prev = articleRepository.findPrevious(article.getId());
            Optional<Article> next = articleRepository.findNext(article.getId());

            // 이전 글 설정
            if (prev.isPresent()) {
                mav.addObject("hasPrev", true)
                        .addObject("prevId", prev.get().getId());
            } else {
                mav.addObject("hasPrev", false);
            }

            // 다음 글 설정 (최신글인 경우 next가 없으므로 비활성화 조건이 됨)
            if (next.isPresent()) {
                mav.addObject("hasNext", true)
                        .addObject("nextId", next.get().getId());
            } else {
                mav.addObject("hasNext", false);
            }

            // 댓글 관련 데이터 (요구사항: 기본 3개 노출 로직 등 - 현재는 개수만 전달)
            // 실제 구현 시 CommentRepository 연동 필요
            int mockCommentCount = 5;
            mav.addObject("commentCount", mockCommentCount)
                    .addObject("showViewAllBtn", mockCommentCount > 3);

        } else {
            // 게시글이 하나도 없는 경우
            mav.addObject("hasArticle", false);
        }

        return mav;
    }
}