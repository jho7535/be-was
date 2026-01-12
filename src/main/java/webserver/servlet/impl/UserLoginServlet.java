package webserver.servlet.impl;

import db.Database;
import model.User;
import webserver.SessionManager;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.model.HttpSession;
import webserver.model.ModelAndView;
import webserver.servlet.HttpServlet;

public class UserLoginServlet extends HttpServlet {

    @Override
    protected ModelAndView doPost(HttpRequest request, HttpResponse response) {
        // 파라미터에서 로그인 정보 추출
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");

        // 메모리 DB(DataBase.addUser로 저장된 데이터)에서 유저 조회
        User user = Database.findUserById(userId);

        // 유저가 존재하고 비밀번호가 일치하는지 확인
        if (user != null && user.getPassword().equals(password)) {
            // 로그인이 성공하면 세션을 생성하고 유저 정보를 저장
            HttpSession session = SessionManager.createSession();
            session.setAttribute("user", user);

            // 응답 헤더에 SID(세션 ID)를 쿠키로 설정하고 모든 경로에서 접근 가능하도록 Path=/ 추가
            response.addHeader("Set-Cookie", "sid=" + session.getId() + "; Path=/");

            // 로그인 성공 시 index.html로 이동
            return new ModelAndView("redirect:/index.html");
        }

        // 로그인이 실패하면 다시 로그인 화면으로 이동
        return new ModelAndView("redirect:/login/index.html");
    }
}