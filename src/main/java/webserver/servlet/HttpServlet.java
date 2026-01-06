package webserver.servlet;

import webserver.model.HttpRequest;
import webserver.model.HttpResponse;

public abstract class HttpServlet {

    public void service(HttpRequest request, HttpResponse response) {
        switch (request.method()) {
            case GET -> doGet(request, response);
            case POST -> doPost(request, response);
            default -> {
                response.setStatus(405, "Method Not Allowed");
                response.setBody("<h1>405 Method Not Allowed</h1>".getBytes());
            }
        }
    }

    protected void doGet(HttpRequest request, HttpResponse response) {
        response.setStatus(405, "Method Not Allowed");
        response.setBody("<h1>405 Method Not Allowed</h1>".getBytes());
    }

    protected void doPost(HttpRequest request, HttpResponse response) {
        response.setStatus(405, "Method Not Allowed");
        response.setBody("<h1>405 Method Not Allowed</h1>".getBytes());
    }
}
