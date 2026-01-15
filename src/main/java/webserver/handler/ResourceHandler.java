package webserver.handler;

import webserver.model.ContentType;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ResourceHandler {
    private static final String STATIC_BASE_PATH = "/static";
    private static final String EXTERNAL_SAVE_PATH = System.getProperty("user.dir") + "/upload_images/";

    public void serve(HttpRequest request, HttpResponse response) {
        String path = request.path();

        // 1. /img/article/로 시작하는 요청은 외부 폴더(upload_images)에서 찾음
        if (path.startsWith("/img/article/")) {
            serveFromExternalFolder(path, response);
            return;
        }

        // 2. 그 외 요청은 클래스패스(/static)에서 찾음
        serveFromClasspath(path, response);
    }

    private void serveFromExternalFolder(String path, HttpResponse response) {
        String fileName = path.substring("/img/article/".length());
        File file = new File(EXTERNAL_SAVE_PATH + fileName);

        if (!file.exists() || !file.isFile()) {
            response.notFound();
            return;
        }

        // FileInputStream을 사용하여 순수 IO로 읽기
        try (InputStream is = new FileInputStream(file)) {
            byte[] body = IOUtils.readAllBytes(is);
            response.ok(body, ContentType.from(path).getMimeType());
        } catch (Exception e) {
            response.internalServerError();
        }
    }

    private void serveFromClasspath(String path, HttpResponse response) {
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