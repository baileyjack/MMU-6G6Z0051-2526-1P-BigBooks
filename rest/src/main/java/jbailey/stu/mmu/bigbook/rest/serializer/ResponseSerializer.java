package jbailey.stu.mmu.bigbook.rest.serializer;

import jbailey.stu.mmu.bigbook.rest.model.RestResponse;

/**
 * Interface for response serializers.
 * 
 * Defines a contract for serializing and deserializing RestResponse objects
 * to different formats (JSON, XML, TEXT). This allows the REST controller
 * to be format-agnostic and support multiple output formats.
 * 
 * @param <T> The data type being serialized.
 */
public interface ResponseSerializer<T> {
    
    /**
     * Serialize a RestResponse to a string in a specific format.
     * 
     * @param response The RestResponse to serialize.
     * @return A string representation in the target format.
     */
    String serialize(RestResponse<T> response);
    
    /**
     * Deserialize a string to an object of the specified type.
     * 
     * @param data The string data to deserialize.
     * @param clazz The target class type.
     * @return An object of type U.
     */
    <U> U deserialize(String data, Class<U> clazz);
}

