package webserver.http;

import webserver.excepiton.CommonException;

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
        if (boundaryStr == null || boundaryStr.isEmpty()) {
            throw new CommonException(400, "잘못된 요청", "Multipart boundary가 없습니다.");
        }
        this.body = body;
        this.boundary = ("--" + boundaryStr).getBytes(StandardCharsets.UTF_8);
        parse();
    }

    private void parse() {
        int offset = 0;
        boolean foundAtLeastOnePart = false;

        while (offset < body.length) {
            // 1. Boundary 위치 찾기
            int start = findIndex(body, boundary, offset);
            if (start == -1) {
                // 더 이상 바운더리가 없는데 한 번도 파싱을 못했다면 에러
                if (!foundAtLeastOnePart) {
                    throw new CommonException(400, "잘못된 요청", "멀티파트 데이터를 찾을 수 없습니다.");
                }
                break;
            }

            // 마지막 바운더리 체크 (바운더리 뒤에 --가 붙음)
            if (start + boundary.length + 2 <= body.length) {
                if (body[start + boundary.length] == '-' && body[start + boundary.length + 1] == '-') {
                    break; // 정상 종료
                }
            }

            // 2. 파트의 헤더와 데이터 경계(\r\n\r\n) 찾기
            offset = start + boundary.length;
            int headerEnd = findIndex(body, new byte[]{13, 10, 13, 10}, offset);
            if (headerEnd == -1) {
                throw new CommonException(400, "잘못된 요청", "멀티파트 헤더 형식이 올바르지 않습니다.");
            }

            // 3. 파트 헤더 파싱
            String partHeader = new String(Arrays.copyOfRange(body, offset, headerEnd), StandardCharsets.UTF_8);
            String name = extractAttribute(partHeader, "name");
            if (name == null) {
                throw new CommonException(400, "잘못된 요청", "멀티파트 파트의 name 속성이 없습니다.");
            }
            String fileName = extractAttribute(partHeader, "filename");

            // 4. 파트 데이터 추출
            int dataStart = headerEnd + 4;
            int nextBoundaryStart = findIndex(body, boundary, dataStart);
            if (nextBoundaryStart == -1) {
                throw new CommonException(400, "잘못된 요청", "멀티파트 데이터가 비정상적으로 종료되었습니다.");
            }

            int dataEnd = nextBoundaryStart - 2; // \r\n 제외
            byte[] partData = Arrays.copyOfRange(body, dataStart, dataEnd);

            if (fileName != null) {
                files.put(name, partData);
                fileNames.put(name, fileName);
            } else {
                parameters.put(name, new String(partData, StandardCharsets.UTF_8));
            }

            foundAtLeastOnePart = true;
            offset = nextBoundaryStart;
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