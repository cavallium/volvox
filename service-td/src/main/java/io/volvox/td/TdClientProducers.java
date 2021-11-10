package io.volvox.td;

import io.quarkus.vertx.runtime.VertxProducer;
import it.tdlight.common.Init;
import it.tdlight.common.ReactiveTelegramClient;
import it.tdlight.common.utils.CantLoadLibrary;
import it.tdlight.tdlight.ClientManager;
import java.time.Duration;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
class TdClientProducers {

    private static final Logger LOGGER = Logger.getLogger(VertxProducer.class);

    @ConfigProperty(name = "td.requests.timeout")
    Duration requestTimeout;

    @Produces
    ReactiveTelegramClient produceReactiveTelegramClient() {
        try {
            Init.start();
        } catch (CantLoadLibrary e) {
            LOGGER.error(e);
        }
        return ClientManager.createReactive();
    }

    @Produces
    @Singleton
    public Duration produceRequestTimeout() {
        return requestTimeout;
    }
}
