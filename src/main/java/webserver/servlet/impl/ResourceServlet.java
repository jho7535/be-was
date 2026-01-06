package webserver.servlet.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.model.ContentType;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.servlet.HttpServlet;
import webserver.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class ResourceServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ResourceServlet.class);

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) {
        String path = request.path();

        if (path == null) path = "";
        path = path.trim();

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
                    try {
                        is.close();
                    } catch (IOException ignored) {
                    }
                }
                is = welcomeIs;
                path = welcomePath;
            }
        }

        if (is != null) {
            try (InputStream ignored = is) {
                byte[] body = IOUtils.readAllBytes(is);
                response.setStatus(200, "OK");
                response.setContentType(ContentType.from(path).getMimeType());
                response.setBody(body);
            } catch (IOException e) {
                logger.error("Error reading static resource", e);
                response.setStatus(500, "Internal Server Error");
            }
        } else {
            response.setStatus(404, "Not Found");
            response.setBody("<h1>404 Not Found</h1>".getBytes());
        }
    }
}
