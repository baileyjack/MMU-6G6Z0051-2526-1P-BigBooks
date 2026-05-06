package jbailey.stu.mmu.bigbook.rest;

import jakarta.servlet.http.HttpServlet;
import jbailey.stu.mmu.bigbook.commons.dao.BookDAO;
import jbailey.stu.mmu.bigbook.commons.dao.query.JdbcQueryAdapter;
import jbailey.stu.mmu.bigbook.commons.dao.query.QueryPort;
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import jbailey.stu.mmu.bigbook.commons.service.DefaultServicePort;
import jbailey.stu.mmu.bigbook.commons.service.ServicePort;
import jbailey.stu.mmu.bigbook.rest.controller.BookResource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Main application entrypoint for the REST API.
 * Bootstraps the {@link SpringApplication} with embedded Tomcat,
 * and registers {@link HttpServlet} for deployment as am app.
 * Uses Spring for dep. inversion and to wire commons components without a framework required.
 */
@SpringBootApplication
public class BigBookAPI {

    public static void main(String[] args) {
        SpringApplication.run(BigBookAPI.class, args);
    }

    /**
     * Bean for JDBC Query Adapter.
     *
     * @return {@link QueryPort} implementation
     */
    @Bean
    public QueryPort queryPort() {
        return new JdbcQueryAdapter();
    }

    /**
     * Bean for {@link BookDAO}, injected with {@link QueryPort}.
     *
     * @param queryPort the {@link QueryPort}
     * @return {@link BookDAO} instance
     */
    @Bean
    public BookDAO bookDAO(QueryPort queryPort) {
        return new BookDAO(queryPort);
    }

    /**
     * Bean for {@link BookService}, injected with {@link BookDAO}.
     *
     * @param bookDAO the data access object
     * @return {@link BookService} instance
     */
    @Bean
    public BookService bookService(BookDAO bookDAO) {
        return new BookService(bookDAO);
    }

    /**
     * Bean to provide {@link ServicePort} instance for dependency injection.
     * Uses Spring-managed {@link BookService}.
     *
     * @param bookService the {@link BookService}
     * @return {@link ServicePort} implementation
     */
    @Bean
    public ServicePort servicePort(BookService bookService) {
        return new DefaultServicePort(bookService);
    }

    /**
     * Registers the {@link BookResource} servlet manually.
     * Maps URLs starting with {@code /books/*} to the {@link BookResource} servlet.
     *
     * @param servicePort the {@link ServicePort}
     * @return {@link ServletRegistrationBean} for {@link BookResource}
     */
    @Bean
    public ServletRegistrationBean<BookResource> bookResourceServlet(ServicePort servicePort) {
        ServletRegistrationBean<BookResource> registration = new ServletRegistrationBean<>();
        registration.setServlet(new BookResource(servicePort));
        registration.addUrlMappings("/books/*");
        registration.setLoadOnStartup(1);
        return registration;
    }
}
