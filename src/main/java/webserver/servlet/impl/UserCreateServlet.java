package webserver.servlet.impl;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.UserRepository;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.model.ModelAndView;
import webserver.servlet.HttpServlet;

public class UserCreateServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserCreateServlet.class);

    @Override
    protected ModelAndView doPost(HttpRequest request, HttpResponse response) {
        User user = new User(
                request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email")
        );
        UserRepository.getInstance().save(user);
        logger.debug("User Created : {}", user);

        return new ModelAndView("redirect:/");
    }
}
