package io.volvox.td;

import it.tdlight.common.Init;
import it.tdlight.common.ReactiveTelegramClient;
import it.tdlight.common.utils.CantLoadLibrary;
import it.tdlight.tdlight.ClientManager;
import javax.enterprise.inject.Produces;
import org.jboss.logging.Logger;

class ReactiveTelegramClientProducer {

    private static final Logger LOGGER = Logger.getLogger(ReactiveTelegramClient.class);

    @Produces
    static ReactiveTelegramClient produceNativeClient() {
        LOGGER.debug("Producing native client");
        try {
            Init.start();
        } catch (CantLoadLibrary e) {
            LOGGER.error("Failed to start native library", e);
            throw new RuntimeException(e);
        }
        return ClientManager.createReactive();
    }
}
