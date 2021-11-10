package io.volvox.td;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import it.tdlight.jni.TdApi.Function;
import it.tdlight.jni.TdApi.Object;
import it.tdlight.jni.TdApi.Update;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class TdSession implements TdClient {

    private static final DeliveryOptions UPDATES_OPTS
            = new DeliveryOptions().setCodecName("TdObjectCodec");
    private static final DeliveryOptions SEND_OPTS
            = new DeliveryOptions().setCodecName("TdObjectCodec");

    @Inject
    RandomUUID uuid;

    @Inject
    Executor executor;

    @Inject
    TdNativeClient client;

    @Inject
    EventBus bus;

    private final AtomicReference<Cancellable> updatesPublisher = new AtomicReference<>();

    @Override public Multi<Update> updates() {
        return client.updates();
    }

    public void publishUpdates() {
        var newPublisher = this.updates()
                .runSubscriptionOn(executor)
                .subscribe()
                .with(item -> bus.publish("td.update", item, UPDATES_OPTS));
        var prev = this.updatesPublisher.getAndSet(newPublisher);
        if (prev != null) {
            throw new IllegalStateException("Called publishUpdates twice!");
        }
    }

    @ConsumeEvent(value = "td.send", codec = TdObjectCodec.class)
    void onSendRequest(Message<TdObject> msg) {
        this.send(msg.body().getObject()).subscribe()
                .with(message -> msg.reply(message, SEND_OPTS),
                        ex -> {
                            if (ex instanceof TdException tdException) {
                                msg.fail(tdException.getCode(), tdException.getMessage());
                            } else {
                                msg.fail(500, ex.toString());
                            }
                        }
                );
    }

    @Override public <T extends Object> Uni<T> send(Function<T> function) {
        return client.send(function);
    }

    @SuppressWarnings("unchecked") @Override public <T extends Object> Uni<T> execute(Function<T> function) {
        return null;
    }

    @Override public void dispose() {
        var updatesPublisher = this.updatesPublisher.get();
        if (updatesPublisher != null) {
            updatesPublisher.cancel();
        }
        client.dispose();
    }
}
