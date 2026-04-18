package jbailey.stu.mmu.bigbook.commons.service;

import jbailey.stu.mmu.bigbook.commons.dao.BookDAO;
import jbailey.stu.mmu.bigbook.commons.dao.query.JdbcQueryAdapter;

/**
 * Default implementation of {@link ServicePort}.
 * Acts as a container for the {@link BookService}. This allows the {@link ServicePort} to be used
 * in manual instantiation (in the MVC version) and framework-managed DI (Spring in REST).
 * Different {@link ServicePort} implementations can be made to implement different DAOs
 * with different query adapters if necessary. (e.g., {@code TestServicePort} with in-memory BD).
 */
public class DefaultServicePort implements ServicePort {

    private final BookService bookService;

    /**
     * Constructor that accepts a {@link BookService} instance for DI.
     * @param bookService is the {@link BookService} instance to be provided by this port
     */
    public DefaultServicePort(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public BookService getBookService() {
        return bookService;
    }
}
