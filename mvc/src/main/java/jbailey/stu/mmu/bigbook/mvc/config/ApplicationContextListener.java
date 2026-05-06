package jbailey.stu.mmu.bigbook.mvc.config;

// Java Imports
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

// Internal Imports
import jbailey.stu.mmu.bigbook.commons.factory.ServiceFactory;
import jbailey.stu.mmu.bigbook.commons.service.ServicePort;

// External Imports
import lombok.extern.slf4j.Slf4j;

/**
 * Initialises the dependency container at app boot.
 * Runs when the servlet context is created, and wires all services. It stores
 * the container in {@link ServletContext} for access by any controllers.
 * Controllers can then retrieve services through {@code ServicePort container = (ServicePort) servletContext.getAttribute("servicePort");}
 */
@Slf4j
@WebListener
public class ApplicationContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        ServletContext servletContext = contextEvent.getServletContext();
        
        log.info("Initialising application context...");
        ServicePort service = ServiceFactory.createServicePort();
        servletContext.setAttribute("servicePort", service);
        log.info("Service initialised and stored in ServletContext");
    }

    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        log.info("Application context destroyed");
    }
}
