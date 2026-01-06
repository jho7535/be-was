package webserver.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.model.HttpResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ResponseWriter {
    private static final Logger logger = LoggerFactory.getLogger(ResponseWriter.class);

    public static void write(OutputStream out, HttpResponse response) {
        DataOutputStream dos = new DataOutputStream(out);
        try {
            writeStatusLine(dos, response);
            writeHeaders(dos, response);
            dos.writeBytes("\r\n"); // Header와 Body 사이의 빈 줄
            writeBody(dos, response);
            dos.flush();
        } catch (IOException e) {
            logger.error("Error writing response", e);
        }
    }

    private static void writeStatusLine(DataOutputStream dos, HttpResponse response) throws IOException {
        String statusLine = String.format("%s %d %s\r\n", 
                response.getVersion(), response.getStatusCode(), response.getStatusMessage());
        dos.writeBytes(statusLine);
    }

    private static void writeHeaders(DataOutputStream dos, HttpResponse response) throws IOException {
        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            dos.writeBytes(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }
    }

    private static void writeBody(DataOutputStream dos, HttpResponse response) throws IOException {
        if (response.getBody() != null && response.getBody().length > 0) {
            dos.write(response.getBody(), 0, response.getBody().length);
        }
    }
}
