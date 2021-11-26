package io.volvox.chats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import java.util.StringJoiner;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

@Entity
@Cacheable
public class Chat extends PanacheEntityBase {

    @Id
    @Positive(message = "id is not positive")
    @Max(message = "id is too big", value = ChatId.MASK)
    @Column(nullable = false, unique = true)
    @JsonSerialize(using = ChatIdJsonSerializer.class)
    @JsonDeserialize(using = ChatIdJsonDeserializer.class)
    public Long id;
    @Column(length = 128)
    public String name;
    @Length(message = "Username length is not valid", min = 5)
    @Column(length = 48)
    @Length(message = "Username must not be an empty string", min = 1, max = 12 + 32)
    @Pattern(message = "Username contains invalid characters", regexp = "^(?:[a-zA-Z\\d][_]?)+$")
    @Pattern(message = "Username is not valid", regexp = "^(?:translation_|mv_)?[a-zA-Z]([a-zA-Z_\\d]){1,30}[a-zA-Z\\d]$")
    public String username;
    @Enumerated
    public Status status;

    @JsonIgnore
    public ChatId getChatId() {
        return ChatId.fromLong(id);
    }

    @JsonIgnore
    public void setChatId(ChatId id) {
        this.id = id.toLong();
    }

    @Override public String toString() {
        return new StringJoiner(", ", Chat.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("username='" + username + "'")
                .add("status=" + status)
                .toString();
    }
}
