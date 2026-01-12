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
            response.notFound();
            return;
        }

        try (is) {
            byte[] body = IOUtils.readAllBytes(is);
            response.ok(body, ContentType.from(path).getMimeType());
        } catch (Exception e) {
            response.internalServerError();
        }
    }
}