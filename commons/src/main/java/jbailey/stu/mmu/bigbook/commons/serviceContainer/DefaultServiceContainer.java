package jbailey.stu.mmu.bigbook.commons.serviceContainer;

import jbailey.stu.mmu.bigbook.commons.dao.BookDAO;
import jbailey.stu.mmu.bigbook.commons.dao.query.JdbcQueryAdapter;
import jbailey.stu.mmu.bigbook.commons.service.BookService;

/**
 * Default implementation of {@link ServiceContainer}.
 * Wires all dependencies using JDBC adapters. This implementation
 * exists in the commons module to provide concrete infrastructure instantiation.
 * Different ServiceContainer implementations can be made to implement differnet DAOs with different query adapters if necessary.
 * (e.g., TestServiceContainer with in-memory adapters).
 */
public class DefaultServiceContainer implements ServiceContainer {

    private final BookService bookService;

    /**
     * Default Service
     */
    public DefaultServiceContainer() {
        BookDAO bookDAO = new BookDAO(new JdbcQueryAdapter());
        this.bookService = new BookService(bookDAO);
    }

    @Override
    public BookService getBookService() {
        return bookService;
    }
}

