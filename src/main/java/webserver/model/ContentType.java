package webserver.model;

import java.util.Arrays;

public enum ContentType {
    CSS("text/css", ".css"),
    JS("application/javascript", ".js"),
    ICO("image/x-icon", ".ico"),
    PNG("image/png", ".png"),
    JPG("image/jpeg", ".jpg", ".jpeg"),
    SVG("image/svg+xml", ".svg"),
    HTML("text/html", ".html");

    private final String mimeType;
    private final String[] extensions;

    ContentType(String mimeType, String... extensions) {
        this.mimeType = mimeType;
        this.extensions = extensions;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static ContentType from(String path) {
        String lowerPath = path.toLowerCase();
        return Arrays.stream(values())
                .filter(type -> Arrays.stream(type.extensions).anyMatch(lowerPath::endsWith))
                .findFirst()
                .orElse(HTML); // 기본값은 HTML
    }
}
