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
import java.util.List;

/**
 * Controller for adding a book to the database.
 * Depends on the {@link ServicePort} abstraction via ServletContext
 * to retrieve a {@link BookService}. This follows the Dependency Inversion Principle
 */
@Slf4j
public class AddBookController extends HttpServlet {

    private ServicePort servicePort;

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        this.servicePort = (ServicePort) servletContext.getAttribute("servicePort");
        if (servicePort == null) {
            throw new ServletException("ServicePort could not be found in ServletContext.");
        }
    }

    /**
     * Loads the form with a GET request
     * @param request as an HTTPServletRequest object
     * @param response as an HTTPServletResponse response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.getRequestDispatcher("/WEB-INF/views/addBook.jsp").forward(request, response);
        } catch (Exception e) {
            log.error("AddBook doGet: Unexpected error", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
            } catch (IOException ioEx) {
                log.error("Failed to send error response", ioEx);
            }
        }
    }

    /**
     * Handles inputted form data and adds the book to the database with a POST request.
     * All dates are formatted to be in ISO 8601 format as per requirements analysis.
     * 
     * @param request HttpServletRequest containing form data
     * @param response HttpServletResponse for redirects/errors
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            BookService bookService = servicePort.getBookService();
            AddBookFormDataFields formData = extractBookFormData(request);

            // Validate through the BookService Facade which will return a list of errors if any are found.
            List<String> errors = bookService.validateBook(formData.title, formData.author, formData.date, formData.synopsis);

            if (!errors.isEmpty()) {
                // If errors are present in the response, show the form again with the errors included
                request.setAttribute("errors", errors);
                request.setAttribute("title", formData.title);
                request.setAttribute("author", formData.author);
                request.setAttribute("date", formData.date);
                request.setAttribute("genres", formData.genresStr);
                request.setAttribute("characters", formData.charactersStr);
                request.setAttribute("synopsis", formData.synopsis);
                request.getRequestDispatcher("/WEB-INF/views/addBook.jsp").forward(request, response);
                return;
            }

            // Delegate creation
            bookService.addBook(formData.title, formData.author, formData.date,
                    formData.genresStr, formData.charactersStr, formData.synopsis);

            try {
                response.sendRedirect("listBooks");
            } catch (IOException ioEx) {
                log.error("Failed to redirect after insert", ioEx);
            }
        } catch (Exception e) {
            log.error("AddBook doPost: Unexpected error", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred while adding the book.");
            } catch (IOException ioEx) {
                log.error("Failed to send error response", ioEx);
            }
        }
    }

    public static class AddBookFormDataFields {
        String title;
        String author;
        String date;
        String genresStr;
        String charactersStr;
        String synopsis;
    }

    private AddBookFormDataFields extractBookFormData(HttpServletRequest request) {
        AddBookFormDataFields data = new AddBookFormDataFields();
        data.title = request.getParameter("title");
        data.author = request.getParameter("author");
        data.date = request.getParameter("date");
        data.genresStr = request.getParameter("genres");
        data.charactersStr = request.getParameter("characters");
        data.synopsis = request.getParameter("synopsis");
        return data;
    }
}
