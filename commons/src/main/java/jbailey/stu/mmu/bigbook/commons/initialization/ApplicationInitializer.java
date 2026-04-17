package jbailey.stu.mmu.bigbook.commons.initialization;

import jbailey.stu.mmu.bigbook.commons.factory.ServiceFactory;
import jbailey.stu.mmu.bigbook.commons.service.ServicePort;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.extern.slf4j.Slf4j;

// TODO: SORT THIS OUT PLEASE

/**
 * The application initializer. This class is the servlet context listener, which is executed when the web application starts.
 * It initializes a ServicePort to centralise access to other components (BookService, BookDAO, JdbcQueryAdapter, DatabaseConfig, etc.).
 * The ServicePort is stored in the servlet context, making it available to all servlets within an applications lifetime.
 */
@Slf4j
@WebListener
public class ApplicationInitializer implements ServletContextListener {

    /**
     * Initialize the application on boot.
     * Called by the servlet container when the web application starts.
     * Creates the ServicePort and stores it in the servlet application context.
     *
     * @param event is the ServletContextEvent which contains the ServletContext.
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            // Create the service container with all necessary services.
            // Uses factory for centralized instantiation
            ServicePort servicePort = ServiceFactory.createServicePort();

            // Store it in the servlet context so all servlets can access it.
            event.getServletContext().setAttribute("servicePort", servicePort);

            log.info("Application initialized successfully - ServicePort created");
        } catch (Exception exception) {
            log.error("Failed to initialize application", exception);
            throw new RuntimeException("Application initialization failed", exception);
        }
    }

    /**
     * Clean up on application shutdown.
     *
     * Called by the servlet container when the web application is shutting down.
     * Performs any necessary cleanup of resources.
     *
     * @param event The ServletContextEvent containing the ServletContext.
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        log.info("Application shutting down");
    }
}
