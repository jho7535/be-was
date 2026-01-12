package webserver;

import java.io.*;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.handler.ResourceHandler;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.http.RequestParser;
import webserver.http.ResponseWriter;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private final DispatcherServlet dispatcherServlet = new DispatcherServlet();

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = RequestParser.parse(in);
            HttpResponse response = new HttpResponse();

            String path = request.path();

            if (isStaticResource(path)) {
                // 정적 리소스 핸들러가 단독으로 처리
                ResourceHandler resourceHandler = new ResourceHandler();
                resourceHandler.serve(request, response);
            } else {
                // 동적 요청(HTML, API 등)은 디스패처 서블릿이 처리
                dispatcherServlet.dispatch(request, response);
            }

            ResponseWriter.write(out, response);

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private boolean isStaticResource(String path) {
        // 모든 .html은 동적 페이지라고 하셨으므로 .html은 제외
        // 확장자가 있고, .html이 아닌 것들을 정적 리소스로 간주
        return path.contains(".") && !path.endsWith(".html");
    }
}
