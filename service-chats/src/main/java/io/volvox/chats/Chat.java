package io.volvox.chats;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Chat extends PanacheEntityBase {
    @Id
    public String id;

    public String name;
    public String username;
    public Status status;

    public ChatId getChatId() {
        return new ChatId(id);
    }

    public void setChatId(ChatId id) {
        this.id = id.toString();
    }
}
