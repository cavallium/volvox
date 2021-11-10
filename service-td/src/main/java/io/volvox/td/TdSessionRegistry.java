package io.volvox.td;

import io.vertx.core.impl.ConcurrentHashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class TdSessionRegistry {

    private final Set<String> clients = new ConcurrentHashSet<>();

    @Produces
    public RandomUUID produceUUID() {
        var randomUUID = new RandomUUID();
        clients.add(randomUUID.uuid);
        return randomUUID;
    }

    public void cleanUUID(@Disposes RandomUUID toClean) {
        clients.remove(toClean.uuid);
    }

    public Set<String> getSessions() {
        return clients;
    }
}
