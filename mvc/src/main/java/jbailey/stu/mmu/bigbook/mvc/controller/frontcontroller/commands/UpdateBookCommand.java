package jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.CommandResult;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.ControllerCommand;
import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.service.BookService;

import java.util.List;

/**
 * Provides the book service with an update request. Validate the request and redirect after the update
 */
public class UpdateBookCommand extends AbstractBaseBookCommand implements ControllerCommand {

    public UpdateBookCommand(BookService bookService) {
        super(bookService);
    }

    @Override
    public CommandResult handleCommandRequest(HttpServletRequest request, HttpServletResponse response) {
        int id = requireId(request.getParameter("id"));
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String date = request.getParameter("date");
        String genresStr = request.getParameter("genres");
        String charactersStr = request.getParameter("characters");
        String synopsis = request.getParameter("synopsis");

        List<String> errors = bookService.validateBook(title, author, date, synopsis);
        if (!errors.isEmpty()) {
            Book book = bookService.getBookById(id);
            request.setAttribute("errors", errors);
            request.setAttribute("book", book);
            request.setAttribute("title", title);
            request.setAttribute("author", author);
            request.setAttribute("date", date);
            request.setAttribute("genresStr", genresStr);
            request.setAttribute("charactersStr", charactersStr);
            request.setAttribute("synopsis", synopsis);
            return forward("/WEB-INF/views/editBook.jsp");
        }

        bookService.updateBook(id, title, author, date, genresStr, charactersStr, synopsis);
        return redirect("/bigbookmvc/books");
    }
}

