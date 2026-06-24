package dronesim.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Thin wrapper around the org.json library providing convenience accessors
 * for the paginated response envelope used by the drone simulation API.
 */
public final class JsonParser {

    private JsonParser() { }

    /**
     * Parses a raw JSON string into a {@link JSONObject}.
     *
     * @param json raw JSON text
     * @return parsed object
     */
    public static JSONObject parseObject(String json) {
        return new JSONObject(json);
    }

    /**
     * Returns the "results" array from a paginated API envelope.
     *
     * @param json raw paginated response
     * @return the results array
     */
    public static JSONArray getResults(String json) {
        JSONObject root = new JSONObject(json);
        return root.optJSONArray("results");
    }

    /**
     * Safely reads a string field, returning an empty string when absent or null.
     */
    public static String getString(JSONObject obj, String key) {
        if (obj.isNull(key)) return "";
        return obj.optString(key, "");
    }

    /**
     * Extracts the numeric ID from a URL like "https://host/api/drones/3/?format=json".
     * Returns -1 when parsing fails.
     */
    public static int idFromUrl(String url) {
        if (url == null || url.isBlank()) return -1;
        String trimmed = url.endsWith("/") ? url.substring(0, url.lastIndexOf('/')) : url;
        int slashIdx = trimmed.lastIndexOf('/');
        if (slashIdx < 0) return -1;
        try {
            return Integer.parseInt(trimmed.substring(slashIdx + 1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
