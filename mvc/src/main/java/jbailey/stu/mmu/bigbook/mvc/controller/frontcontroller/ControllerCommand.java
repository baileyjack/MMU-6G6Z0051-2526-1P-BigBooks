package jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
/**
 * The interface used by the Front Controller.
 * Each concrete implementation of the command handles one use case.
 */
@FunctionalInterface
public interface ControllerCommand {
    CommandResult handleCommandRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
