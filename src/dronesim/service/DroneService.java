package dronesim.service;

import dronesim.api.ApiClient;
import dronesim.exception.ApiException;
import dronesim.model.Drone;
import dronesim.util.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Fetches and parses all individual drones from /api/drones/.
 */
public class DroneService extends DataService<Drone> {

    private static final Logger LOGGER = Logger.getLogger(DroneService.class.getName());
    private static final String ENDPOINT = "/api/drones/?format=json";
    private static final int PAGE_SIZE = 100;

    private final ApiClient apiClient;

    public DroneService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Returns all individual drones by walking through every available page.
     */
    @Override
    public List<Drone> getAll() throws ApiException {
        List<Drone> result = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            var paged = apiClient.fetchPage(ENDPOINT, page, PAGE_SIZE);
            JSONArray items = JsonParser.getResults(paged.getJsonBody());
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    result.add(parseDrone(items.getJSONObject(i)));
                }
            }
            hasMore = paged.hasNext();
            page++;
        }

        LOGGER.info("Loaded " + result.size() + " drones");
        return result;
    }

    private Drone parseDrone(JSONObject obj) {
        String droneTypeUrl = JsonParser.getString(obj, "dronetype");
        int droneTypeId = JsonParser.idFromUrl(droneTypeUrl);
        return new Drone(
                obj.optInt("id", -1),
                droneTypeUrl,
                droneTypeId,
                JsonParser.getString(obj, "serialnumber"),
                obj.optInt("carriage_weight", 0),
                JsonParser.getString(obj, "carriage_type")
        );
    }
}
