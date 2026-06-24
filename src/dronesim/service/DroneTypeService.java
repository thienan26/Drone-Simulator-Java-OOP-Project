package dronesim.service;

import dronesim.api.ApiClient;
import dronesim.exception.ApiException;
import dronesim.model.DroneType;
import dronesim.util.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Fetches and parses all drone types (models) from /api/dronetypes/.
 * Follows pagination automatically because type data is small and static.
 */
public class DroneTypeService extends DataService<DroneType> {

    private static final Logger LOGGER = Logger.getLogger(DroneTypeService.class.getName());
    private static final String ENDPOINT = "/api/dronetypes/?format=json";
    private static final int PAGE_SIZE = 100;

    private final ApiClient apiClient;

    public DroneTypeService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Returns all drone types by walking through every available page.
     */
    @Override
    public List<DroneType> getAll() throws ApiException {
        List<DroneType> result = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            var paged = apiClient.fetchPage(ENDPOINT, page, PAGE_SIZE);
            JSONArray items = JsonParser.getResults(paged.getJsonBody());
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    result.add(parseDroneType(items.getJSONObject(i)));
                }
            }
            hasMore = paged.hasNext();
            page++;
        }

        LOGGER.info("Loaded " + result.size() + " drone types");
        return result;
    }

    private DroneType parseDroneType(JSONObject obj) {
        return new DroneType(
                obj.optInt("id", -1),
                JsonParser.getString(obj, "manufacturer"),
                JsonParser.getString(obj, "typename"),
                obj.optInt("weight", 0),
                obj.optInt("max_speed", 0),
                obj.optInt("max_carriage", 0),
                obj.optInt("max_carriage", 0),
                obj.optInt("battery_capacity", 0),
                obj.optInt("control_range", 0)
        );
    }
}
