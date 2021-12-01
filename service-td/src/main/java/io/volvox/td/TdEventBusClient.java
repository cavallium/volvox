package io.volvox.td;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.Function;
import it.tdlight.jni.TdApi.Update;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

@Dependent
public class TdEventBusClient implements TdClient {

    @Inject
    TdNativeClient client;

    private static final ExecutorService EXECUTOR_SERVICE
            = Executors.newCachedThreadPool(new BasicThreadFactory.Builder().namingPattern("TdSession").build());

    private static final DeliveryOptions UPDATES_OPTS
            = new DeliveryOptions().setCodecName("TdObjectCodec");
    private static final DeliveryOptions SEND_OPTS
            = new DeliveryOptions().setCodecName("TdObjectCodec");

    @Inject
    EventBus bus;

    private final AtomicReference<Cancellable> updatesPublisher = new AtomicReference<>();

    @PostConstruct
    void init() {
        this.publishUpdates();
    }

    public void publishUpdates() {
        var newPublisher = this.updates()
                .runSubscriptionOn(EXECUTOR_SERVICE)
                .subscribe()
                .with(item -> bus.publish("td.update", item, UPDATES_OPTS));
        var prev = this.updatesPublisher.getAndSet(newPublisher);
        if (prev != null) {
            throw new IllegalStateException("Called publishUpdates twice!");
        }
    }

    @Override
    public Multi<Update> updates() {
        return client.updates();
    }

    @Override
    public <T extends TdApi.Object> Uni<T> send(Function<T> function) {
        return client.send(function);
    }

    @Override
    public <T extends TdApi.Object> Uni<T> execute(Function<T> function) {
        return client.execute(function);
    }

    @ConsumeEvent(value = "td.send", codec = TdObjectCodec.class)
    public void onSendRequest(Message<TdObjectCodec.TdObject> msg) {
        this.send(msg.body().getObject()).subscribe().with(message -> msg.reply(message, SEND_OPTS), ex -> {
            if (ex instanceof TelegramException) {
				TelegramException tdException = (TelegramException) ex;
                msg.fail(tdException.getCode(), tdException.getMessage());
            } else {
                msg.fail(500, ex.toString());
            }
        });
    }

    @Override
    @PreDestroy
    public void dispose() {
        var updatesPublisher = this.updatesPublisher.get();
        if (updatesPublisher != null) {
            updatesPublisher.cancel();
        }
        client.dispose();
    }
}
