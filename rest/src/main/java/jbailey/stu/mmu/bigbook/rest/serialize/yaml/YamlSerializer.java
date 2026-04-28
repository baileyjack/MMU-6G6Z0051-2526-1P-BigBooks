package jbailey.stu.mmu.bigbook.rest.serialize.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jbailey.stu.mmu.bigbook.rest.model.RestResponse;
import jbailey.stu.mmu.bigbook.rest.serialize.ResponseSerializer;

public class YamlSerializer<T> implements ResponseSerializer<T> {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    @Override
    public String serialise(RestResponse<T> response) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialise YAML", e);
        }
    }

    @Override
    public <U> U deserialize(String data, Class<U> classType) {
        try {
            return MAPPER.readValue(data, classType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialise YAML", e);
        }
    }
}
