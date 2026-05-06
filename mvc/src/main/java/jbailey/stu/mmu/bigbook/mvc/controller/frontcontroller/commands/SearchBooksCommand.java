package jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands;

// Internal Imports
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.CommandResult;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.ControllerCommand;
import jbailey.stu.mmu.bigbook.commons.model.Book;

// Java Imports
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Handles search requests centrally through the Front Controller.
 */
public class SearchBooksCommand extends AbstractBaseBookCommand implements ControllerCommand {

    public SearchBooksCommand(BookService bookService) {
        super(bookService);
    }

    @Override
    public CommandResult handleCommandRequest(HttpServletRequest request, HttpServletResponse response) {
        String search = request.getParameter("search");
        int page = parseNonNegativeInt(request.getParameter("page"), 0);
        int pageSize = parsePositiveInt(request.getParameter("pageSize"), 10);

        if (search == null || search.trim().isEmpty()) {
            request.setAttribute("books", List.of());
            request.setAttribute("searchQuery", "");
            request.setAttribute("currentPage", 0);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("totalPages", 0);
            request.setAttribute("totalBooks", 0);
            return forward("/WEB-INF/views/searchResults.jsp");
        }

        List<Book> books = bookService.searchPaginatedBooksList(search, page, pageSize);
        int totalBooks = bookService.getSearchBookCount(search);
        int totalPages = (int) Math.ceil((double) totalBooks / pageSize);

        request.setAttribute("books", books);
        request.setAttribute("searchQuery", search);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalBooks", totalBooks);

        return forward("/WEB-INF/views/searchResults.jsp");
    }
}


