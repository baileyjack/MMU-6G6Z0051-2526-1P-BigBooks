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
import jbailey.stu.mmu.bigbook.mvc.util.MVCErrorUtility;
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
                    response.sendRedirect(request.getContextPath() + "/listBooks");
                    return;
                }
                int id;
                try {
                    id = Integer.parseInt(idParameter.trim());
                } catch (NumberFormatException numberFormatException) {
                    log.warn("DeleteBook doGet: Invalid id parameter: {}", idParameter);
                    response.sendRedirect(request.getContextPath() + "/listBooks");
                    return;
                }
                bookService.deleteBook(id);
                response.sendRedirect(request.getContextPath() + "/listBooks");
            } catch (Exception exception) {
                MVCErrorUtility.handleServerError(response, log, exception, "An unexpected error occurred while deleting the book.");
            }
        }
}
