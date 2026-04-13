package jbailey.stu.mmu.bigbook.commons.serviceContainer;

import jbailey.stu.mmu.bigbook.commons.service.BookService;

/**
 * Outbound port for dependency injection for services.
 * Controllers and other consumers can depend on this abstraction to consume
 * services. This allows concrete implementations to be changed or swapped entirely
 * without changing consumer code. This is Dependency Inversion Principle.
 */
public interface ServiceContainer {
    BookService getBookService();
}

