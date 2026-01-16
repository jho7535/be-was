package webserver.handler;

import webserver.excepiton.CommonException;
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

        // 파일이 없으면 404 예외 발생
        if (!file.exists() || !file.isFile()) {
            throw new CommonException(404, "이미지를 찾을 수 없습니다",
                    "요청하신 이미지(" + fileName + ")가 서버에 존재하지 않습니다.");
        }

        // FileInputStream을 사용하여 순수 IO로 읽기
        try (InputStream is = new FileInputStream(file)) {
            byte[] body = IOUtils.readAllBytes(is);
            response.ok(body, ContentType.from(path).getMimeType());
        } catch (Exception e) {
            throw new CommonException(500, "파일 읽기 오류", "서버에서 이미지 파일을 읽는 중 문제가 발생했습니다.");
        }
    }

    private void serveFromClasspath(String path, HttpResponse response) {
        String resourcePath = STATIC_BASE_PATH + path;
        InputStream is = getClass().getResourceAsStream(resourcePath);

        if (is == null) {
            throw new CommonException(404, "페이지를 찾을 수 없습니다",
                    "요청하신 경로(" + path + ")를 찾을 수 없습니다. 주소를 다시 확인해주세요.");
        }

        try (is) {
            byte[] body = IOUtils.readAllBytes(is);
            response.ok(body, ContentType.from(path).getMimeType());
        } catch (Exception e) {
            throw new CommonException(500, "서버 내부 오류", "정적 리소스를 처리하는 중 오류가 발생했습니다.");
        }
    }
}