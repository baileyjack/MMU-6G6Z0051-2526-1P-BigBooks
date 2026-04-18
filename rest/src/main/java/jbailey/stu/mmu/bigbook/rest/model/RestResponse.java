package jbailey.stu.mmu.bigbook.rest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Standard REST API response wrapper.
 * 
 * All REST API responses follow this structure:
 * {@code {
 *   "status": 200,
 *   "message": "Success",
 *   "data": { ... }
 * }}
 * 
 * This ensures consistent response format across all endpoints and makes client-side
 * parsing predictable and straightforward.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlRootElement(name = "RestResponse")
public class RestResponse<T> {
    private int status;
    private String message;
    private T data;
}
