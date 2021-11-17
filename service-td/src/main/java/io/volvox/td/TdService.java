package io.volvox.td;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class TdService {

    private final ConcurrentMap<String, TdClient> clients = new ConcurrentHashMap<>();

    @Inject
    EventBus bus;

    @Inject
    Instance<TdEventBusClient> sessionInstances;

    @ConsumeEvent(value = "td.start-session")
    public void onStartSession(Message<String> msg) {
        var sessionId = this.startSession();
        msg.reply(sessionId);
    }

    public String startSession() {
        String uuid = generateRandomUUID();

        var client = sessionInstances.get();
        clients.put(uuid, client);

        return uuid;
    }

    private String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }

    void shutdown(@Observes ShutdownEvent event) {
        clients.forEach((uuid, client) -> client.dispose());
    }

    public Optional<TdClient> get(String uuid) {
        if (uuid == null) return Optional.empty();
        return Optional.ofNullable(clients.get(uuid));
    }

    public Optional<String> get(TdClient client) {
        if (client == null) return Optional.empty();
        return clients.entrySet().stream().filter(e -> e.getValue() == client).map(Entry::getKey).findAny();
    }

    public Set<Entry<String, TdClient>> getSessions() {
        return clients.entrySet();
    }
}