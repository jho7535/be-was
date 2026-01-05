package webserver.model;

import java.util.Map;

public record HttpRequest(
        HttpMethod method,
        String path,
        String version,
        Map<String, String> headers,
        Map<String, String> params
) {
    public HttpRequest {
        headers = Map.copyOf(headers);
        params = Map.copyOf(params);
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
}