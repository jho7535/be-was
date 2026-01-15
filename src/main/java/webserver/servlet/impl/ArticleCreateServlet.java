package webserver.servlet.impl;

import model.Article;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.ArticleRepository;
import webserver.SessionManager;
import webserver.http.MultipartParser;
import webserver.model.HttpRequest;
import webserver.model.HttpResponse;
import webserver.model.HttpSession;
import webserver.model.ModelAndView;
import webserver.servlet.HttpServlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ArticleCreateServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ArticleCreateServlet.class);
    private final ArticleRepository articleRepository = ArticleRepository.getInstance();

    private static final String SAVE_PATH = System.getProperty("user.dir") + "/upload_images/";

    @Override
    protected ModelAndView doPost(HttpRequest request, HttpResponse response) {
        // 1. 세션에서 로그인 유저 확인
        HttpSession session = SessionManager.getSession(request.getSessionId());
        if (session == null || session.getAttribute("user") == null) {
            return new ModelAndView("redirect:/login");
        }
        User loginUser = (User) session.getAttribute("user");

        String content;
        String imagePath = null;

        // 2. 멀티파트 데이터인 경우 처리
        if (request.isMultipart()) {
            MultipartParser parser = new MultipartParser(request.body(), request.getBoundary());

            // 텍스트 내용 추출
            content = parser.getParameter("content");

            // 이미지 파일 추출 및 저장
            byte[] imageBytes = parser.getFile("imageFile");
            String originalFileName = parser.getFileName("imageFile");

            if (imageBytes != null && imageBytes.length > 0) {
                imagePath = saveImage(imageBytes, originalFileName);
            }
        } else {
            // 일반 Form 요청인 경우 (Fallback)
            content = request.getParameter("content");
        }

        // 3. Article 객체 생성 및 저장
        if (content != null && !content.isEmpty()) {
            Article article = new Article(loginUser.getUserId(), content, imagePath);
            articleRepository.save(article);
            logger.info("Article created by user {}: [Image: {}]", loginUser.getUserId(), imagePath);
        }

        // 4. 작성 완료 후 메인 페이지로 이동
        return new ModelAndView("redirect:/");
    }

    private String saveImage(byte[] imageBytes, String originalFileName) {
        File directory = new File(SAVE_PATH);
        if (!directory.exists()) directory.mkdirs();

        // 확장자 추출 안전하게 수정
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } else {
            extension = ".jpg"; // 기본값
        }

        String savedFileName = UUID.randomUUID() + extension;
        File file = new File(directory, savedFileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(imageBytes);
            // DB에 저장할 경로: ResourceHandler는 "/static"을 붙여서 찾으므로
            // "/img/article/..." 만 저장하면 됩니다.
            return "/img/article/" + savedFileName;
        } catch (IOException e) {
            logger.error("Failed to save image file", e);
            return null;
        }
    }
}