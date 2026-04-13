package jbailey.stu.mmu.bigbook.commons.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a single full book entity. Contains a list of Genre and Character Objects
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private int id;
    private String title;
    private String author;
    private String date;
    private List<Genre> genres;
    private List<Character> characters;
    private String synopsis;
}
