package webserver.servlet;

import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.model.ModelAndView;

public abstract class HttpServlet {

    public ModelAndView service(HttpRequest request, HttpResponse response) {
        return switch (request.method()) {
            case GET -> doGet(request, response);
            case POST -> doPost(request, response);
            default -> {
                response.setStatus(405, "Method Not Allowed");
                response.setBody("<h1>405 Method Not Allowed</h1>".getBytes());
                yield null;
            }
        };
    }

    protected ModelAndView doGet(HttpRequest request, HttpResponse response) {
        response.setStatus(405, "Method Not Allowed");
        response.setBody("<h1>405 Method Not Allowed</h1>".getBytes());
        return null;
    }

    protected ModelAndView doPost(HttpRequest request, HttpResponse response) {
        response.setStatus(405, "Method Not Allowed");
        response.setBody("<h1>405 Method Not Allowed</h1>".getBytes());
        return null;
    }
}
