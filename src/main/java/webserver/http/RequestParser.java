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
        
        // 요청 라인 파싱
        String[] tokens = line.split(" ");
        if (tokens.length < 2) {
             throw new IOException("Invalid HTTP Request Line: " + line);
        }
        
        HttpMethod method = HttpMethod.valueOf(tokens[0]);
        String fullPath = tokens[1];
        String version = (tokens.length > 2) ? tokens[2] : "HTTP/1.1";

        String path = fullPath;
        Map<String, String> params = new HashMap<>();

        if (fullPath.contains("?")) {
            String[] parts = fullPath.split("\\?");
            path = parts[0];
            if (parts.length > 1) {
                params = parseQueryString(parts[1]);
            }
        }

        // Header Parsing
        Map<String, String> headers = new HashMap<>();
        while (true) {
            line = br.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }
            parseHeader(line, headers);
        }

        // Body Parsing
        if (headers.containsKey("Content-Length") || headers.containsKey("Transfer-Encoding")) {
            String contentLengthValue = headers.get("Content-Length");
            if (contentLengthValue != null) {
                int contentLength = Integer.parseInt(contentLengthValue);
                char[] bodyChars = new char[contentLength];
                br.read(bodyChars, 0, contentLength);
                String body = new String(bodyChars);
                
                if (!body.isEmpty()) {
                    params.putAll(parseQueryString(body));
                }
            } else {
                logger.warn("Transfer-Encoding detected without Content-Length. Chunked encoding not yet fully supported.");
            }
        }

        logger.debug("Method: {}, Path: {}, Version: {}", method, path, version);
        if (!params.isEmpty()) {
            logger.debug("Query Parameters: {}", params);
        }
        headers.forEach((key, value) -> logger.debug("Header: {}: {}", key, value));

        return new HttpRequest(method, path, version, headers, params);
    }

    private static void parseHeader(String line, Map<String, String> headers) {
        String[] pair = line.split(": ");
        if (pair.length >= 2) {
            headers.put(pair[0], pair[1]);
        }
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
}
