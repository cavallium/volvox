package io.volvox.td;

public class TdException extends Exception {

    private final int code;
    private final String message;

    public TdException(int code, String message) {
        super(code + ": " + message);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getErrorMessage() {
        return message;
    }

    @Override public String toString() {
        return code + ": " + message;
    }
}
