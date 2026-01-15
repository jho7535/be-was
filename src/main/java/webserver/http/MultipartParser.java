package webserver.http;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MultipartParser {
    private final byte[] body;
    private final byte[] boundary;
    private final Map<String, String> parameters = new HashMap<>();
    private final Map<String, byte[]> files = new HashMap<>();
    private final Map<String, String> fileNames = new HashMap<>();

    public MultipartParser(byte[] body, String boundaryStr) {
        this.body = body;
        this.boundary = ("--" + boundaryStr).getBytes(StandardCharsets.UTF_8);
        parse();
    }

    private void parse() {
        int offset = 0;
        while (offset < body.length) {
            // 1. Boundary 위치 찾기
            int start = findIndex(body, boundary, offset);
            if (start == -1) break;

            // 2. 파트의 헤더와 데이터 경계(\r\n\r\n) 찾기
            offset = start + boundary.length;
            int headerEnd = findIndex(body, new byte[]{13, 10, 13, 10}, offset);
            if (headerEnd == -1) break;

            // 3. 파트 헤더 파싱 (Content-Disposition 등)
            String partHeader = new String(Arrays.copyOfRange(body, offset, headerEnd), StandardCharsets.UTF_8);
            String name = extractAttribute(partHeader, "name");
            String fileName = extractAttribute(partHeader, "filename");

            // 4. 파트 데이터 추출 (다음 Boundary 이전까지)
            int dataStart = headerEnd + 4;
            int dataEnd = findIndex(body, boundary, dataStart) - 2; // \r\n 제외
            if (dataEnd < 0) break;

            byte[] partData = Arrays.copyOfRange(body, dataStart, dataEnd);

            if (fileName != null) {
                files.put(name, partData);
                fileNames.put(name, fileName);
            } else {
                parameters.put(name, new String(partData, StandardCharsets.UTF_8));
            }
            offset = dataEnd;
        }
    }

    // 바이트 배열 내 패턴 검색 (핵심 알고리즘)
    private int findIndex(byte[] source, byte[] target, int start) {
        for (int i = start; i <= source.length - target.length; i++) {
            boolean match = true;
            for (int j = 0; j < target.length; j++) {
                if (source[i + j] != target[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    private String extractAttribute(String header, String attribute) {
        int idx = header.indexOf(attribute + "=\"");
        if (idx == -1) return null;
        int start = idx + attribute.length() + 2;
        int end = header.indexOf("\"", start);
        return header.substring(start, end);
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public byte[] getFile(String name) {
        return files.get(name);
    }

    public String getFileName(String name) {
        return fileNames.get(name);
    }
}