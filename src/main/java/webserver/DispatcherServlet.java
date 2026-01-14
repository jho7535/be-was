package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.filter.LoginCheckFilter;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.model.ModelAndView;
import webserver.servlet.HttpServlet;
import webserver.servlet.RequestMapping;
import webserver.view.View;
import webserver.view.ViewResolver;


public class DispatcherServlet {
    private static final Logger logger = LoggerFactory.getLogger(DispatcherServlet.class);
    private final ViewResolver viewResolver = new ViewResolver();
    private final LoginCheckFilter loginFilter = new LoginCheckFilter();

    public void dispatch(HttpRequest request, HttpResponse response) {
        if (!loginFilter.doFilter(request, response)) {
            return;
        }

        HttpServlet servlet = RequestMapping.getServlet(request.path());

        if (servlet == null) {
            logger.warn("No mapping found for path: {}", request.path());
            response.notFound();
            return;
        }

        try {
            ModelAndView mav = servlet.service(request, response);
            processResponse(mav, response);
        } catch (Exception e) {
            logger.error("Error processing request: {}", request.path(), e);
            response.internalServerError();
        }
    }

    private void processResponse(ModelAndView mav, HttpResponse response) throws Exception {
        // 케이스 1: ModelAndView가 없는 경우
        if (mav == null) {
            return;
        }

        String viewName = mav.getViewName();

        // 케이스 2: 리다이렉트 처리
        if (viewName.startsWith("redirect:")) {
            response.sendRedirect(viewName.substring("redirect:".length()));
            return;
        }

        // 케이스 3: 동적 뷰 렌더링 (ViewResolver 호출)
        View view = viewResolver.resolve(viewName);

        // View에게 렌더링 위임
        if (view != null) {
            view.render(mav.getModel(), response);
        }
    }
}