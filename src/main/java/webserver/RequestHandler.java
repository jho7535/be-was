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

            // 파일 객체 생성
            File file = new File("src/main/resources/static" + path);
            DataOutputStream dos = new DataOutputStream(out);

            if (file.exists() && !file.isDirectory()) {
                // 스트림으로 파일 읽기
                byte[] body = readAllBytes(file);

                response200Header(dos, body.length);
                responseBody(dos, body);
            } else {
                response404Header(dos);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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

    private byte[] readAllBytes(File file) throws IOException {
        int length = (int) file.length();
        byte[] body = new byte[length];

        try (FileInputStream fis = new FileInputStream(file)) {
            int offset = 0; // 현재 배열의 어디까지 채웠는지 나타내는 인덱스
            int numRead = 0; // 이번에 스트림에서 실제로 읽어온 바이트 수

            // 파일을 바이트 단위로 끝까지 읽어 배열에 채움
            while (offset < body.length && (numRead = fis.read(body, offset, body.length - offset)) >= 0) {
                offset += numRead;
            }
        }
        return body;
    }
}
