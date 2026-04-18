package jbailey.stu.mmu.bigbook.rest.serializer;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jbailey.stu.mmu.bigbook.rest.model.RestResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * XML serializer using JAXB (Jakarta XML Binding).
 * 
 * JAXB is Java's standard for XML binding, providing automatic marshalling
 * (serialization) and unmarshalling (deserialization) of Java objects to/from XML.
 * 
 * @param <T> The data type being serialized.
 */
@Slf4j
public class XmlSerializer<T> implements ResponseSerializer<T> {

    /**
     * Serialize a RestResponse object to XML string.
     * 
     * Uses JAXB to convert the Java object to XML format with proper indentation.
     * 
     * @param response The RestResponse to serialize.
     * @return XML string representation.
     */
    @Override
    public String serialise(RestResponse<T> response) {
        try {
            // Create a JAXB context for the RestResponse class.
            JAXBContext context = JAXBContext.newInstance(RestResponse.class);
            
            // Create a marshaller to convert Java object to XML.
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            // Write XML to string.
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(response, stringWriter);
            
            return stringWriter.toString();
        } catch (JAXBException jaxbException) {
            log.error("Error serialising to XML", jaxbException);
            throw new RuntimeException("Failed to serialise to XML", jaxbException);
        }
    }

    /**
     * Deserialize an XML string to a specific type.
     * 
     * @param xmlString The XML string to deserialize.
     * @param clazz The target class type.
     * @return Object of type U.
     */
    @Override
    public <U> U deserialize(String xmlString, Class<U> clazz) {
        try {
            // Create a JAXB context for the target class.
            JAXBContext context = JAXBContext.newInstance(clazz);
            
            // Create an unmarshaller to convert XML to Java object.
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            // Parse XML from string.
            StringReader stringReader = new StringReader(xmlString);
            return clazz.cast(unmarshaller.unmarshal(stringReader));
        } catch (JAXBException jaxbException) {
            log.error("Error deserializing XML", jaxbException);
            throw new RuntimeException("Failed to deserialize XML", jaxbException);
        }
    }
}

