package jbailey.stu.mmu.bigbook.commons.factory;

import jbailey.stu.mmu.bigbook.commons.dao.BookDAO;
import jbailey.stu.mmu.bigbook.commons.dao.query.JdbcQueryAdapter;
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import jbailey.stu.mmu.bigbook.commons.service.DefaultServicePort;
import jbailey.stu.mmu.bigbook.commons.service.ServicePort;

/**
 * Factory for creating the ServicePort with its dependencies.
 * Centralizes the instantiation of the service layer components,
 * promoting clean architecture by keeping wiring logic in one place.
 * This factory ensures framework agnosticism while providing a clean API
 * for both manual instantiation (MVC) and DI frameworks (Spring in REST).
 */
public class ServiceFactory {

    /**
     * Creates a fully wired ServicePort instance.
     * Builds the dependency chain: ServicePort -> BookService -> BookDAO -> JdbcQueryAdapter
     *
     * @return a new ServicePort implementation with all dependencies injected
     */
    public static ServicePort createServicePort() {
        return new DefaultServicePort(
            new BookService(
                new BookDAO(
                    new JdbcQueryAdapter()
                )
            )
        );
    }
}
