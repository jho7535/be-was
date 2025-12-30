package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String path;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();

    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line = br.readLine();

        if (line == null) return;

        // Request Line 파싱
        parseRequestLine(line);

        // Header 파싱
        while (!((line = br.readLine()).getBytes().length == 0) || line.isEmpty()) {
            if (line.isEmpty()) break;
            parseHeader(line);
        }
    }

    // 전체 요청 파싱 메서드
    private void parseRequestLine(String line) {
        String[] tokens = line.split(" ");
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
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public String getHeader(String name) { return headers.get(name); }
    public String getParameter(String name) { return params.get(name); }
}