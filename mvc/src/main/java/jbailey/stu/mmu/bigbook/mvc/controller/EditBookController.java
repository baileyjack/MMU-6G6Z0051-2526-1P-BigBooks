package jbailey.stu.mmu.bigbook.mvc.controller;

// Jakarta Servlet imports
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// DI and Service layer
import jbailey.stu.mmu.bigbook.commons.service.ServicePort;
import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import jbailey.stu.mmu.bigbook.commons.util.DateUtility;

// Logging
import lombok.extern.slf4j.Slf4j;

// Java
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for editing an existing book.
 */
@Slf4j
public class EditBookController extends HttpServlet {

    private ServicePort servicePort;

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        this.servicePort = (ServicePort) servletContext.getAttribute("servicePort");
        if (servicePort == null) {
            throw new ServletException("Service not found in ServletContext.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            BookService bookService = servicePort.getBookService();
            String idParameter = request.getParameter("id");
            if (idParameter == null || idParameter.trim().isEmpty()) {
                log.warn("EditBook doGet: Missing or empty id parameter");
                try {
                    response.sendRedirect("listBooks");
                } catch (IOException ioException) {
                    log.error("Failed to redirect on missing id", ioException);
                }
                return;
            }
            int id;
            try {
                id = Integer.parseInt(idParameter);
            } catch (NumberFormatException numberFormatException) {
                log.error("EditBook doGet: Invalid id parameter: {}", idParameter, numberFormatException);
                try {
                    response.sendRedirect("listBooks");
                } catch (IOException ioException) {
                    log.error("Failed to redirect on invalid id", ioException);
                }
                return;
            }
            Book book = bookService.getBookById(id);
            if (book == null) {
                log.warn("EditBook doGet: Book not found for id: {}", id);
                try {
                    response.sendRedirect("listBooks");
                } catch (IOException ioException) {
                    log.error("Failed to redirect on book not found", ioException);
                }
                return;
            }
            
            // Normalise the date from the database to ISO 8601 format for display purposes
            // If the date is invalid or in the future, show invalid date message
            String isoDateToNormalise = DateUtility.normaliseDateToISO(book.getDate());
            String dateToDisplay;
            if (isoDateToNormalise == null || isoDateToNormalise.isEmpty()) {
                dateToDisplay = DateUtility.INVALID_DATE_MESSAGE;
            } else {
                try {
                    LocalDate parsedDate = LocalDate.parse(isoDateToNormalise);
                    if (parsedDate.isAfter(LocalDate.now())) {
                        dateToDisplay = DateUtility.INVALID_DATE_MESSAGE;
                    } else {
                        dateToDisplay = isoDateToNormalise;
                    }
                } catch (Exception exception) {
                    dateToDisplay = DateUtility.INVALID_DATE_MESSAGE;
                }
            }

            request.setAttribute("book", book);
            request.setAttribute("genresStr", bookService.serialiseGenres(book.getGenres()));
            request.setAttribute("charactersStr", bookService.serialiseCharacters(book.getCharacters()));
            // Display either ISO date or the invalid date message in the form
            request.setAttribute("date", dateToDisplay != null ? dateToDisplay : "");
            
            request.getRequestDispatcher("/WEB-INF/views/editBook.jsp").forward(request, response);
        } catch (Exception e) {
            log.error("EditBook doGet: Unexpected error", e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
            } catch (IOException ioException) {
                log.error("Failed to send error response", ioException);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            BookService bookService = servicePort.getBookService();
            EditBookFormDataFields formData = extractEditBookFormData(request);

            // Validate input via the Faxade
            List<String> errors = bookService.validateBook(formData.title, formData.author, formData.date, formData.synopsis);

            if (!errors.isEmpty()) {
                // If there are errors, reload book and show the form again with error messages
                Book book = bookService.getBookById(formData.id);
                request.setAttribute("errors", errors);
                request.setAttribute("book", book);
                request.setAttribute("title", formData.title);
                request.setAttribute("author", formData.author);
                request.setAttribute("date", formData.date);
                request.setAttribute("genresStr", formData.genresStr);
                request.setAttribute("charactersStr", formData.charactersStr);
                request.setAttribute("synopsis", formData.synopsis);
                request.getRequestDispatcher("/WEB-INF/views/editBook.jsp").forward(request, response);
                return;
            }

            // Delegate update to the Facade
            bookService.updateBook(formData.id, formData.title, formData.author, formData.date,
                    formData.genresStr, formData.charactersStr, formData.synopsis);

            try {
                response.sendRedirect("listBooks");
            } catch (IOException ioException) {
                log.error("Failed to redirect after update", ioException);
            }
        } catch (Exception exception) {
            log.error("EditBook doPost: Unexpected error", exception);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred while updating the book.");
            } catch (IOException ioException) {
                log.error("Failed to send error response", ioException);
            }
        }
    }

    /**
     * private class to hold extracted form data from an edit request.
     */
    private static class EditBookFormDataFields {
        int id;
        String title;
        String author;
        String date;
        String genresStr;
        String charactersStr;
        String synopsis;
    }

    /**
     * Extract form data from a given request.
     */
    private EditBookFormDataFields extractEditBookFormData(HttpServletRequest request) {
        EditBookFormDataFields bookEditData = new EditBookFormDataFields();
        try {
            String bookIDparam = request.getParameter("id");
            if (bookIDparam == null || bookIDparam.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing id parameter");
            }
            bookEditData.id = Integer.parseInt(bookIDparam);
        } catch (NumberFormatException numberFormatException) {
            log.error("EditBook extractEditBookFormData: Invalid id parameter", numberFormatException);
            throw new IllegalArgumentException("Invalid id parameter", numberFormatException);
        }
        bookEditData.title = request.getParameter("title");
        bookEditData.author = request.getParameter("author");
        bookEditData.date = request.getParameter("date");
        bookEditData.genresStr = request.getParameter("genres");
        bookEditData.charactersStr = request.getParameter("characters");
        bookEditData.synopsis = request.getParameter("synopsis");
        return bookEditData;
    }
}
