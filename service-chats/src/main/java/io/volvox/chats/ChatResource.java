package io.volvox.chats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Typing;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.net.URI;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/chats")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@JsonSerialize(using = ChatIdLongJsonSerializer.class, as = ChatId.class, typing = Typing.STATIC)
@JsonDeserialize(using = ChatIdLongJsonDeserializer.class, as = ChatId.class)
public class ChatResource {

	@Inject
	ChatService chatService;

    @GET
    public Multi<Chat> list() {
        return chatService.listAll();
    }

    @GET
    @Path("/{id}")
    public Uni<Chat> get(@PathParam("id") ChatId id) {
        return chatService.get(id.toLong());
    }

    @POST
    public Uni<Response> create(Chat chat) {
        return chatService.create(chat)
                .onItem().transform(inserted -> Response.created(URI.create("/chats/" + chat.id)).build());
    }

    @PUT
    @Path("/{id}")
    public Uni<Chat> update(@PathParam("id") ChatId id, Chat chat) {
		return chatService.update(id.toLong(), chat);
    }

    @DELETE
    @Path("/{id}")
    public Uni<Void> delete(@PathParam("id") ChatId id) {
		return chatService.delete(id.toLong());
    }

    @GET
    @Path("/by-username/{username}")
    public Uni<Chat> resolveByUsername(@PathParam("username") String username) {
		return chatService.resolveByUsername(username);
    }

    @GET
    @Path("/count")
    public Uni<Long> count() {
        return chatService.count();
    }
}
