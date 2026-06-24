package dronesim.service;

import dronesim.api.ApiClient;
import dronesim.api.PaginatedResult;
import dronesim.exception.ApiException;
import dronesim.model.DroneDynamics;
import dronesim.util.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches drone dynamics from /api/dronedynamics/.
 * Pagination is exposed explicitly so the GUI only requests the page currently on screen.
 */
public class DroneDynamicsService extends DataService<DroneDynamics> {

    private static final Logger LOGGER = Logger.getLogger(DroneDynamicsService.class.getName());
    private static final String ENDPOINT = "/api/dronedynamics/?format=json";

    private final ApiClient apiClient;

    public DroneDynamicsService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Returns the first page (20 records) of dynamics – used only for dashboard previews.
     * For the Flight Dynamics view use {@link #getPage} or {@link #getPageForDrone} instead.
     */
    @Override
    public List<DroneDynamics> getAll() throws ApiException {
        return getPage(1, 20);
    }

    /**
     * Fetches a single page of dynamics across all drones.
     *
     * @param page     1-based page index
     * @param pageSize number of records per page
     */
    public List<DroneDynamics> getPage(int page, int pageSize) throws ApiException {
        PaginatedResult result = apiClient.fetchPage(ENDPOINT, page, pageSize);
        return parseDynamics(result.getJsonBody());
    }

    /**
     * Fetches a single page of dynamics filtered to one specific drone.
     * This is the method called by the Flight Dynamics pagination buttons.
     *
     * @param droneId  the drone whose dynamics to load
     * @param page     1-based page index
     * @param pageSize number of records per page
     * @return parsed list and the PaginatedResult (for hasNext / hasPrevious)
     */
    public PagedDynamics getPageForDrone(int droneId, int page, int pageSize) throws ApiException {
        String endpoint = ENDPOINT + "&drone=" + droneId;
        PaginatedResult result = apiClient.fetchPage(endpoint, page, pageSize);
        List<DroneDynamics> items = parseDynamics(result.getJsonBody());
        LOGGER.info("Loaded dynamics page " + page + " for drone " + droneId
                + " (" + items.size() + " records)");
        return new PagedDynamics(items, result);
    }

    /**
     * Fetches the N most recent dynamics records for a drone.
     * Used by MetricsService to compute average speed and connection state.
     *
     * @param droneId the drone to query
     * @param count   maximum number of recent records to return
     * @return list of recent DroneDynamics, newest first; empty if none available
     */
    public List<DroneDynamics> getRecentForDrone(int droneId, int count) throws ApiException {
        String endpoint = ENDPOINT + "&drone=" + droneId;
        PaginatedResult result = apiClient.fetchPage(endpoint, 1, count);
        return parseDynamics(result.getJsonBody());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<DroneDynamics> parseDynamics(String json) {
        List<DroneDynamics> list = new ArrayList<>();
        String results = extractResultsArray(json);
        if (results == null || results.isEmpty()) return list;

        for (String objectJson : splitObjects(results)) {
            String droneUrl = extractString(objectJson, "drone");
            int droneId = JsonParser.idFromUrl(droneUrl);
            list.add(new DroneDynamics(
                    droneUrl,
                    droneId,
                    extractString(objectJson, "timestamp"),
                    extractInt(objectJson, "speed", 0),
                    extractDouble(objectJson, "align_roll", 0),
                    extractDouble(objectJson, "align_pitch", 0),
                    extractDouble(objectJson, "align_yaw", 0),
                    extractDouble(objectJson, "longitude", 0),
                    extractDouble(objectJson, "latitude", 0),
                    extractInt(objectJson, "battery_status", 0),
                    extractString(objectJson, "last_seen"),
                    extractString(objectJson, "status")
            ));
        }
        return list;
    }

    private String extractResultsArray(String json) {
        if (json == null) return null;
        int start = json.indexOf("\"results\"");
        if (start < 0) return null;
        start = json.indexOf('[', start);
        if (start < 0) return null;

        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return json.substring(start + 1, i);
            }
        }
        return null;
    }

    private List<String> splitObjects(String arrayJson) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < arrayJson.length(); i++) {
            char c = arrayJson.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) objects.add(arrayJson.substring(start, i + 1));
            }
        }
        return objects;
    }

    private String extractString(String json, String field) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*\\\"(.*?)\\\"").matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private int extractInt(String json, String field, int defaultValue) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*(-?\\d+)").matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : defaultValue;
    }

    private double extractDouble(String json, String field, double defaultValue) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").matcher(json);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : defaultValue;
    }

    // ── inner result carrier ─────────────────────────────────────────────────

    /** Carries a parsed dynamics page together with its pagination metadata. */
    public static final class PagedDynamics {
        public final List<DroneDynamics> items;
        public final PaginatedResult pagination;

        PagedDynamics(List<DroneDynamics> items, PaginatedResult pagination) {
            this.items = items;
            this.pagination = pagination;
        }
    }
}
