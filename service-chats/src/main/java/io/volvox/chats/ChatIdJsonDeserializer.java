package io.volvox.chats;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class ChatIdJsonDeserializer extends JsonDeserializer<ChatId> {

    @Override
    public ChatId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var id = p.readValueAs(String.class);
        return ChatId.fromString(id);
    }
}
