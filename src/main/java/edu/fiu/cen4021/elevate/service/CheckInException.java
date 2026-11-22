package edu.fiu.cen4021.elevate.service;

/** A check-in action was rejected by a business rule. The message is shown to the employee. */
public class CheckInException extends RuntimeException {
    public CheckInException(String message) {
        super(message);
    }
}
