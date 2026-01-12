package webserver.servlet.impl;

import model.User;
import webserver.SessionManager;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.model.HttpSession;
import webserver.model.ModelAndView;
import webserver.servlet.HttpServlet;

public class RegistrationPageServlet extends HttpServlet {

    @Override
    protected ModelAndView doGet(HttpRequest request, HttpResponse response) {
        ModelAndView mav = new ModelAndView("registration/index");

        HttpSession session = SessionManager.getSession(request.getSessionId());
        User loginUser = null;
        if (session != null) {
            loginUser = (User) session.getAttribute("user");
        }

        if (loginUser != null) {
            mav.addObject("isLoggedIn", true)
                    .addObject("userName", loginUser.getName());
        } else {
            mav.addObject("isLoggedIn", false);
        }

        return mav;
    }
}
