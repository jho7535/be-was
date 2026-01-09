package webserver.servlet.impl;

import model.User;
import webserver.SessionManager;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.model.HttpSession;
import webserver.model.ModelAndView;
import webserver.servlet.HttpServlet;

public class HomeServlet extends HttpServlet {

    @Override
    protected ModelAndView doGet(HttpRequest request, HttpResponse response) {
        ModelAndView mav = new ModelAndView("index"); // index.html로 이동

        // 1. 요청의 쿠키/세션에서 사용자 정보 조회
        HttpSession session = SessionManager.getSession(request.getSessionId());

        User loginUser = null;
        if (session != null) {
            loginUser = (User) session.getAttribute("user");
        }

        if (loginUser != null) {
            // 2. 로그인된 경우: isLoggedIn을 true로, 사용자 이름을 모델에 담음
            mav.addObject("isLoggedIn", true)
                    .addObject("userName", loginUser.getName());
        } else {
            // 3. 로그인되지 않은 경우: isLoggedIn을 false로 설정
            mav.addObject("isLoggedIn", false);
        }

        return mav;
    }
}