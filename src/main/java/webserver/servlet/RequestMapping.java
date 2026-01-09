package webserver.servlet;

import webserver.servlet.impl.UserCreateServlet;
import webserver.servlet.impl.UserLoginServlet;
import webserver.servlet.impl.UserLogoutServlet;

import java.util.HashMap;
import java.util.Map;

public class RequestMapping {
    private static final Map<String, HttpServlet> controllers = new HashMap<>();

    static {
        controllers.put("/user/create", new UserCreateServlet());
        controllers.put("/user/login", new UserLoginServlet());
        controllers.put("/user/logout", new UserLogoutServlet());
    }

    public static HttpServlet getServlet(String path) {
        return controllers.get(path);
    }
}