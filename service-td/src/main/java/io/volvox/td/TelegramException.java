package io.volvox.td;

public class TelegramException extends Exception {

    private final int code;
    private final String message;

    public TelegramException(int code, String message) {
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
