package jbailey.stu.mmu.bigbook.mvc.controller;

// Jakarta Servlet imports
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// DI and Service layer
import jbailey.stu.mmu.bigbook.commons.service.ServicePort;
import jbailey.stu.mmu.bigbook.commons.service.BookService;

// Logging
import lombok.extern.slf4j.Slf4j;

// Java
import java.io.IOException;

/**
 * Controller for deleting a book from the database.
 */
@Slf4j
public class DeleteBookController extends HttpServlet {

    private ServicePort servicePort;

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        this.servicePort = (ServicePort) servletContext.getAttribute("servicePort");
        if (servicePort == null) {
            throw new ServletException("Service not found in ServletContext");
        }
    }

    /**
     * Handles a delete request via GET.
     * @param request as an HTTPServletRequest object
     * @param response as an HTTPServletResponse response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            BookService bookService = servicePort.getBookService();
            String idParameter = request.getParameter("id");
            if (idParameter == null || idParameter.trim().isEmpty()) {
                log.warn("DeleteBook doGet: Missing or empty ID");
                try {
                    response.sendRedirect("listBooks");
                } catch (IOException ioException) {
                    log.error("Failed to redirect due to a missing ID", ioException);
                }
                return;
            }
            int id;
            try {
                id = Integer.parseInt(idParameter);
            } catch (NumberFormatException numberFormatException) {
                log.error("DeleteBook doGet: Invalid id parameter: {}", idParameter, numberFormatException);
                try {
                    response.sendRedirect("listBooks");
                } catch (IOException ioException) {
                    log.error("Failed to redirect on invalid id", ioException);
                }
                return;
            }
            bookService.deleteBook(id);
            try {
                response.sendRedirect("listBooks");
            } catch (IOException ioException) {
                log.error("Failed to redirect after delete", ioException);
            }
        } catch (Exception exception) {
            log.error("DeleteBook doGet: Unexpected error", exception);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred while deleting the book.");
            } catch (IOException ioException) {
                log.error("Failed to send error response", ioException);
            }
        }
    }
}
