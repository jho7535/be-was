package webserver.view;

import webserver.model.HttpResponse;
import webserver.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class View {
    // 정규표현식 패턴 상수화 (성능 및 가독성)
    private static final Pattern POSITIVE_SECTION = Pattern.compile("\\{\\{#(\\w+)}}(.*?)\\{\\{/\\1}}", Pattern.DOTALL);
    private static final Pattern NEGATIVE_SECTION = Pattern.compile("\\{\\{\\^(\\w+)}}(.*?)\\{\\{/\\1}}", Pattern.DOTALL);
    private static final Pattern VARIABLE = Pattern.compile("\\{\\{(?![#^/])(.*?)}}");

    private final String resourcePath;

    public View(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public void render(Map<String, Object> model, HttpResponse response) throws IOException {
        String template = new String(readResource(resourcePath), StandardCharsets.UTF_8);
        String rendered = processTemplate(template, model);

        response.addHeader("Content-Type", "text/html;charset=utf-8");
        response.setBody(rendered.getBytes(StandardCharsets.UTF_8));
    }

    private String processTemplate(String template, Map<String, Object> model) {
        String result = template;

        // 1. 조건부 섹션 처리 (Positive: # / Negative: ^)
        result = renderSections(result, POSITIVE_SECTION, model, true);
        result = renderSections(result, NEGATIVE_SECTION, model, false);

        // 2. 변수 치환 처리
        result = renderVariables(result, model);

        return result;
    }

    private String renderSections(String template, Pattern pattern, Map<String, Object> model, boolean isPositive) {
        return pattern.matcher(template).replaceAll(matchResult -> {
            String key = matchResult.group(1);
            String content = matchResult.group(2);

            Object value = model.get(key);
            boolean condition = isConditionMet(value);

            // Positive 세션이면 조건 충족 시 출력, Negative면 조건 미충족 시 출력
            return (isPositive == condition) ? Matcher.quoteReplacement(content) : "";
        });
    }

    private String renderVariables(String template, Map<String, Object> model) {
        return VARIABLE.matcher(template).replaceAll(matchResult -> {
            String key = matchResult.group(1);
            Object value = model.getOrDefault(key, "");
            return Matcher.quoteReplacement(String.valueOf(value));
        });
    }

    private boolean isConditionMet(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return true; // 값이 존재하면 true로 판단 (타임리프/머스테치 방식)
    }

    private byte[] readResource(String path) throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IOException("Resource not found: " + path);
            return IOUtils.readAllBytes(is);
        }
    }
}