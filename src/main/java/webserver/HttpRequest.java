package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private String method;
    private String path;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();

    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line = br.readLine();

        // 빈 요청인 경우 예외를 던져서 이후 로직이 실행되지 않게 함
        if (line == null || line.trim().isEmpty()) {
            throw new IOException("Empty HTTP Request Line");
        }

        // Request Line 파싱
        parseRequestLine(line);
        logger.debug("Method: {}, Path: {}", method, path);

        // 헤더 파싱
        while (true) {
            line = br.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }
            parseHeader(line);
            logger.debug("Header: {}", line);
        }

        // 쿼리 파라미터 로그
        if (!params.isEmpty()) {
            logger.debug("Query Parameters: {}", params);
        }
    }

    // 전체 요청 파싱 메서드
    private void parseRequestLine(String line) {
        String[] tokens = line.split(" ");
        if (tokens.length < 2) return; // 최소한 Method와 Path는 있어야 함

        this.method = tokens[0];
        this.path = tokens[1];

        // 쿼리 스트링 파싱
        if (path.contains("?")) {
            String[] parts = path.split("\\?");
            this.path = parts[0];
            parseQueryString(parts[1]);
        }
    }

    // 헤더 파싱 메서드
    private void parseHeader(String line) {
        String[] pair = line.split(": ");
        if (pair.length >= 2) {
            headers.put(pair[0], pair[1]);
        }
    }

    // 쿼리 스트링 파싱 메서드
    private void parseQueryString(String queryString) {
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] tokens = pair.split("=");
            if (tokens.length == 2) params.put(tokens[0], tokens[1]);
        }
    }

    // Getter
    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getParameter(String name) {
        return params.get(name);
    }
}