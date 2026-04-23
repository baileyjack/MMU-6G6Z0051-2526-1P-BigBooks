package jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.CommandResult;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.ControllerCommand;
import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.service.BookService;

import java.util.List;

/**
 * Provides the book service with a list request. Validates the request and forwards to the list book page with the results
 */
public class ListBooksCommand extends AbstractBaseBookCommand implements ControllerCommand {

    public ListBooksCommand(BookService bookService) {
        super(bookService);
    }

    @Override
    public CommandResult handleCommandRequest(HttpServletRequest request, HttpServletResponse response) {
        int page = parseNonNegativeInt(request.getParameter("page"), 0);
        int pageSize = parsePositiveInt(request.getParameter("pageSize"), 10);

        List<Book> books = bookService.getAllBooksPaginated(page, pageSize);
        int totalBooks = bookService.getTotalBookCount();
        int totalPages = (int) Math.ceil((double) totalBooks / pageSize);

        request.setAttribute("books", books);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalBooks", totalBooks);

        return forward("/WEB-INF/views/listBooks.jsp");
    }
}

