package webserver.model;

import java.util.Map;

public record HttpRequest(
        HttpMethod method,
        String path,
        String version,
        Map<String, String> headers,
        Map<String, String> params,
        Map<String, String> cookies
) {
    public HttpRequest {
        headers = Map.copyOf(headers);
        params = Map.copyOf(params);
        cookies = (cookies != null) ? Map.copyOf(cookies) : Map.of();
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getParameter(String name) {
        return params.get(name);
    }

    public boolean isFileRequest() {
        if (path == null) return false;
        int lastSlashIndex = path.lastIndexOf('/');
        int lastDotIndex = path.lastIndexOf('.');
        return lastDotIndex > lastSlashIndex;
    }

    public String getSessionId() {
        return cookies.get("sid");
    }

    public String getCookie(String name) {
        return cookies.get(name);
    }
}