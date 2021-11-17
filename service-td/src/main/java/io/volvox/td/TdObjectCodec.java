package io.volvox.td;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.Deserializer;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TdObjectCodec implements MessageCodec<TdObjectCodec.TdObject, TdObjectCodec.TdObject> {

    @Override public void encodeToWire(Buffer buffer, TdObject t) {
        BufferUtils.encode(buffer, out -> t.getObject().serialize(out));
    }

    @Override
    public TdObject decodeFromWire(int i, Buffer buffer) {
        return new TdObject(BufferUtils.decode(i, buffer, Deserializer::deserialize));
    }

    @Override public TdObject transform(TdObject t) {
        // If a message is sent *locally* across the event bus.
        // This sends message just as is
        return t;
    }

    @Override public String name() {
        return "TdObjectCodec";
    }

    @Override public byte systemCodecID() {
        // Always "-1"
        return -1;
    }

    public static class TdObject {

        private final TdApi.Object object;

        public TdObject(TdApi.Object object) {
            this.object = object;
        }

        public <T extends TdApi.Object> T getObject() {
            //noinspection unchecked
            return (T) object;
        }
    }
}
