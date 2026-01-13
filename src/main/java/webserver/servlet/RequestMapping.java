package webserver.servlet;

import webserver.servlet.impl.*;

import java.util.HashMap;
import java.util.Map;

public class RequestMapping {
    private static final Map<String, HttpServlet> controllers = new HashMap<>();

    static {
        controllers.put("/", new HomeServlet());
        controllers.put("/index.html", new HomeServlet());

        controllers.put("/login", new LoginPageServlet());
        controllers.put("/registration", new RegistrationPageServlet());
        controllers.put("/article", new ArticlePageServlet());

        controllers.put("/user/create", new UserCreateServlet());
        controllers.put("/user/login", new UserLoginServlet());
        controllers.put("/user/logout", new UserLogoutServlet());

        controllers.put("/article/create", new ArticleCreateServlet());
    }

    public static HttpServlet getServlet(String path) {
        return controllers.get(path);
    }
}