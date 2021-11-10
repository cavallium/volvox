package io.volvox.td;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@ApplicationScoped
public class ExecutorProducer {

    @Produces
    @Singleton
    public Executor produceExecutor() {
        return Executors.newCachedThreadPool();
    }
}
