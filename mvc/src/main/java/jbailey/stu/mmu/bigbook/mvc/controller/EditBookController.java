package jbailey.stu.mmu.bigbook.mvc.controller;

import jbailey.stu.mmu.bigbook.commons.service.ServicePort;
import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import jbailey.stu.mmu.bigbook.commons.util.DateUtility;
import jbailey.stu.mmu.bigbook.mvc.util.MVCErrorUtility;

// External Imports
import lombok.extern.slf4j.Slf4j;

// Java
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
                response.sendRedirect(request.getContextPath() + "/listBooks");
                return;
            }
            int id;
            try {
                id = Integer.parseInt(idParameter.trim());
            } catch (NumberFormatException numberFormatException) {
                log.warn("EditBook doGet: Invalid id parameter: {}", idParameter);
                response.sendRedirect(request.getContextPath() + "/listBooks");
                return;
            }
            Book book = bookService.getBookById(id);
            if (book == null) {
                log.warn("EditBook doGet: Book not found for id: {}", id);
                response.sendRedirect(request.getContextPath() + "/listBooks");
                return;
            }
            // Normalise the date from the database to ISO 8601 format for display purposes
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
            request.setAttribute("date", dateToDisplay != null ? dateToDisplay : "");
            request.getRequestDispatcher("/WEB-INF/views/editBook.jsp").forward(request, response);
        } catch (Exception e) {
            MVCErrorUtility.handleServerError(response, log, e, "An unexpected error occurred while loading the edit form.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setCharacterEncoding("UTF-8");
            BookService bookService = servicePort.getBookService();
            EditBookFormDataFields formData = extractEditBookFormData(request);
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
            bookService.updateBook(formData.id, formData.title, formData.author, formData.date,
                    formData.genresStr, formData.charactersStr, formData.synopsis);
            response.sendRedirect(request.getContextPath() + "/listBooks");
        } catch (Exception exception) {
            MVCErrorUtility.handleServerError(response, log, exception, "An unexpected error occurred while updating the book.");
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
