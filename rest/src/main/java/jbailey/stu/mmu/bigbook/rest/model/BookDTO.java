package jbailey.stu.mmu.bigbook.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// todo: move this / explain in the write-up so I can delete it from here :)

/**
 * Data Transfer Object (DTO) for Book entities in the REST API.
 *
 * The DTO is used as a pattern to transfer data between layers and systems,
 * in the context of the REST service, it matches the Book object and serves to decouple the domain model from commons.
 * This prevents exposing internal model details (e.g., implementation-specific fields) which isn't necessarily important in this
 * application but in a more complex, enterprise level application it would allow API versioning or format changes without affecting the domain.
 *
 * It mirrors the Book domain model but can be customized for API needs,
 * such as excluding sensitive fields or adding computed properties where required. It allows API extensibility without changing the core model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private int id;
    private String title;
    private String author;
    private String date;
    private List<String> genres; // Strings for simplicity instead of a list of the object
    private List<String> characters;
    private String synopsis;
}
