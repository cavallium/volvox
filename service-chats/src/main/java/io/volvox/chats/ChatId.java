package io.volvox.chats;

public record ChatId(Type type, long id) {
    ChatId(String id) {
        this(getType(id), getIdLong(id));
    }

    private static Type getType(String id) {
        return switch (id.charAt(0)) {
            case 's' -> Type.SUPER;
            case 'b' -> Type.BASIC;
            case 'u' -> Type.PRIVATE;
            default -> throw new IllegalArgumentException();
        };
    }

    private static long getIdLong(String id) {
        return Long.parseUnsignedLong(id.substring(1));
    }

    public enum Type {
        BASIC,
        SUPER,
        PRIVATE
    }

    @Override
    public String toString() {
        return switch (type) {
            case SUPER -> 's';
            case BASIC -> 'b';
            case PRIVATE -> 'u';
        } + Long.toUnsignedString(id);
    }
}
