package io.volvox.td;

import io.smallrye.mutiny.Uni;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.Function;
import it.tdlight.jni.TdApi.Object;
import java.io.InputStream;
import java.util.NoSuchElementException;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/td/session/{sessionId}")
public class TdSessionResource {

    @PathParam("sessionId")
    String sessionId;

    @Inject
    TdService tdService;

    @Inject
    TdObjectJsonSerializer tdObjectJsonSerializer;

    private TdClient client() {
        return tdService.get(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + sessionId));
    }

    @SuppressWarnings("unchecked")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/send")
    public <T extends Object> Uni<String> sendRest(InputStream functionJson) {
        TdApi.Function<T> requestFunction = (Function<T>) tdObjectJsonSerializer.deserialize(functionJson);
        return client().send(requestFunction).map(response -> tdObjectJsonSerializer.serialize(response));
    }

    @SuppressWarnings("unchecked")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/execute")
    public <T extends Object> Uni<String> executeRest(InputStream functionJson) {
        TdApi.Function<T> requestFunction = (Function<T>) tdObjectJsonSerializer.deserialize(functionJson);
        return client().execute(requestFunction).map(response -> tdObjectJsonSerializer.serialize(response));
    }
}
