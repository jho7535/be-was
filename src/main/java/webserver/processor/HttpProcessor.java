package webserver.processor;

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

        if (path.isEmpty() || path.equals("/")) {
            path = "/index.html";
        }

        // 1. Static Resource Handling
        String resourcePath = "/static" + path;
        InputStream is = getClass().getResourceAsStream(resourcePath);

        // 2. Welcome File Handling (if not found or directory request)
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
