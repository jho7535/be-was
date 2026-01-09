package webserver.view;

import java.io.File;

public class ViewResolver {
    private final String prefix = "static";
    private final String suffix = ".html";

    public View resolve(String viewName) {
        if (viewName.startsWith("/")) {
            viewName = viewName.substring(1);
        }

        String resourcePath = prefix + "/" + viewName + suffix;
        return new View(resourcePath);
    }
}