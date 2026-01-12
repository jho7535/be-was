package webserver.handler;

import webserver.model.ContentType;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.util.IOUtils;
import java.io.InputStream;

public class ResourceHandler {
    private static final String STATIC_BASE_PATH = "/static";

    public void serve(HttpRequest request, HttpResponse response) {
        String path = request.path();
        String resourcePath = STATIC_BASE_PATH + path;

        InputStream is = getClass().getResourceAsStream(resourcePath);

        if (is == null) {
            response.setStatus(404, "Not Found");
            response.setBody("<h1>404 Not Found</h1>".getBytes());
            return;
        }

        try (is) {
            byte[] body = IOUtils.readAllBytes(is);
            response.setStatus(200, "OK");
            response.setContentType(ContentType.from(path).getMimeType());
            response.setBody(body);
        } catch (Exception e) {
            response.setStatus(500, "Internal Server Error");
        }
    }
}