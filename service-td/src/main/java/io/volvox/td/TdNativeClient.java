package io.volvox.td;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.tdlight.common.ReactiveTelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.Error;
import it.tdlight.jni.TdApi.Update;
import java.time.Duration;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public class TdNativeClient implements TdClient {

    @Inject
    ReactiveTelegramClient client;

    @ConfigProperty(name = "td.requests.timeout")
    Duration requestTimeout;

    private Multi<Update> updates;

    @PostConstruct
    void init() {
        this.updates = Multi.createFrom().<Update>emitter(emitter -> {
            client.createAndRegisterClient();
            client.setListener(signal -> {
                if (signal.isClosed()) {
                    emitter.complete();
                } else if (signal.isUpdate()) {
                    var update = (TdApi.Update) signal.getUpdate();
                    emitter.emit(update);
                } else if (signal.isException()) {
                    emitter.fail(signal.getException());
                } else {
                    throw new IllegalStateException("Unknown signal: " + signal);
                }
            });
            emitter.onTermination(client::dispose);
        }).broadcast().toAllSubscribers();
    }

    @Override
    public Multi<TdApi.Update> updates() {
        return updates;
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends TdApi.Object> Uni<T> send(TdApi.Function<T> function) {
        return (Uni<T>) Uni
                .createFrom()
                .publisher(client.send(function, requestTimeout))
                .onItem()
                .transformToUni(item -> {
                    if (item.getConstructor() == Error.CONSTRUCTOR) {
                        TdApi.Error error = (TdApi.Error) item;
                        return Uni.createFrom().failure(new TdException(error.code, error.message));
                    } else {
                        return Uni.createFrom().item(item);
                    }
                });
    }

    @Override @SuppressWarnings("unchecked")
    public <T extends TdApi.Object> Uni<T> execute(TdApi.Function<T> function) {
        return (Uni<T>) Uni
                .createFrom()
                .item(() -> client.execute(function))
                .onItem()
                .transformToUni(item -> {
                    if (item.getConstructor() == Error.CONSTRUCTOR) {
                        TdApi.Error error = (TdApi.Error) item;
                        return Uni.createFrom().failure(new TdException(error.code, error.message));
                    } else {
                        return Uni.createFrom().item(item);
                    }
                });
    }

    @Override
    @PreDestroy
    public void dispose() {
        this.client.dispose();
    }
}
