package com.company.option;

public class ExecutionException extends Exception {
    public ExecutionException(Throwable t) {
        super(t);
    }

    public ExecutionException(String msg) {
        super(msg);
    }

    public ExecutionException(String msg, Throwable t) {
        super(msg, t);
    }
}
