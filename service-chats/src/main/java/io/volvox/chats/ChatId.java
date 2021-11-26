package io.volvox.chats;

public record ChatId(Type type, long subId) {

    public static final int SUB_ID_MASK_BYTES =  52;
    public static final int TYPE_MASK_BYTES =    2;

    public static final long SUB_ID_MASK =       0b001111111111111111111111111111111111111111111111111111L;
    public static final long TYPE_MASK =         0b11L << SUB_ID_MASK_BYTES;
    public static final long MASK =              SUB_ID_MASK | TYPE_MASK;
    public static final int TYPE_PRIVATE_INT =   0b00;
    public static final int TYPE_BASIC_INT =     0b01;
    public static final int TYPE_SUPER_INT =     0b10;
    public static final int TYPE_SECRET_INT =    0b11;
    public static final long TYPE_PRIVATE_LONG = 0;
    public static final long TYPE_BASIC_LONG =   0b01L << SUB_ID_MASK_BYTES & TYPE_MASK;
    public static final long TYPE_SUPER_LONG =   0b10L << SUB_ID_MASK_BYTES & TYPE_MASK;
    public static final long TYPE_SECRET_LONG =  0b11L << SUB_ID_MASK_BYTES & TYPE_MASK;

    public ChatId {
        if ((subId & SUB_ID_MASK) != subId) {
            throw new IllegalArgumentException("subId is too big");
        }
    }

    public static ChatId fromLong(long id) {
        return new ChatId(getType(id), getIdLong(id));
    }

    private static Type getType(long id) {
        return switch ((int) ((id & TYPE_MASK) >> SUB_ID_MASK_BYTES)) {
            case TYPE_SUPER_INT -> Type.SUPER;
            case TYPE_BASIC_INT -> Type.BASIC;
            case TYPE_PRIVATE_INT -> Type.PRIVATE;
            case TYPE_SECRET_INT -> Type.SECRET;
            default -> throw new IllegalArgumentException("Invalid id type: " + id);
        };
    }

    private static long getIdLong(long id) {
        return id & SUB_ID_MASK;
    }

    public long toLong() {
        return switch (type) {
            case SUPER -> TYPE_SUPER_LONG;
            case BASIC -> TYPE_BASIC_LONG;
            case PRIVATE -> TYPE_PRIVATE_LONG;
            case SECRET -> TYPE_SECRET_LONG;
        } | (subId & SUB_ID_MASK);
    }

    public enum Type {
        PRIVATE,
        BASIC,
        SUPER,
        SECRET
    }

    @Override
    public String toString() {
        return toString(this);
    }

    public static String toString(ChatId chatId) {
        return Long.toUnsignedString(chatId.subId) + "-" + switch (chatId.type) {
            case SUPER -> 's';
            case BASIC -> 'b';
            case PRIVATE -> 'u';
            case SECRET -> 'd';
        };
    }

    public static String toString(long chatId) {
        return Long.toUnsignedString(getIdLong(chatId)) + "-" + switch (getType(chatId)) {
            case SUPER -> 's';
            case BASIC -> 'b';
            case PRIVATE -> 'u';
            case SECRET -> 'd';
        };
    }

    public static ChatId fromString(String chatId) {
        var parts = chatId.split("-", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Malformed chat id");
        }
        if (parts[1].length() != 1) {
            throw new IllegalArgumentException("Chat type is too long");
        }
        return new ChatId(switch(parts[1].charAt(0)) {
            case 's' -> Type.SUPER;
            case 'b' -> Type.BASIC;
            case 'u' -> Type.PRIVATE;
            case 'd' -> Type.SECRET;
            default -> throw new IllegalStateException("Unexpected value: " + parts[1].charAt(0));
        }, Long.parseUnsignedLong(parts[0]) & SUB_ID_MASK);
    }

    public static Long stringToLong(String chatId) {
        var parts = chatId.split("-", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Malformed chat id");
        }
        if (parts[1].length() != 1) {
            throw new IllegalArgumentException("Chat type is too long");
        }
        return switch(parts[1].charAt(0)) {
            case 's' -> TYPE_SUPER_LONG;
            case 'b' -> TYPE_BASIC_LONG;
            case 'u' -> TYPE_PRIVATE_LONG;
            case 'd' -> TYPE_SECRET_LONG;
            default -> throw new IllegalStateException("Unexpected value: " + parts[1].charAt(0));
        } | (Long.parseUnsignedLong(parts[0]) & SUB_ID_MASK);
    }
}
