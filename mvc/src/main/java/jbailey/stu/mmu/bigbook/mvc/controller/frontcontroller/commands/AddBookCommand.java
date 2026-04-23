package jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.CommandResult;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.ControllerCommand;
import jbailey.stu.mmu.bigbook.commons.service.BookService;

import java.util.List;

/**
 * Provides the book service with a book creation request. Validates the request and redirects to the all books page after the update
 */
public class AddBookCommand extends AbstractBaseBookCommand implements ControllerCommand {

    public AddBookCommand(BookService bookService) {
        super(bookService);
    }

    @Override
    public CommandResult handleCommandRequest(HttpServletRequest request, HttpServletResponse response) {
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String date = request.getParameter("date");
        String genres = request.getParameter("genres");
        String characters = request.getParameter("characters");
        String synopsis = request.getParameter("synopsis");

        List<String> errors = bookService.validateBook(title, author, date, synopsis);

        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            request.setAttribute("title", title);
            request.setAttribute("author", author);
            request.setAttribute("date", date);
            request.setAttribute("genres", genres);
            request.setAttribute("characters", characters);
            request.setAttribute("synopsis", synopsis);
            return forward("/WEB-INF/views/addBook.jsp");
        }

        bookService.addBook(title, author, date, genres, characters, synopsis);

        return redirect("/bigbookmvc/books");
    }
}

