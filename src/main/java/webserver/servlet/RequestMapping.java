package webserver.servlet;

import webserver.servlet.impl.UserCreateServlet;

import java.util.HashMap;
import java.util.Map;

public class RequestMapping {
    private static final Map<String, HttpServlet> controllers = new HashMap<>();

    static {
        controllers.put("/user/create", new UserCreateServlet());
    }

    public static HttpServlet getServlet(String path) {
        return controllers.get(path);
    }
}