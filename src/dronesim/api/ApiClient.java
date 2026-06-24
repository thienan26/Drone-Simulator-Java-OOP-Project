package dronesim.api;

import dronesim.exception.ApiException;

/**
 * Interface for all HTTP communication with the drone simulation API.
 * Abstracts away transport details so services never touch HttpURLConnection directly.
 */
public interface ApiClient {

    /**
     * Performs an HTTP GET to the given endpoint and returns the raw response body.
     *
     * @param endpoint relative path (e.g. "/api/drones/?format=json") or absolute URL
     * @return raw JSON string
     * @throws ApiException on non-200 response, network failure, or missing token
     */
    String get(String endpoint) throws ApiException;

    /**
     * Fetches a single page of results from a paginated endpoint.
     *
     * @param endpoint  relative path for the collection (e.g. "/api/dronedynamics/?format=json")
     * @param page      1-based page number
     * @param pageSize  number of records per page
     * @return a {@link PaginatedResult} containing the JSON body and next/previous URLs
     * @throws ApiException on network or HTTP errors
     */
    PaginatedResult fetchPage(String endpoint, int page, int pageSize) throws ApiException;
}
