package io.volvox.chats;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(ElasticsearchContainerTestResource.class)
public class ChatsEndpointTest {

    @Test
    public void testListAllChats() {
        //List all, should have all 3 usernames the database has initially:
        Response response = given()
                .when()
                .get("/chats")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().response();
        assertThat(response.jsonPath().getList("name")).containsExactlyInAnyOrder("My Supergroup", "Telegram", "School group", "Old school group");

        // Update Telegram to Telegram Official
        given()
                .when()
                .body("{\"name\" : \"Telegram Official\"}")
                .contentType("application/json")
                .put("/chats/777000")
                .then()
                .statusCode(200)
                .body(
                        containsString("\"id\":"),
                        containsString("\"name\":\"Telegram Official\""));

        //List all, Telegram Official should've replaced Telegram:
        response = given()
                .when()
                .get("/chats")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().response();
        assertThat(response.jsonPath().getList("name"))
                .containsExactlyInAnyOrder("My Supergroup", "Telegram Official", "School group", "Old school group");

        //Delete Telegram:
        given()
                .when()
                .delete("/chats/777000")
                .then()
                .statusCode(204);

        response = given()
                .when()
                .get("/chats")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().response();
        assertThat(response.jsonPath().getList("name"))
                .containsExactlyInAnyOrder("My Supergroup", "School group", "Old school group");

        //Create Telegram2:
        given()
                .when()
                .body("{\"id\": \"777001-u\", \"name\" : \"Telegram2\"}")
                .contentType("application/json")
                .post("/chats")
                .then()
                .statusCode(201)
                .body(emptyString());

        //List all, Pineapple should be still missing now:
        response = given()
                .when()
                .get("/chats")
                .then()
                .statusCode(200)
                .extract().response();
        assertThat(response.jsonPath().getList("name"))
                .containsExactlyInAnyOrder("My Supergroup", "School group", "Old school group", "Telegram2");
    }

    @Test
    public void testEntityNotFoundForDelete() {
        given()
                .when()
                .delete("/chats/777123")
                .then()
                .statusCode(404)
                .body(emptyString());
    }

    @Test
    public void testEntityNotFoundForUpdate() {
        given()
                .when()
                .body("{\"id\": \"777234-u\", \"name\" : \"Juan\"}")
                .contentType("application/json")
                .put("/chats/777234")
                .then()
                .statusCode(200);
    }

    @Test
    public void testEntityWithoutIdForUpdate() {
        given()
                .when()
                .body("{\"name\" : \"Juan\"}")
                .contentType("application/json")
                .put("/chats/777234")
                .then()
                .statusCode(500);
    }

	@Test
	public void testHealth() {
		given()
			.when()
			.get("/q/health/ready")
			.then()
			.statusCode(200)
			.contentType("application/json")
			.body("status", is("UP"),
				"checks.status", containsInAnyOrder("UP"),
				"checks.name", containsInAnyOrder("Elasticsearch cluster health check"));
	}

    @BeforeEach
    public void tearDown(){
        Panache.withTransaction(() -> Chat.deleteById(777234L)).await().indefinitely();
    }
}
