package jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbailey.stu.mmu.bigbook.commons.service.ServicePort;
import jbailey.stu.mmu.bigbook.commons.service.BookService;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands.AddBookCommand;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands.DeleteBookCommand;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands.HomeCommand;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands.ListBooksCommand;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands.SearchBooksCommand;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands.ShowAddBookCommand;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands.ShowEditBookCommand;
import jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller.commands.UpdateBookCommand;
import jbailey.stu.mmu.bigbook.mvc.util.MVCErrorUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Front Controller handles all browser requests entering the application. This is a single servlet
 * which centralises the routing, command dispatch, redirect/forward and top-level error handling. 
 * Individual use cases are delegated to command objects which keeps the servlet focused on orchestration instead of business logic.
 * It depends on the {@link ServicePort} abstraction (retrieved from ServletContext)
 * to obtain a {@link BookService} object, following the Dependency Inversion Principle.
 */
@Slf4j
@WebServlet("/bigbookmvc/*")
public class FrontController extends HttpServlet {

    private final Map<String, ControllerCommand> commandRegistry = new HashMap<>();

    /**
     * Initialisation of the Controller and the command registry which gives a simple map of HTTP method & path to a Command.
     * @throws ServletException if the context is not provided to the controller.
     */
    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        ServicePort servicePort = (ServicePort) servletContext.getAttribute("servicePort");
        
        if (servicePort == null) {
            throw new ServletException("ServicePort not found in ServletContext. Check ApplicationContextListener.");
        }
        
        BookService bookService = servicePort.getBookService();

        commandRegistry.put(routePath("GET", "/home"), new HomeCommand(bookService));
        commandRegistry.put(routePath("GET", "/books"), new ListBooksCommand(bookService));
        commandRegistry.put(routePath("GET", "/books/new"), new ShowAddBookCommand(bookService));
        commandRegistry.put(routePath("POST", "/books"), new AddBookCommand(bookService));
        commandRegistry.put(routePath("GET", "/books/edit"), new ShowEditBookCommand(bookService));
        commandRegistry.put(routePath("POST", "/books/edit"), new UpdateBookCommand(bookService));
        commandRegistry.put(routePath("GET", "/books/delete"), new DeleteBookCommand(bookService));
        commandRegistry.put(routePath("GET", "/search"), new SearchBooksCommand(bookService));
    }

    // the below are overrides of the {@link HttpServlet}
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        dispatch(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        dispatch(request, response);
    }
    
    private void dispatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String path = normalisePath(request.getPathInfo());
        String routeKey = routePath(request.getMethod(), path);
        ControllerCommand command = commandRegistry.get(routeKey);

        if (command == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No route found for " + request.getMethod() + " " + path);
            return;
        }

        try {
            CommandResult result = command.handleCommandRequest(request, response);
            if (result == null || response.isCommitted()) {
                return;
            }

            if (result.getDispatchType() == CommandResult.DispatchType.REDIRECT) {
                response.sendRedirect(request.getContextPath() + result.getTarget());
                return;
            }

            request.getRequestDispatcher(result.getTarget()).forward(request, response);
        } catch (Exception exception) {
            MVCErrorUtility.handleServerError(response, log, exception, "An unexpected application error occurred.");
        }
    }

    private String normalisePath(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return "/home";
        }
        return pathInfo;
    }

    private String routePath(String method, String path) {
        return method.toUpperCase(Locale.ROOT) + " " + path;
    }
}
