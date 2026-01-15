package webserver.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.model.HttpMethod;
import webserver.model.HttpRequest;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {
    private static final Logger logger = LoggerFactory.getLogger(RequestParser.class);

    public static HttpRequest parse(InputStream in) throws IOException {
        // 1. 요청 라인 읽기 (바이트 단위 한 줄 읽기)
        String requestLineStr = readLine(in);
        if (requestLineStr == null || requestLineStr.isEmpty()) {
            throw new IOException("Empty HTTP Request Line");
        }
        RequestLine requestLine = parseRequestLine(requestLineStr);

        // 2. 헤더 읽기 (빈 줄이 나올 때까지 한 줄씩 읽기)
        Map<String, String> headers = new HashMap<>();
        String headerLine;
        while (!(headerLine = readLine(in)).isEmpty()) {
            String[] pair = headerLine.split(": ");
            if (pair.length >= 2) headers.put(pair[0], pair[1]);
        }

        // 3. 쿠키 파싱
        Map<String, String> cookies = parseCookies(headers.get("Cookie"));

        // 4. 바디 읽기 (Content-Length만큼의 바이너리 데이터)
        byte[] body = new byte[0];
        Map<String, String> params = new HashMap<>(requestLine.queryParams);

        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            // ★ 핵심: InputStream에서 정확히 길이만큼 바이트를 읽어옴
            body = in.readNBytes(contentLength);

            // 일반 Form 데이터일 경우만 파라미터 맵에 추가
            String contentType = headers.get("Content-Type");
            if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                params.putAll(parseQueryString(new String(body, StandardCharsets.UTF_8)));
            }
        }

        return new HttpRequest(requestLine.method, requestLine.path, requestLine.version, headers, params, cookies, body);
    }

    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') break; // \n을 만나면 종료
            if (b != '\r') baos.write(b); // \r은 제외하고 저장
        }
        if (b == -1 && baos.size() == 0) return null;
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static RequestLine parseRequestLine(String line) {
        String[] tokens = line.split(" ");
        HttpMethod method = HttpMethod.valueOf(tokens[0]);
        String fullPath = tokens[1];
        String version = (tokens.length > 2) ? tokens[2] : "HTTP/1.1";

        String path = fullPath;
        Map<String, String> queryParams = new HashMap<>();
        if (fullPath.contains("?")) {
            String[] parts = fullPath.split("\\?");
            path = parts[0];
            if (parts.length > 1) {
                queryParams = parseQueryString(parts[1]);
            }
        }
        return new RequestLine(method, path, version, queryParams);
    }

    private static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) return params;
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] tokens = pair.split("=");
            if (tokens.length == 2) params.put(tokens[0], tokens[1]);
        }
        return params;
    }

    private static Map<String, String> parseCookies(String cookieHeader) {
        Map<String, String> cookies = new HashMap<>();
        if (cookieHeader == null) return cookies;
        String[] pairs = cookieHeader.split(";");
        for (String pair : pairs) {
            String[] tokens = pair.trim().split("=");
            if (tokens.length == 2) cookies.put(tokens[0], tokens[1]);
        }
        return cookies;
    }

    private record RequestLine(HttpMethod method, String path, String version, Map<String, String> queryParams) {
    }
}