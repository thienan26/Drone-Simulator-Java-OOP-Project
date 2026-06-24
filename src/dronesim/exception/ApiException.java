package dronesim.exception;

/**
 * Custom exception for all API-related errors: wrong token, wrong URL,
 * non-200 HTTP responses, JSON parse failures, and rate-limit hits.
 */
public class ApiException extends Exception {

    private final int httpCode;

    public ApiException(String message) {
        super(message);
        this.httpCode = -1;
    }

    public ApiException(String message, int httpCode) {
        super(message);
        this.httpCode = httpCode;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpCode = -1;
    }

    /** Returns the HTTP status code that triggered this exception, or -1 if not applicable. */
    public int getHttpCode() {
        return httpCode;
    }
}
