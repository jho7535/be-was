package webserver.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.model.HttpMethod;
import webserver.model.HttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {
    private static final Logger logger = LoggerFactory.getLogger(RequestParser.class);

    public static HttpRequest parse(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line = br.readLine();

        if (line == null || line.trim().isEmpty()) {
            throw new IOException("Empty HTTP Request Line");
        }

        logger.debug("Request Line: {}", line);

        // 1. 요청 라인 파싱
        RequestLine requestLine = parseRequestLine(line);

        // 2. 헤더 파싱
        Map<String, String> headers = parseHeaders(br);

        // 3. 쿠키 파싱
        Map<String, String> cookies = parseCookies(headers.get("Cookie"));

        // 4. 바디 파싱 및 파라미터 병합
        Map<String, String> params = new HashMap<>(requestLine.queryParams);
        params.putAll(parseBody(br, headers));

        logger.debug("Method: {}, Path: {}, Version: {}", requestLine.method, requestLine.path, requestLine.version);
        if (!params.isEmpty()) {
            logger.debug("Query Parameters: {}", params);
        }
        headers.forEach((key, value) -> logger.debug("Header: {}: {}", key, value));

        return new HttpRequest(requestLine.method, requestLine.path, requestLine.version, headers, params, cookies);
    }

    private static RequestLine parseRequestLine(String line) throws IOException {
        String[] tokens = line.split(" ");
        if (tokens.length < 2) {
            throw new IOException("Invalid HTTP Request Line: " + line);
        }

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

    private static Map<String, String> parseHeaders(BufferedReader br) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while (true) {
            line = br.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }
            String[] pair = line.split(": ");
            if (pair.length >= 2) {
                headers.put(pair[0], pair[1]);
            }
        }
        return headers;
    }

    private static Map<String, String> parseBody(BufferedReader br, Map<String, String> headers) throws IOException {
        Map<String, String> bodyParams = new HashMap<>();
        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            char[] bodyChars = new char[contentLength];
            br.read(bodyChars, 0, contentLength);
            String body = new String(bodyChars);

            if (!body.isEmpty()) {
                bodyParams = parseQueryString(body);
            }
        } else if (headers.containsKey("Transfer-Encoding")) {
            logger.warn("Transfer-Encoding detected without Content-Length. Chunked encoding not yet fully supported.");
        }
        return bodyParams;
    }

    private static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] tokens = pair.split("=");
            if (tokens.length == 2) {
                params.put(tokens[0], tokens[1]);
            }
        }
        return params;
    }

    private static Map<String, String> parseCookies(String cookieHeader) {
        Map<String, String> cookies = new HashMap<>();
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return cookies;
        }
        String[] pairs = cookieHeader.split(";");
        for (String pair : pairs) {
            String[] tokens = pair.trim().split("=");
            if (tokens.length == 2) {
                cookies.put(tokens[0], tokens[1]);
            }
        }
        return cookies;
    }

    // 내부 데이터 전달용 객체
    private record RequestLine(HttpMethod method, String path, String version, Map<String, String> queryParams) {}
}