package io.volvox.td;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TdEventBusTest {

    @Inject
    EventBus bus;

    @Test
    public void testStartSession() {
        Uni<String> uni = bus.<String>request("td.start-session", null).map(Message::body);

        UniAssertSubscriber<String> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());

        String item = subscriber.awaitItem().assertCompleted().getItem();

        Assertions.assertNotNull(item);
        Assertions.assertDoesNotThrow(() -> UUID.fromString(item));
    }
}
