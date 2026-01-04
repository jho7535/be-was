package webserver.processor;

import db.Database;
import model.User;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HttpProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HttpProcessor.class);

    public HttpResponse process(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        String path = request.path();

        if (path == null) path = "";
        path = path.trim();

        // 0. 동적 요청 처리 (회원가입)
        if (path.startsWith("/user/create")) {
            User user = new User(
                    request.getParameter("userId"),
                    request.getParameter("password"),
                    request.getParameter("name"),
                    request.getParameter("email")
            );
            Database.addUser(user);
            logger.debug("User Created : {}", user);
            
            response.sendRedirect("/index.html");
            return response;
        }

        if (path.isEmpty() || path.equals("/")) {
            path = "/index.html";
        }

        // 1. 정적 리소스 처리
        String resourcePath = "/static" + path;
        InputStream is = getClass().getResourceAsStream(resourcePath);

        // 2. 웰컴 파일 처리 (파일을 찾지 못했거나 디렉토리 요청인 경우)
        if (is == null || !request.isFileRequest()) {
            String welcomePath = path.endsWith("/") ? path + "index.html" : path + "/index.html";
            InputStream welcomeIs = getClass().getResourceAsStream("/static" + welcomePath);
            if (welcomeIs != null) {
                if (is != null) {
                    try { is.close(); } catch (IOException ignored) {}
                }
                is = welcomeIs;
                path = welcomePath;
            }
        }

        if (is != null) {
            try (InputStream ignored = is) {
                byte[] body = readAllBytes(is);
                response.setStatus(200, "OK");
                response.setContentType(getContentType(path));
                response.setBody(body);
            } catch (IOException e) {
                logger.error("Error reading static resource", e);
                response.setStatus(500, "Internal Server Error");
            }
        } else {
            response.setStatus(404, "Not Found");
            response.setBody("<h1>404 Not Found</h1>".getBytes());
        }

        return response;
    }

    private byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private String getContentType(String path) {
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".ico")) return "image/x-icon";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";
        return "text/html";
    }
}
