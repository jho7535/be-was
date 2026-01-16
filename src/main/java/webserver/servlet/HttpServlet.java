package webserver.servlet;

import webserver.excepiton.CommonException;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.model.ModelAndView;

public abstract class HttpServlet {

    public ModelAndView service(HttpRequest request, HttpResponse response) {
        return switch (request.method()) {
            case GET -> doGet(request, response);
            case POST -> doPost(request, response);
            default -> throw new CommonException(405, "Method Not Allowed",
                    request.method() + " 방식의 요청은 지원하지 않습니다.");
        };
    }

    protected ModelAndView doGet(HttpRequest request, HttpResponse response) {
        throw new CommonException(405, "Method Not Allowed",
                "이 경로는 GET 요청을 지원하지 않습니다.");
    }

    protected ModelAndView doPost(HttpRequest request, HttpResponse response) {
        throw new CommonException(405, "Method Not Allowed",
                "이 경로는 POST 요청을 지원하지 않습니다.");
    }
}
