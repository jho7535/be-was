package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.servlet.HttpServlet;
import webserver.servlet.RequestMapping;
import webserver.servlet.impl.ResourceServlet;


public class DispatcherServlet {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);
    private final HttpServlet resourceServlet = new ResourceServlet();

    public HttpResponse dispatch(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        
        HttpServlet servlet = RequestMapping.getServlet(request.path());
        
        if (servlet == null) {
            servlet = resourceServlet;
        }

        try {
            servlet.service(request, response);
        } catch (Exception e) {
            logger.error("Error processing request: {}", request.path(), e);
            response.setStatus(500, "Internal Server Error");
            response.setBody("<h1>500 Internal Server Error</h1>".getBytes());
        }

        return response;
    }
}