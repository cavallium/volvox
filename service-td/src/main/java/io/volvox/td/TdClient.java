package io.volvox.td;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.Update;

public interface TdClient {
    Multi<Update> updates();

    <T extends TdApi.Object> Uni<T> send(TdApi.Function<T> function);

    <T extends TdApi.Object> Uni<T> execute(TdApi.Function<T> function);

    void dispose();
}
