package jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands;

// Internal Imports
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.CommandResult;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.ControllerCommand;
import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import jbailey.stu.mmu.bigbook.commons.util.DateUtility;

// Java Imports
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * Shows the edit-book form for a provided book ID.
 */
public class ShowEditBookCommand extends AbstractBaseBookCommand implements ControllerCommand {

    public ShowEditBookCommand(BookService bookService) {
        super(bookService);
    }

    @Override
    public CommandResult handleCommandRequest(HttpServletRequest request, HttpServletResponse response) {
        int bookID;
        try {
            bookID = requireId(request.getParameter("id"));
            // Redirect if the arg is invalid
        } catch (IllegalArgumentException ex) {
            return redirect("/bigbookmvc/books");
        }

        // Retreieve the ID from the service
        Book book = bookService.getBookById(bookID);
        if (book == null) {
            // Redirect if the book ID isn't in the DB
            return redirect("/bigbookmvc/books");
        }

        String normalisedISODate = DateUtility.normaliseDateToISO(book.getDate());
        String dateToDisplay;
        if (normalisedISODate == null || normalisedISODate.isEmpty()) {
            dateToDisplay = DateUtility.INVALID_DATE_MESSAGE;
        } else {
            try {
                LocalDate bookDate = LocalDate.parse(normalisedISODate);
                dateToDisplay = bookDate.isAfter(LocalDate.now()) ? DateUtility.INVALID_DATE_MESSAGE : normalisedISODate;
            } catch (Exception exception) {
                dateToDisplay = DateUtility.INVALID_DATE_MESSAGE;
            }
        }

        request.setAttribute("book", book);
        request.setAttribute("genresStr", bookService.serialiseGenres(book.getGenres()));
        request.setAttribute("charactersStr", bookService.serialiseCharacters(book.getCharacters()));
        request.setAttribute("date", dateToDisplay != null ? dateToDisplay : ""); // If date display is null, show nothing as a catch all

        return forward("/WEB-INF/views/editBook.jsp");
    }
}

