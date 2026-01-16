package webserver.handler;

import webserver.excepiton.CommonException;
import webserver.model.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public ModelAndView handle(Exception e) {
        ModelAndView mav = new ModelAndView("error"); // 아까 만든 HTML 경로

        if (e instanceof CommonException ce) {
            // 우리가 정의한 에러인 경우
            mav.addObject("status", ce.getStatus())
                    .addObject("errorTitle", ce.getTitle())
                    .addObject("errorMessage", ce.getMessage());

            logger.warn("CommonException [{}]: {}", ce.getStatus(), ce.getMessage());
        } else {
            // 예상치 못한 시스템 에러(NullPointer 등)인 경우
            mav.addObject("status", 500)
                    .addObject("errorTitle", "Internal Server Error")
                    .addObject("errorMessage", "서버 내부에서 처리 중 오류가 발생했습니다.");

            logger.error("Unexpected Error: ", e);
        }

        return mav;
    }
}