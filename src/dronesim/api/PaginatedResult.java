package dronesim.api;

/**
 * Wraps a single page of JSON results returned by the drone simulation API,
 * along with the next/previous page URLs for navigation.
 */
public class PaginatedResult {

    private final String jsonBody;
    private final String nextUrl;
    private final String previousUrl;
    private final int count;

    public PaginatedResult(String jsonBody, String nextUrl, String previousUrl, int count) {
        this.jsonBody = jsonBody;
        this.nextUrl = nextUrl;
        this.previousUrl = previousUrl;
        this.count = count;
    }

    public String getJsonBody() { return jsonBody; }
    public String getNextUrl() { return nextUrl; }
    public String getPreviousUrl() { return previousUrl; }
    public int getCount() { return count; }

    /** Returns true when there is a subsequent page available. */
    public boolean hasNext() {
        return nextUrl != null && !nextUrl.equals("null") && !nextUrl.isBlank();
    }

    /** Returns true when there is a previous page available. */
    public boolean hasPrevious() {
        return previousUrl != null && !previousUrl.equals("null") && !previousUrl.isBlank();
    }
}
