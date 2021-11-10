package io.volvox.td;

import java.util.UUID;

public class RandomUUID {

    final String uuid;

    public RandomUUID() {
        this.uuid = UUID.randomUUID().toString();
    }

}
