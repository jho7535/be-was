package webserver;

import java.io.*;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = new HttpRequest(in);

            // HttpRequest에서 가져온 직후 바로 trim() 적용
            String path = request.getPath();
            if (path != null) {
                path = path.trim();
            }

            DataOutputStream dos = new DataOutputStream(out);

            // 경로가 "/" 로 시작하지 않는 경우 처리
            if (path == null || path.isEmpty()) path = "/";

            // 1순위: 요청한 경로 그대로 찾아보기 (파일인 경우)
            String resourcePath = "/static" + path;
            InputStream is = getClass().getResourceAsStream(resourcePath);

            // 2순위: 1순위에서 못 찾았거나, 디렉토리 요청인 경우 (Welcome File 탐색)
            if (is == null || !isFileRequest(path)) {
                String welcomePath = path.endsWith("/") ? path + "index.html" : path + "/index.html";
                InputStream welcomeIs = getClass().getResourceAsStream("/static" + welcomePath);

                if (welcomeIs != null) {
                    // 기존 열려있던 is가 있다면 닫고 교체 (is가 null이 아닐 수도 있으므로)
                    if (is != null) is.close();
                    is = welcomeIs;
                    path = welcomePath; // Content-Type 결정을 위해 path 업데이트
                }
            }

            // 최종 결과 응답
            if (is != null) {
                try (InputStream ignored = is) { // 자동 자원 반납
                    byte[] body = readAllBytesFromStream(is);
                    String contentType = getContentType(path);
                    response200Header(dos, body.length, contentType);
                    responseBody(dos, body);
                }
            } else {
                response404Header(dos);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int length, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            // 전달받은 contentType을 설정
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + length + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response404Header(DataOutputStream dos) {
        try {
            byte[] body = "<h1>404 Not Found</h1>".getBytes();
            dos.writeBytes("HTTP/1.1 404 Not Found \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + body.length + "\r\n");
            dos.writeBytes("\r\n");
            dos.write(body);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private byte[] readAllBytesFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096]; // 4KB 버퍼
        int nRead;

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }

    // 확장자별 MIME 타입을 반환하는 메서드
    private String getContentType(String path) {
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".ico")) return "image/x-icon";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg")) return "image/svg+xml";

        // 기본값은 html
        return "text/html";
    }

    // 파일 요청인지 확인하는 유틸리티 (톰캣의 DefaultServlet 판단 로직 모방)
    private boolean isFileRequest(String path) {
        int lastSlashIndex = path.lastIndexOf('/');
        int lastDotIndex = path.lastIndexOf('.');
        return lastDotIndex > lastSlashIndex;
    }
}
