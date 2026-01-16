package webserver.excepiton;

public class CommonException extends RuntimeException {
    private final int status;
    private final String title;

    public CommonException(int status, String title, String message) {
        super(message);
        this.status = status;
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }
}