package io.volvox.chats;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.reactiverse.elasticsearch.client.mutiny.RestHighLevelClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

@ApplicationScoped
public class ChatService {
    @Inject
    RestHighLevelClient restHighLevelClient;

	private Uni<IndexResponse> updateIndex(Chat chat) {
		var request = new IndexRequest("chats");
		request.id(ChatId.toString(chat.id));
		request.source(JsonObject.mapFrom(chat).toString(), XContentType.JSON);
		return restHighLevelClient.indexAsync(request, RequestOptions.DEFAULT);
	}

	private Uni<DeleteResponse> removeFromIndex(Long id) {
		var request = new DeleteRequest("chats");
		request.id(ChatId.toString(id));
		return restHighLevelClient.deleteAsync(request, RequestOptions.DEFAULT);
	}

	public Uni<Chat> get(Long id) {
		return Chat.findById(id);
	}

    public Uni<Chat> getFromIndex(Long id) {
        GetRequest getRequest = new GetRequest("chats", ChatId.toString(id));
        return restHighLevelClient.getAsync(getRequest, RequestOptions.DEFAULT)
                .map(getResponse -> {
                    if (getResponse.isExists()) {
                        String sourceAsString = getResponse.getSourceAsString();
                        JsonObject json = new JsonObject(sourceAsString);
                        return json.mapTo(Chat.class);
                    }
                    return null;
                });
    }

	public Uni<Void> create(Chat chat) {
		return Panache.withTransaction(() -> Chat.persist(chat).replaceWith(updateIndex(chat)))
			.replaceWithVoid();
	}

	public Uni<Void> delete(Long id) {
		return Panache.withTransaction(() -> Chat.findById(id)
			.onItem().ifNull().failWith(NotFoundException::new)
			.flatMap(PanacheEntityBase::delete)
			.replaceWith(removeFromIndex(id))
			.onItem().transform(DeleteResponse::status)
			.replaceWithVoid()
		);
	}

	public Uni<Chat> update(Long id, Chat chat) {
		if (chat.id != null && id != null && !Objects.equals(chat.id, id)) {
			throw new IllegalArgumentException("Chat id is different than id");
		}
		// Find chat by id
		return Panache.withTransaction(() -> Chat.<Chat>findById(id)
			.flatMap(entity -> {
				if (entity == null) {
					// Persist the chat if not found
					return Chat.persist(chat)
						// Return the chat
						.replaceWith(chat);
				} else {
					// Update all fields
					if (chat.name != null) {
						entity.name = chat.name;
					}
					if (chat.username != null) {
						entity.username = chat.username;
					}
					if (chat.status != null) {
						entity.status = chat.status;
					}
					// Return the updated item
					return Uni.createFrom().item(entity);
				}
			})
			// Update index
			.onItem().transformToUni(updatedChat -> updateIndex(updatedChat).replaceWith(updatedChat))
		);
	}

	public Uni<List<Chat>> searchByUsername(String username) {
		return search("username", username);
	}

	public Uni<List<Chat>> searchByName(String name) {
		return search("name", name);
	}

	public Uni<Chat> resolveByUsername(String username) {
		return Chat.findUsername(username);
	}

    private Uni<List<Chat>> search(String term, String match) {
        SearchRequest searchRequest = new SearchRequest("chats");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery(term, match));
        searchRequest.source(searchSourceBuilder);

        return restHighLevelClient.searchAsync(searchRequest, RequestOptions.DEFAULT)
                .map(searchResponse -> {
                    SearchHits hits = searchResponse.getHits();
                    List<Chat> results = new ArrayList<>(hits.getHits().length);
                    for (SearchHit hit : hits.getHits()) {
                        String sourceAsString = hit.getSourceAsString();
                        JsonObject json = new JsonObject(sourceAsString);
                        results.add(json.mapTo(Chat.class));
                    }
                    return results;
                });
    }

	public Multi<Chat> listAll() {
		return Chat.streamAll();
	}

	public Uni<Long> count() {
		return Chat.count();
	}
}
