package jbailey.stu.mmu.bigbook.rest.serialize.xml;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jbailey.stu.mmu.bigbook.rest.model.RestResponse;
import jbailey.stu.mmu.bigbook.rest.serialize.ResponseSerializer;

public class XmlSerializer<T> implements ResponseSerializer<T> {

    private static final XmlMapper MAPPER = new XmlMapper();

    @Override
    public String serialise(RestResponse<T> response) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialise to XML", e);
        }
    }

    @Override
    public <U> U deserialize(String data, Class<U> classType) {
        try {
            return MAPPER.readValue(data, classType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialise XML", e);
        }
    }
}
