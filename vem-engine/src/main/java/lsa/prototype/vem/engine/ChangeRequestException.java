package lsa.prototype.vem.engine;

public class ChangeRequestException extends RuntimeException {
    public ChangeRequestException() {
    }

    public ChangeRequestException(String message) {
        super(message);
    }

    public ChangeRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChangeRequestException(Throwable cause) {
        super(cause);
    }

    public ChangeRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
