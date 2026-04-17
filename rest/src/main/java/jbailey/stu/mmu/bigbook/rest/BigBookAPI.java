package jbailey.stu.mmu.bigbook.rest;

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
 * Bootstraps the Spring Boot application with embedded Tomcat,
 * and registers the HttpServlet manually for deployment as a Spring JAR.
 * Uses Spring for DI to wire commons components with frameworks agnostically.
 */
@SpringBootApplication
public class BigBookAPI {

    public static void main(String[] args) {
        SpringApplication.run(BigBookAPI.class, args);
    }

    /**
     * Bean for JDBC Query Adapter.
     *
     * @return QueryPort implementation
     */
    @Bean
    public QueryPort queryPort() {
        return new JdbcQueryAdapter();
    }

    /**
     * Bean for BookDAO, injected with QueryPort.
     *
     * @param queryPort the query port
     * @return BookDAO instance
     */
    @Bean
    public BookDAO bookDAO(QueryPort queryPort) {
        return new BookDAO(queryPort);
    }

    /**
     * Bean for BookService, injected with BookDAO.
     *
     * @param bookDAO the data access object
     * @return BookService instance
     */
    @Bean
    public BookService bookService(BookDAO bookDAO) {
        return new BookService(bookDAO);
    }

    /**
     * Bean to provide ServicePort instance for dependency injection.
     * Uses Spring-managed BookService.
     *
     * @param bookService the book service
     * @return ServicePort implementation
     */
    @Bean
    public ServicePort servicePort(BookService bookService) {
        return new DefaultServicePort(bookService);
    }

    /**
     * Registers the BookResource servlet manually.
     * Maps URLs starting with /books/* to the BookResource servlet.
     *
     * @param servicePort the service port
     * @return ServletRegistrationBean for BookResource
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
