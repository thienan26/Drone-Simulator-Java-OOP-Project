package dronesim.service;

import dronesim.exception.ApiException;

import java.util.List;

/**
 * Abstract base class for all data-fetching services.
 * Declares the abstract {@code getAll()} method, satisfying the OOP abstract-method requirement.
 *
 * @param <T> the model type this service returns
 */
public abstract class DataService<T> {

    /**
     * Fetches and returns the complete list of objects for this service's resource type.
     * Subclasses retrieve data from the API and parse it into Java model objects.
     *
     * @return list of parsed model objects
     * @throws ApiException on network or HTTP errors
     */
    public abstract List<T> getAll() throws ApiException;
}
