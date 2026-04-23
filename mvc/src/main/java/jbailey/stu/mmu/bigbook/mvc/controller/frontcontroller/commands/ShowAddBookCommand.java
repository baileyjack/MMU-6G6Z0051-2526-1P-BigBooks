package jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.CommandResult;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.ControllerCommand;
import jbailey.stu.mmu.bigbook.commons.service.BookService;

/**
 * Shows the form at add a new book to the DB.
 */
public class ShowAddBookCommand extends AbstractBaseBookCommand implements ControllerCommand {

    public ShowAddBookCommand(BookService bookService) {
        super(bookService);
    }

    @Override
    public CommandResult handleCommandRequest(HttpServletRequest request, HttpServletResponse response) {
        return forward("/WEB-INF/views/addBook.jsp");
    }
}

