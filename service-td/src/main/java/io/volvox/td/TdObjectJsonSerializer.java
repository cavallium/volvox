package io.volvox.td;

import it.tdlight.jni.TdApi;
import java.io.InputStream;

public interface TdObjectJsonSerializer {

    TdApi.Object deserialize(InputStream json);

    String serialize(TdApi.Object object);
}
