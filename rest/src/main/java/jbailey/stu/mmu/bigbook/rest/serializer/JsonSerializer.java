package jbailey.stu.mmu.bigbook.rest.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jbailey.stu.mmu.bigbook.rest.model.RestResponse;

/**
 * JSON serializer using GSON.
 * 
 * GSON (Google's JSON serialization library) handles conversion between Java objects
 * and JSON strings. It's configured with pretty printing for readability.
 * 
 * @param <T> The data type being serialized.
 */
public class JsonSerializer<T> implements ResponseSerializer<T> {
    
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Serialize a RestResponse object to JSON string.
     * 
     * @param response The RestResponse to serialize.
     * @return JSON string representation.
     */
    @Override
    public String serialize(RestResponse<T> response) {
        return GSON.toJson(response);
    }

    /**
     * Deserialize a JSON string to a specific type.
     * 
     * @param jsonString The JSON string to deserialize.
     * @param clazz The target class type.
     * @return Object of type T.
     */
    @Override
    public <U> U deserialize(String jsonString, Class<U> clazz) {
        return GSON.fromJson(jsonString, clazz);
    }
}

