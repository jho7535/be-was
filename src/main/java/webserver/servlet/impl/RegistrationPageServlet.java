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
        return new ModelAndView("registration/index");
    }
}
