package io.volvox.chats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(ElasticsearchContainerTestResource.class)
public class ChatServiceTest {

	@Inject ChatService chatService;

	@Inject ChatsServiceWarmup chatsServiceWarmup;

	@BeforeEach
	public void beforeEach() {
		chatsServiceWarmup.warmup();
	}

	@Test
	public void testCount() {
		var count = chatService.count().await().indefinitely();
		assertEquals(4, count);
	}

	@Test
	public void testListAll() {
		var list = chatService.listAll().map(Chat::getChatId).collect().asList().await().indefinitely();
		assertThat(list)
			.containsExactlyInAnyOrder(ChatId.fromLong(9007199256673076L), ChatId.fromLong(777000),
				ChatId.fromLong(4503599627464345L), ChatId.fromLong(4503599627382355L)
			);
	}

	@Test
	public void testSearchByName() {
		var chat = chatService.searchByName("Telegram").await().indefinitely();
		assertThat(chat.size()).isEqualTo(1L);
		assertThat(chat.get(0).getChatId()).isEqualTo(ChatId.fromLong(777000L));
	}

	@Test
	public void testSearchByUsername() {
		var chat = chatService.searchByUsername("telegram").await().indefinitely();
		assertThat(chat.size()).isEqualTo(1L);
		assertThat(chat.get(0).getChatId()).isEqualTo(ChatId.fromLong(777000));
	}

	@Test
	public void testCreateNonexistent() {
		var newChat = new Chat();
		newChat.id = 777234L;
		newChat.name = "TestChat";
		newChat.username = "uname";
		newChat.status = Status.ALIVE;
		chatService.create(newChat).await().indefinitely();
		Panache.getSession().onItem().invoke(() -> {
			assertThat(newChat.isPersistent()).isEqualTo(true);
			assertThat(newChat.id).isEqualTo(777234L);
			assertThat(newChat.name).isEqualTo("TestChat");
			assertThat(newChat.username).isEqualTo("uname");
			assertThat(newChat.status).isEqualTo(Status.ALIVE);
		}).await().indefinitely();
	}

	@Test
	public void testUpdateNonexistent() {
		var chat = new Chat();
		chat.id = 777234L;
		chat.name = "TestChat";
		chat.username = "uname";
		chat.status = Status.ALIVE;
		var newChat = chatService.update(777234L, chat).await().indefinitely();
		Panache.getSession().onItem().invoke(() -> {
			assertThat(newChat).isNotNull();
			assertThat(newChat.isPersistent()).isEqualTo(true);
			assertThat(newChat.id).isEqualTo(777234L);
			assertThat(newChat.name).isEqualTo("TestChat");
			assertThat(newChat.username).isEqualTo("uname");
			assertThat(newChat.status).isEqualTo(Status.ALIVE);
		}).await().indefinitely();
	}

	@Test
	public void testUpdateExisting() {
		// Create chat
		{
			var chat = new Chat();
			chat.id = 777234L;
			chat.name = "TestChat";
			chat.username = "uname";
			chat.status = Status.ALIVE;
			chatService.create(chat).await().indefinitely();
		}
		// Test update
		{
			var chat = new Chat();
			chat.id = 777234L;
			chat.username = "mario";
			var newChat = chatService.update(777234L, chat).await().indefinitely();
			Panache.getSession().onItem().invoke(() -> {
				assertThat(newChat).isNotNull();
				assertThat(newChat.isPersistent()).isEqualTo(true);
				assertThat(newChat.id).isEqualTo(777234L);
				assertThat(newChat.name).isEqualTo("TestChat");
				assertThat(newChat.username).isEqualTo("mario");
				assertThat(newChat.status).isEqualTo(Status.ALIVE);
			}).await().indefinitely();
		}
	}

	@BeforeEach
	public void tearDown(){
		Panache.withTransaction(() -> Chat.deleteById(777234L)).await().indefinitely();
	}
}
