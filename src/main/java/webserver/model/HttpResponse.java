package webserver.model;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private String version = "HTTP/1.1";
    private int statusCode = 200;
    private String statusMessage = "OK";
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public HttpResponse() {
        // 기본 헤더 설정
        headers.put("Content-Type", "text/html;charset=utf-8");
    }

    // 상태 설정을 위한 편의 메서드
    public void setStatus(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    // 리다이렉트 설정
    public void sendRedirect(String url) {
        this.statusCode = 302;
        this.statusMessage = "Found";
        headers.put("Location", url);
    }

    public void setBody(byte[] body) {
        this.body = body;
        this.headers.put("Content-Length", String.valueOf(body.length));
    }

    public void setContentType(String contentType) {
        this.headers.put("Content-Type", contentType + ";charset=utf-8");
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public String getVersion() {
        return version;
    }

    public void sendError(int statusCode, String message) {
        this.setStatus(statusCode, message);
        this.setContentType("text/html;charset=utf-8");

        String errorPage = String.format(
                "<html><body><h1>%d %s</h1><p>에러 발생</p></body></html>",
                statusCode, message
        );
        this.setBody(errorPage.getBytes());
    }

    public void ok(byte[] body, String contentType) {
        this.setStatus(200, "OK");
        this.setContentType(contentType);
        this.setBody(body);
    }

    public void notFound() {
        sendError(404, "Not Found");
    }

    public void internalServerError() {
        sendError(500, "Internal Server Error");
    }
}