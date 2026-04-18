package jbailey.stu.mmu.bigbook.commons.factory;

import jbailey.stu.mmu.bigbook.commons.dao.BookDAO;
import jbailey.stu.mmu.bigbook.commons.dao.query.JdbcQueryAdapter;
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import jbailey.stu.mmu.bigbook.commons.service.DefaultServicePort;
import jbailey.stu.mmu.bigbook.commons.service.ServicePort;

/**
 * Factory which enables creating the {@link ServicePort} with its dependencies.
 * It centralises the instantiation of service layer components,
 * enabling clean architecture by keeping wiring logic in one place.
 * This factory ensures frameworks can be swapped out easily while providing a usable API
 * for both manual instantiation (MVC) and DI frameworks (Spring in REST).
 */
public class ServiceFactory {

    /**
     * Creates a fully wired {@link ServicePort} instance.
     * Builds the dependency chain: {@link ServicePort} -> {@link BookService} -> {@link BookDAO} -> {@link JdbcQueryAdapter}
     *
     * @return a new {@link ServicePort} implementation with all dependencies injected
     */
    public static ServicePort createServicePort() {
        // This makes it easy to swap out any of the components in the application easily. i.e. Adding a new SerVice Factory Creator here with a different query adapter, or a different object model etc.
        return new DefaultServicePort(
            new BookService(
                new BookDAO(
                    new JdbcQueryAdapter()
                )
            )
        );
    }
}
