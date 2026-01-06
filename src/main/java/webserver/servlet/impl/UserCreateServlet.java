package webserver.servlet.impl;

import db.Database;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.servlet.HttpServlet;

public class UserCreateServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserCreateServlet.class);

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) {
        User user = new User(
                request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email")
        );
        Database.addUser(user);
        logger.debug("User Created : {}", user);

        response.sendRedirect("/index.html");
    }
}
