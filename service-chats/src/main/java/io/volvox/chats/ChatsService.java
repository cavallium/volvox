package io.volvox.chats;

import io.quarkus.vertx.ConsumeEvent;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ChatsService {

    @Inject
    EventBus bus;

    @Inject
    ChatResource chatResource;

    @ConsumeEvent(value = "chats.list")
    public void listChats(Message<Void> msg) {
        chatResource.listSessions().collect().asList().subscribe().with(msg::reply);
    }

    @ConsumeEvent(value = "chats.get")
    public void get(Message<Long> msg) {
        chatResource.get(msg.body()).subscribe().with(msg::reply);
    }

    @ConsumeEvent(value = "chats.update")
    public void update(Message<Chat> msg) {
        chatResource.update(msg.body().id, msg.body()).subscribe().with(msg::reply);
    }
}