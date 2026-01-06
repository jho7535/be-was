package webserver;

import java.io.*;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.http.RequestParser;
import webserver.http.ResponseWriter;
import webserver.processor.HttpProcessor;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private final HttpProcessor processor = new HttpProcessor();

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = RequestParser.parse(in);
            
            HttpResponse response = processor.process(request);

            ResponseWriter.write(out, response);

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
