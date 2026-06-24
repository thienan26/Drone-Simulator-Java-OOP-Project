package dronesim.api;

import dronesim.config.AppConfig;
import dronesim.exception.ApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Concrete implementation of {@link ApiClient} that communicates with the
 * Drone Simulation REST API over HTTPS using token-based authentication.
 * This is the only class that touches HttpURLConnection.
 */
public class DroneSimApiClient implements ApiClient {

    private static final Logger LOGGER = Logger.getLogger(DroneSimApiClient.class.getName());
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 15_000;

    private final AppConfig config;

    public DroneSimApiClient(AppConfig config) {
        this.config = config;
    }

    @Override
    public String get(String endpoint) throws ApiException {
        if (config.getToken() == null || config.getToken().isBlank()) {
            throw new ApiException("No API token configured. Please enter your token in Settings.");
        }

        String urlText = buildUrl(endpoint);
        LOGGER.info("GET " + urlText);

        try {
            URL url = URI.create(urlText).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Token " + config.getToken());
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "DroneSim-Java-OOP-Client/1.0");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);

            int responseCode = connection.getResponseCode();
            LOGGER.info("HTTP " + responseCode + " from " + urlText);

            if (responseCode == 401) {
                throw new ApiException("Authentication failed (HTTP 401). Check your token.", 401);
            }
            if (responseCode == 429) {
                throw new ApiException("Rate limit exceeded (HTTP 429). Wait before retrying.", 429);
            }
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new ApiException("API returned HTTP " + responseCode, responseCode);
            }

            return readResponse(connection);

        } catch (IOException e) {
            LOGGER.warning("Network error contacting API: " + e.getMessage());
            throw new ApiException("Could not connect to API at " + urlText
                    + ". Check URL and VPN connection.", e);
        }
    }

    @Override
    public PaginatedResult fetchPage(String endpoint, int page, int pageSize) throws ApiException {
        String separator = endpoint.contains("?") ? "&" : "?";
        String pagedEndpoint = endpoint + separator + "limit=" + pageSize + "&offset=" + ((page - 1) * pageSize);

        String json = get(pagedEndpoint);

        // Parse next/previous URLs and count from the JSON using simple string search.
        // The API returns: {"count":N,"next":"url or null","previous":"url or null","results":[...]}
        String nextUrl = extractStringField(json, "next");
        String previousUrl = extractStringField(json, "previous");
        int count = extractIntField(json, "count");

        return new PaginatedResult(json, nextUrl, previousUrl, count);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private String buildUrl(String endpoint) {
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        String base = config.getBaseUrl();
        if (base.endsWith("/") && endpoint.startsWith("/")) {
            return base + endpoint.substring(1);
        }
        if (!base.endsWith("/") && !endpoint.startsWith("/")) {
            return base + "/" + endpoint;
        }
        return base + endpoint;
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Extracts a JSON string field value by searching for {@code "key":"value"}.
     * Returns null when the field value is JSON null or missing.
     */
    private String extractStringField(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return null;

        int valStart = keyIdx + searchKey.length();
        // skip whitespace
        while (valStart < json.length() && json.charAt(valStart) == ' ') valStart++;

        if (valStart >= json.length()) return null;

        if (json.charAt(valStart) == 'n') {
            // JSON null
            return null;
        }

        if (json.charAt(valStart) == '"') {
            int valEnd = json.indexOf('"', valStart + 1);
            if (valEnd < 0) return null;
            return json.substring(valStart + 1, valEnd);
        }

        return null;
    }

    /**
     * Extracts a JSON integer field value by searching for {@code "key":digits}.
     * Returns 0 when missing.
     */
    private int extractIntField(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return 0;

        int valStart = keyIdx + searchKey.length();
        while (valStart < json.length() && json.charAt(valStart) == ' ') valStart++;

        int valEnd = valStart;
        while (valEnd < json.length() && Character.isDigit(json.charAt(valEnd))) valEnd++;

        if (valStart == valEnd) return 0;

        try {
            return Integer.parseInt(json.substring(valStart, valEnd));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
