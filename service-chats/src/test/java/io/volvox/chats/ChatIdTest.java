package io.volvox.chats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.volvox.chats.ChatId.Type;
import org.junit.jupiter.api.Test;

public class ChatIdTest {

    @Test
    public void newChatId() {
        assertEquals(Type.BASIC, new ChatId(Type.BASIC, 10).type());
        assertEquals(Type.PRIVATE, new ChatId(Type.PRIVATE, 10).type());
        assertEquals(Type.SECRET, new ChatId(Type.SECRET, 10).type());
        assertEquals(Type.SUPER, new ChatId(Type.SUPER, 10).type());

        assertEquals(1, new ChatId(Type.BASIC, 1).subId());
        assertEquals(2, new ChatId(Type.PRIVATE, 2).subId());
        assertEquals(3, new ChatId(Type.SECRET, 3).subId());
        assertEquals(4, new ChatId(Type.SUPER, 4).subId());
    }

    @Test
    public void toLong() {
        assertEquals(777000, new ChatId(Type.PRIVATE, 777000).toLong());
        assertEquals(0b01L << 52 | 777000, new ChatId(Type.BASIC, 777000).toLong());
        assertEquals(0b10L << 52 | 777000, new ChatId(Type.SUPER, 777000).toLong());
        assertEquals(0b11L << 52 | 777000, new ChatId(Type.SECRET, 777000).toLong());
    }

    @Test
    public void fromLong() {
        assertEquals(new ChatId(Type.PRIVATE, 777000), ChatId.fromLong(777000));
        assertEquals(new ChatId(Type.BASIC, 777000), ChatId.fromLong(0b01L << 52 | 777000));
        assertEquals(new ChatId(Type.SUPER, 777000), ChatId.fromLong(0b10L << 52 | 777000));
        assertEquals(new ChatId(Type.SECRET, 777000), ChatId.fromLong(0b11L << 52 | 777000));
    }

    @Test
    public void fromString() {
        assertEquals(new ChatId(Type.PRIVATE, 777000), ChatId.fromString("777000-u"));
        assertEquals(new ChatId(Type.BASIC, 777000), ChatId.fromString("777000-b"));
        assertEquals(new ChatId(Type.SUPER, 777000), ChatId.fromString("777000-s"));
        assertEquals(new ChatId(Type.SECRET, 777000), ChatId.fromString("777000-d"));
    }

    @Test
    public void testToString() {
        assertEquals("777000-u", new ChatId(Type.PRIVATE, 777000).toString());
        assertEquals("777000-b", new ChatId(Type.BASIC, 777000).toString());
        assertEquals("777000-s", new ChatId(Type.SUPER, 777000).toString());
        assertEquals("777000-d", new ChatId(Type.SECRET, 777000).toString());
    }

    @Test
    public void longToString() {
        assertEquals("777000-u", ChatId.toString(777000));
        assertEquals("777000-b", ChatId.toString(0b01L << 52 | 777000));
        assertEquals("777000-s", ChatId.toString(0b10L << 52 | 777000));
        assertEquals("777000-d", ChatId.toString(0b11L << 52 | 777000));
    }

    @Test
    public void stringToLong() {
        assertEquals(777000, ChatId.stringToLong("777000-u"));
        assertEquals(0b01L << 52 | 777000, ChatId.stringToLong("777000-b"));
        assertEquals(0b10L << 52 | 777000, ChatId.stringToLong("777000-s"));
        assertEquals(0b11L << 52 | 777000, ChatId.stringToLong("777000-d"));
    }
}
