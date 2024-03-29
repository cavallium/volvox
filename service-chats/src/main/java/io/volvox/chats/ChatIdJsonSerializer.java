package io.volvox.chats;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class ChatIdJsonSerializer extends JsonSerializer<ChatId> {

    @Override
    public void serialize(ChatId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(ChatId.toString(value));
    }
}
