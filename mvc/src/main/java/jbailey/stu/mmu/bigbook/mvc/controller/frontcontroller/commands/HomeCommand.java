package jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands;

// Internal IMports
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.CommandResult;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.ControllerCommand;
import jbailey.stu.mmu.bigbook.commons.model.Book;
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import jbailey.stu.mmu.bigbook.commons.util.DateUtility;

// Java Imports
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the home page display logic.
 * Includes getting all books, validating their dates, and listing the 5 most recent books.
 */
public class HomeCommand extends AbstractBaseBookCommand implements ControllerCommand {

    public HomeCommand(BookService bookService) {
        super(bookService);
    }

    @Override
    public CommandResult handleCommandRequest(HttpServletRequest request, HttpServletResponse response) {
        List<Book> allBooks = bookService.getAllBooks();
        List<Book> validDatedBooks = new ArrayList<>();

        // todo: Could we make the DB query only return valid dates instead of doing this in memory?

        for (Book book : allBooks) {
            String isoDate = DateUtility.normaliseDateToISO(book.getDate());
            boolean isValid = false;
            if (isoDate != null && !isoDate.isEmpty()) {
                try {
                    // Cgeck that the book doesn't have a date after today to avoid invalid entries
                    LocalDate dateParse = LocalDate.parse(isoDate);
                    isValid = !dateParse.isAfter(LocalDate.now());
                } catch (Exception exception) {
                    isValid = false;
                }
            }
            if (isValid) {
                // Only use books that return valid for the recent books list
                validDatedBooks.add(book);
            }
        }

        validDatedBooks.sort((bookOne, bookTwo) -> {
            String bookOneDate = DateUtility.normaliseDateToISO(bookOne.getDate());
            String bookTwoDate = DateUtility.normaliseDateToISO(bookTwo.getDate());
            // If both are null then sort by id desc. If one is null then the null is considerd less recent...
            // Otherwise compare dates and if they are the same compare id desc
            if (bookOneDate == null && bookTwoDate == null) return Integer.compare(bookTwo.getId(), bookOne.getId());
            if (bookOneDate == null) return 1;
            if (bookTwoDate == null) return -1;
            int comparedDate = bookTwoDate.compareTo(bookOneDate);
            if (comparedDate != 0) return comparedDate;
            return Integer.compare(bookTwo.getId(), bookOne.getId());
        });
        List<Book> recentBooks = validDatedBooks.subList(0, Math.min(5, validDatedBooks.size()));
        request.setAttribute("recentBooks", recentBooks);
        return forward("/WEB-INF/views/home.jsp");
    }
}

