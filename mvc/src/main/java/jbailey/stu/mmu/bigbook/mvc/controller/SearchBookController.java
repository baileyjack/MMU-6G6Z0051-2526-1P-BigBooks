package jbailey.stu.mmu.bigbook.mvc.controller;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbailey.stu.mmu.bigbook.commons.service.ServicePort;
import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * Controller for searching books by title or author with pagination.
 * Depends on {@link ServicePort} abstraction (injected via ServletContext)
 * to retrieve {@link BookService}. This follows the Dependency Inversion Principle:
 * the controller depends on an abstraction, not on concrete factories.
 */
@Slf4j
public class SearchBookController extends HttpServlet {

    private ServicePort servicePort;

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        this.servicePort = (ServicePort) servletContext.getAttribute("servicePort");
        if (servicePort == null) {
            throw new ServletException("ServicePort not found in ServletContext");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            BookService bookService = servicePort.getBookService();

            // Set empty attributes for the view in case of empty values
            String search = request.getParameter("search");
            if (search == null || search.trim().isEmpty()) {
                log.warn("SearchBooks doGet: Missing or empty search parameter");
                request.setAttribute("books", List.of());
                request.setAttribute("searchQuery", "");
                request.setAttribute("currentPage", 0);
                request.setAttribute("pageSize", 10);
                request.setAttribute("totalPages", 0);
                request.setAttribute("totalBooks", 0);
                request.getRequestDispatcher("/WEB-INF/views/searchResults.jsp").forward(request, response);
                return;
            }

            int currentPage = 0;
            int pageSize = 10; // Default to 10 pages
            String pageParameter = request.getParameter("currentPage");
            String pageSizeParameter = request.getParameter("pageSize");

            if (pageParameter != null) {
                try {
                    currentPage = Integer.parseInt(pageParameter);
                    if (currentPage < 0) currentPage = 0;
                } catch (NumberFormatException numberFormatException) {
                    currentPage = 0;
                }
            }

            if (pageSizeParameter != null) {
                try {
                    pageSize = Integer.parseInt(pageSizeParameter);
                    if (pageSize < 1) pageSize = 10;
                } catch (NumberFormatException numberFormatException) {
                    pageSize = 10;
                }
            }

            // Create a new list of searched books based on the criteria
            List<Book> books = bookService.searchPaginatedBooksList(search, currentPage, pageSize);
            int totalBooks = bookService.getSearchBookCount(search);
            int totalPages = (int) Math.ceil((double) totalBooks / pageSize);

            request.setAttribute("books", books);
            request.setAttribute("searchQuery", search);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalBooks", totalBooks);

            request.getRequestDispatcher("/WEB-INF/views/searchResults.jsp").forward(request, response);
        } catch (Exception exception) {
            log.error("SearchBooks doGet: Unexpected error", exception);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred while searching for books.");
            } catch (IOException ioException) {
                log.error("Failed to send error response", ioException);
            }
        }
    }
}
