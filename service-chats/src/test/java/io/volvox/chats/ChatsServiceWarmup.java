package io.volvox.chats;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.logging.Log;
import io.reactiverse.elasticsearch.client.mutiny.RestHighLevelClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.collect.List;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;

@ApplicationScoped
public class ChatsServiceWarmup {
	@Inject
	ChatService chatService;
	@Inject
	RestHighLevelClient restHighLevelClient;

	public void warmup() {
		try {
			resetDb();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		createIndices();
		chatService.listAll().onItem().transformToUni(this::updateIndex).merge().collect().last().await()
			.indefinitely();

		restHighLevelClient.indices().flushAsyncAndAwait(new FlushRequest("chats").force(true).waitIfOngoing(true), RequestOptions.DEFAULT);
		restHighLevelClient.indices().refreshAsyncAndAwait(new RefreshRequest("chats"), RequestOptions.DEFAULT);
		restHighLevelClient.indices().clearCacheAsyncAndAwait(new ClearIndicesCacheRequest("chats"), RequestOptions.DEFAULT);
	}

	private void resetDb() throws IOException {
		var db = new String(Objects
			.requireNonNull(ChatsServiceWarmup.class.getResourceAsStream("/import.sql"), "Cannot find import.sql")
			.readAllBytes());
		Panache.getSession().flatMap(sess -> Multi.createFrom().iterable(List.of(db.split("\n")))
				.filter(s -> !s.isBlank() && !s.startsWith("#"))
				.onItem().transformToUni(query -> sess.createNativeQuery(query).executeUpdate()).merge().collect().last()).await()
			.indefinitely();
	}

	private void createIndices() {
		try {
			var req = new DeleteIndexRequest("chats");
			restHighLevelClient.indices().deleteAsyncAndAwait(req, RequestOptions.DEFAULT);
		} catch (ElasticsearchStatusException ignored) {}
		try {
			var req = new CreateIndexRequest("chats");
			restHighLevelClient.indices().createAsyncAndAwait(req, RequestOptions.DEFAULT);
		} catch (ElasticsearchStatusException ex) {
			if (ex.status() != RestStatus.BAD_REQUEST) {
				throw ex;
			}
		}
	}

	private Uni<UpdateResponse> updateIndex(Chat chat) {
		var request = new UpdateRequest("chats", ChatId.toString(chat.id)).docAsUpsert(true);
		request.doc(JsonObject.mapFrom(chat).toString(), XContentType.JSON);

		Log.infof("Index chat %s", chat);

		return restHighLevelClient.updateAsync(request, RequestOptions.DEFAULT).onItem().invoke(response -> {
			if (response.status() != RestStatus.CREATED && response.status() != RestStatus.OK) {
				throw new UnsupportedOperationException("Unexpected status: " + response.status().toString());
			}
		});
	}
}
