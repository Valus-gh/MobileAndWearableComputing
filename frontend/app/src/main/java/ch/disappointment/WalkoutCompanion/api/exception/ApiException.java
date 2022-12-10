package ch.disappointment.WalkoutCompanion.api.exception;

public class ApiException extends RuntimeException {
    private int status;

    public ApiException(String message, int status, Throwable cause){
        super(message, cause);
        this.status = status;
    }

    public ApiException(String message, int status){
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
