package jbailey.stu.mmu.bigbook.commons.model.modelbuilder;

import jbailey.stu.mmu.bigbook.commons.dao.BookRowMapper;
import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.model.Character;
import jbailey.stu.mmu.bigbook.commons.model.Genre;
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder pattern for constructing Book objects with their (currently) 7 fields.
 * The class is used by {@link BookRowMapper} to map {@link ResultSet} rows, {@link BookService} assembles books from form data.
 * This code can be used anywhere a {@link Book} needs to be constructed.
 */
@Setter
@Accessors(fluent = true, chain = true) // builder methods need to return 'this' (i.e. the object itself...) - This is a cleaner way that having an individual getter for every attribute of an object.
public class BookBuilder {

    private int id;
    private String title;
    private String author;
    private String date;
    private List<Genre> genres = new ArrayList<>();
    private List<Character> characters = new ArrayList<>();
    private String synopsis;
    /**
     * Constructs a {@link Book} from the builder values.
     * @return a new {@link Book} instance.
     */
    public Book build() {
        return new Book(id, title, author, date, genres, characters, synopsis);
    }
}


