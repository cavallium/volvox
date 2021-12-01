package io.volvox.chats;

import java.util.Objects;

public final class ChatId {
	private final Type type;
	private final long subId;

	public ChatId(Type type, long subId) {
		if ((subId & SUB_ID_MASK) != subId) {
			throw new IllegalArgumentException("subId is too big");
		}
		this.type = type;
		this.subId = subId;
	}

	public Type type() {
		return type;
	}

	public long subId() {
		return subId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		var that = (ChatId) obj;
		return Objects.equals(this.type, that.type) &&
			this.subId == that.subId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, subId);
	}

	public static final int SUB_ID_MASK_BYTES = 52;
	public static final int TYPE_MASK_BYTES = 2;

	public static final long SUB_ID_MASK = 0b001111111111111111111111111111111111111111111111111111L;
	public static final long TYPE_MASK = 0b11L << SUB_ID_MASK_BYTES;
	public static final long MASK = SUB_ID_MASK | TYPE_MASK;
	public static final int TYPE_PRIVATE_INT = 0b00;
	public static final int TYPE_BASIC_INT = 0b01;
	public static final int TYPE_SUPER_INT = 0b10;
	public static final int TYPE_SECRET_INT = 0b11;
	public static final long TYPE_PRIVATE_LONG = 0;
	public static final long TYPE_BASIC_LONG = 0b01L << SUB_ID_MASK_BYTES & TYPE_MASK;
	public static final long TYPE_SUPER_LONG = 0b10L << SUB_ID_MASK_BYTES & TYPE_MASK;
	public static final long TYPE_SECRET_LONG = 0b11L << SUB_ID_MASK_BYTES & TYPE_MASK;

	public static ChatId fromLong(long id) {
		return new ChatId(getType(id), getIdLong(id));
	}

	private static Type getType(long id) {
		switch ((int) ((id & TYPE_MASK) >> SUB_ID_MASK_BYTES)) {
			case TYPE_SUPER_INT:
				return Type.SUPER;
			case TYPE_BASIC_INT:
				return Type.BASIC;
			case TYPE_PRIVATE_INT:
				return Type.PRIVATE;
			case TYPE_SECRET_INT:
				return Type.SECRET;
			default:
				throw new IllegalArgumentException("Invalid id type: " + id);
		}
	}

	private static long getIdLong(long id) {
		return id & SUB_ID_MASK;
	}

	public long toLong() {
		long result;
		switch (type) {
			case SUPER:
				result = TYPE_SUPER_LONG;
				break;
			case BASIC:
				result = TYPE_BASIC_LONG;
				break;
			case PRIVATE:
				result = TYPE_PRIVATE_LONG;
				break;
			case SECRET:
				result = TYPE_SECRET_LONG;
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + type);
		}
		result |= (subId & SUB_ID_MASK);
		return result;
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
		char suffix;
		switch (chatId.type) {
			case SUPER:
				suffix = 's';
				break;
			case BASIC:
				suffix = 'b';
				break;
			case PRIVATE:
				suffix = 'u';
				break;
			case SECRET:
				suffix = 'd';
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + chatId.type);
		}
		return Long.toUnsignedString(chatId.subId) + "-" + suffix;
	}

	public static String toString(long chatId) {
		char suffix;
		switch (getType(chatId)) {
			case SUPER:
				suffix = 's';
				break;
			case BASIC:
				suffix = 'b';
				break;
			case PRIVATE:
				suffix = 'u';
				break;
			case SECRET:
				suffix = 'd';
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + chatId);
		}
		return Long.toUnsignedString(getIdLong(chatId)) + "-" + suffix;
	}

	public static ChatId fromString(String chatId) {
		var parts = chatId.split("-", 2);
		if (parts.length != 2) {
			throw new IllegalArgumentException("Malformed chat id");
		}
		if (parts[1].length() != 1) {
			throw new IllegalArgumentException("Chat type is too long");
		}

		Type type;
		switch (parts[1].charAt(0)) {
			case 's':
				type = Type.SUPER;
				break;
			case 'b':
				type = Type.BASIC;
				break;
			case 'u':
				type = Type.PRIVATE;
				break;
			case 'd':
				type = Type.SECRET;
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + parts[1].charAt(0));
		}

		return new ChatId(type, Long.parseUnsignedLong(parts[0]) & SUB_ID_MASK);
	}

	public static Long stringToLong(String chatId) {
		var parts = chatId.split("-", 2);
		if (parts.length != 2) {
			throw new IllegalArgumentException("Malformed chat id");
		}
		if (parts[1].length() != 1) {
			throw new IllegalArgumentException("Chat type is too long");
		}
		long result;
		switch (parts[1].charAt(0)) {
			case 's':
				result = TYPE_SUPER_LONG;
				break;
			case 'b':
				result = TYPE_BASIC_LONG;
				break;
			case 'u':
				result = TYPE_PRIVATE_LONG;
				break;
			case 'd':
				result = TYPE_SECRET_LONG;
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + parts[1].charAt(0));
		}
		result |= (Long.parseUnsignedLong(parts[0]) & SUB_ID_MASK);
		return result;
	}
}
