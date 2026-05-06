package jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands;

// Java Imports
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Internal Imports
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.CommandResult;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.ControllerCommand;
import jbailey.stu.mmu.bigbook.commons.service.BookService;

/**
 * Provides the book service with a book deletion request. Validates the request and redirects to the all books page after the update
 */
public class DeleteBookCommand extends AbstractBaseBookCommand implements ControllerCommand {

    public DeleteBookCommand(BookService bookService) {
        super(bookService);
    }

    @Override
    public CommandResult handleCommandRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            int id = requireId(request.getParameter("id"));
            bookService.deleteBook(id);
        } catch (IllegalArgumentException silentException) {
            // Invalid ids will always return the user to the book list as we can't delete somehting that doesn't exist
        }
        return redirect("/bigbookmvc/books");
    }
}

