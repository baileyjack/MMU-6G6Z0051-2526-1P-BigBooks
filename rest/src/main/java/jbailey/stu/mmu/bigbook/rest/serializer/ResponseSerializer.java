package jbailey.stu.mmu.bigbook.rest.serializer;

import jbailey.stu.mmu.bigbook.rest.model.RestResponse;

/**
 * Interface for response serializers.
 * 
 * Defines a contract for serializing and deserializing {@link RestResponse} objects
 * to different formats (JSON, XML, TEXT). This allows the REST controller
 * to be format-agnostic and support multiple output formats.
 * 
 * @param <T> The data type being serialized.
 */
public interface ResponseSerializer<T> {
    
    /**
     * Serialize a {@link RestResponse} to a {@code String} in a specific format.
     * 
     * @param response The {@link RestResponse} to serialize.
     * @return A {@code String} representation in the target format.
     */
    String serialise(RestResponse<T> response);
    
    /**
     * Deserialize a {@code String} to an object of the specified type.
     * 
     * @param data The {@code String} data to deserialize.
     * @param clazz The target class type.
     * @return A generic object of type {@code U}.
     */
    <U> U deserialize(String data, Class<U> clazz);
}
