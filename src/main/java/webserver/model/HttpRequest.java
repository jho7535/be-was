package webserver.model;

import java.util.Map;

public record HttpRequest(
        HttpMethod method,
        String path,
        String version,
        Map<String, String> headers,
        Map<String, String> params,
        Map<String, String> cookies,
        byte[] body
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

    public boolean isMultipart() {
        String contentType = getHeader("Content-Type");
        return contentType != null && contentType.startsWith("multipart/form-data");
    }

    public String getBoundary() {
        String contentType = headers.get("Content-Type");
        if (contentType == null || !contentType.contains("boundary=")) return null;
        return contentType.split("boundary=")[1].trim();
    }
}