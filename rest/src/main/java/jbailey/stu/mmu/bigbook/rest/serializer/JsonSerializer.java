package jbailey.stu.mmu.bigbook.rest.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jbailey.stu.mmu.bigbook.rest.model.RestResponse;

/**
 * JSON serializer using GSON.
 * 
 * {@link Gson} (Google's JSON serialization library) handles conversion between Java objects
 * and JSON {@code String}s. It's configured with pretty printing for readability.
 * 
 * @param <T> The data type being serialized.
 */
public class JsonSerializer<T> implements ResponseSerializer<T> {
    
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Serialize a {@link RestResponse} object to JSON {@code String}.
     * 
     * @param response The {@link RestResponse} to serialize.
     * @return JSON {@code String} representation.
     */
    @Override
    public String serialise(RestResponse<T> response) {
        return GSON.toJson(response);
    }

    /**
     * Deserialize a JSON {@code String} to a specific type.
     * 
     * @param jsonString The JSON {@code String} to deserialize.
     * @param clazz The target class type.
     * @return Object of type {@code U}.
     */
    @Override
    public <U> U deserialize(String jsonString, Class<U> clazz) {
        return GSON.fromJson(jsonString, clazz);
    }
}
