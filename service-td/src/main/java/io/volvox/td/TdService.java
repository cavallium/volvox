package io.volvox.td;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
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
    Instance<TdSession> sessionInstances;

    @ConsumeEvent(value = "td.start-session")
    public String startSession(Void param) {
        String uuid = generateRandomUUID();

        var client = sessionInstances.get();
        clients.put(uuid, client);
        client.publishUpdates();

        return uuid;
    }

    private String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }

    void shutdown(@Observes ShutdownEvent event) {
        clients.forEach((uuid, client) -> client.dispose());
    }
}