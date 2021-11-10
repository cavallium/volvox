package io.volvox.td;

import it.tdlight.jni.TdApi;

public class TdObject {

    private final TdApi.Object object;

    public TdObject(TdApi.Object object) {
        this.object = object;
    }

    public <T extends TdApi.Object> T getObject() {
        //noinspection unchecked
        return (T) object;
    }
}
