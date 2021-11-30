package io.volvox.chats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.reactiverse.elasticsearch.client.mutiny.RestHighLevelClient;
import javax.inject.Inject;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.rest.RestStatus;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(ElasticsearchContainerTestResource.class)
public class IndexWarmupTest {
	@Inject
	RestHighLevelClient restHighLevelClient;

	@Inject ChatsServiceWarmup chatsServiceWarmup;

	@Test
	public void test() {
		chatsServiceWarmup.warmup();
		var count = restHighLevelClient.countAsyncAndAwait(new CountRequest("chats"), RequestOptions.DEFAULT);
		assertEquals(RestStatus.OK, count.status());
		assertEquals(4, count.getCount());
	}
}
