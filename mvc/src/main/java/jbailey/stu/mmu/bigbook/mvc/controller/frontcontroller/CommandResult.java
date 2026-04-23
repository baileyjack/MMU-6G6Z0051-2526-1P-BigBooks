package jbailey.stu.mmu.bigbook.mvc.controller.frontcontroller;

/**
 * Represents the outcome of a front controller command.
 * The controller reads the command result and performs final dispatch
 * either by forwarding to a JSP or redirecting to another route.
 */
public final class CommandResult {

    public enum DispatchType {
        FORWARD,
        REDIRECT
    }

    private final DispatchType dispatchType;
    private final String dispatchTarget;

    private CommandResult(DispatchType dispatchType, String dispatchTarget) {
        this.dispatchType = dispatchType;
        this.dispatchTarget = dispatchTarget;
    }

    public static CommandResult forward(String dispatchTarget) {
        return new CommandResult(DispatchType.FORWARD, dispatchTarget);
    }

    public static CommandResult redirect(String target) {
        return new CommandResult(DispatchType.REDIRECT, target);
    }

    public DispatchType getDispatchType() {
        return dispatchType;
    }

    public String getTarget() {
        return dispatchTarget;
    }
}

