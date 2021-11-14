package io.volvox.td;

import java.util.UUID;
import javax.enterprise.context.Dependent;

@Dependent
public class RandomUUID {

    final String uuid;

    public RandomUUID() {
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return uuid;
    }
}
