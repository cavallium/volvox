package io.volvox.chats;

import io.quarkus.logging.Log;
import io.reactiverse.elasticsearch.client.mutiny.RestHighLevelClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;

@ApplicationScoped
public class ChatsServiceWarmup {
	@Inject
	ChatService chatService;
	@Inject
	RestHighLevelClient restHighLevelClient;


	public void warmup() {
		createIndices();
		chatService.listAll().onItem().transformToUni(this::updateIndex).merge().select().last().toUni().await()
			.indefinitely();

		restHighLevelClient.indices().flushAsyncAndAwait(new FlushRequest("chats"), RequestOptions.DEFAULT);
		restHighLevelClient.indices().refreshAsyncAndAwait(new RefreshRequest("chats"), RequestOptions.DEFAULT);
	}

	private void createIndices() {
		var req = new CreateIndexRequest("chats");
		try {
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