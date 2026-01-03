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
            // HttpRequest 파서를 통해 요청 분석
            HttpRequest request = new HttpRequest(in);
            String path = request.getPath();

            // 루트 리다이렉트
            if (path.equals("/")) {
                path = "/index.html";
            }

            DataOutputStream dos = new DataOutputStream(out);

            // 핵심 수정: File 객체 대신 ClassLoader를 통해 리소스를 스트림으로 가져옴
            String resourcePath = "/static" + path;
            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {

                if (is != null) {
                    // InputStream에서 바이트 배열을 읽어옴
                    byte[] body = readAllBytesFromStream(is);

                    String contentType = getContentType(path);
                    response200Header(dos, body.length, contentType);
                    responseBody(dos, body);
                } else {
                    response404Header(dos);
                }
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
}
