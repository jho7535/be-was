package webserver.servlet.impl;

import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.model.ModelAndView;
import webserver.servlet.HttpServlet;

public class LoginPageServlet extends HttpServlet {

    @Override
    protected ModelAndView doGet(HttpRequest request, HttpResponse response) {
        return new ModelAndView("login/index");
    }
}
