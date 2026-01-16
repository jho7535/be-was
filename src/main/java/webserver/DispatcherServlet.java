package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.excepiton.CommonException;
import webserver.filter.LoginCheckFilter;
import webserver.handler.GlobalExceptionHandler;
import webserver.handler.ResourceHandler;
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
    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    public void dispatch(HttpRequest request, HttpResponse response) {
        try {
            // 1. 정적 리소스 처리
            if (isStaticResource(request.path())) {
                ResourceHandler resourceHandler = new ResourceHandler();
                resourceHandler.serve(request, response);
                return; // 정적 리소스 응답 완료
            }

            // 2. 로그인 필터 (동적 요청에만 적용하고 싶다면 여기서 수행)
            if (!loginFilter.doFilter(request, response)) {
                return;
            }

            // 3. 서블릿 찾기 및 실행
            HttpServlet servlet = RequestMapping.getServlet(request.path());
            if (servlet == null) {
                throw new CommonException(404, "Not Found", "요청하신 페이지를 찾을 수 없습니다.");
            }

            ModelAndView mav = servlet.service(request, response);
            processResponse(mav, response);

        } catch (Exception e) {
            logger.error("Exception caught in Dispatcher: {}", e.getMessage());
            ModelAndView errorMav = exceptionHandler.handle(e);

            // 에러 발생 시 HTTP 상태 코드도 익셉션에 맞춰 설정
            if (e instanceof CommonException ce) {
                response.setStatus(ce.getStatus(), ce.getMessage());
            } else {
                response.setStatus(500, "서버 내부 에러");
            }

            try {
                processResponse(errorMav, response);
            } catch (Exception ex) {
                logger.error("Fatal: Error page rendering failed", ex);
            }
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

    private boolean isStaticResource(String path) {
        return path.contains(".") && !path.endsWith(".html");
    }
}