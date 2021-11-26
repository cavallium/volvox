package io.volvox.td;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/td")
public class TdResource {

    @Inject TdService tdService;

    @Path("/list")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String listSessions() {
        StringBuilder sb = new StringBuilder();
        for (var session : tdService.getSessions()) {
            sb.append(session.getKey()).append(System.lineSeparator());
        }
        return sb.toString();
    }

    @Path("/create-session")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String createSession() {
        return tdService.startSession();
    }
}
