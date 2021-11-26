package io.volvox.chats;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.net.URI;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/chats")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatResource {

    @Inject
    ChatRepository chatRepository;

    @GET
    public Multi<Chat> listSessions() {
        return chatRepository.streamAll();
    }

    @GET
    @Path("/{id}")
    public Uni<Chat> get(@PathParam("id") String id) {
        return chatRepository.findById(id);
    }

    @POST
    @Transactional
    public Uni<Response> create(Chat chat) {
        return chatRepository.persist(chat)
                // Return success
                .replaceWith(() -> Response.created(URI.create("/api/" + chat.id)).build());
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Uni<Chat> update(@PathParam("id") String id, Chat chat) {
        // Find chat by id
        return chatRepository.<Chat>findById(id)
                .flatMap(entity -> {
                    if (entity == null) {
                        // Persist the chat if not found
                        return chatRepository.persist(chat);
                    } else {
                        // Update all fields
                        entity.name = chat.name;
                        // Return the updated item
                        return Uni.createFrom().item(entity);
                    }
                });
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Uni<Void> delete(@PathParam("id") String id) {
        return chatRepository.findById(id)
                .onItem().ifNull().failWith(NotFoundException::new)
                .flatMap(PanacheEntityBase::delete);
    }

    @GET
    @Path("/search/{username}")
    public Uni<Chat> search(@PathParam("username") String username) {
        return chatRepository.findByUsername(username);
    }

    @GET
    @Path("/count")
    public Uni<Long> count() {
        return chatRepository.count();
    }
}
