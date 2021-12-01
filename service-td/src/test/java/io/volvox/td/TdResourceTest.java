package io.volvox.td;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;

import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TdResourceTest {

	@Inject TdService tdService;

    @Test
    public void testEmptyList() {
        given()
                .when().get("/td/list")
                .then()
                .statusCode(200)
                .body(is(""));
    }

    @Test
    public void testCreateSession() {
        given()
                .when().get("/td/create-session")
                .then()
                .statusCode(200)
                .body(not(emptyOrNullString()));
    }

    @Test
    public void testCreateMultipleSessions() {
        var sessionId1 = given()
                .when().get("/td/create-session")
                .then()
                .statusCode(200)
                .body(not(emptyOrNullString()))
                .extract()
                .body()
                .asString();
        var sessionId2 = given()
                .when().get("/td/create-session")
                .then()
                .statusCode(200)
                .body(not(emptyOrNullString()))
                .extract()
                .body()
                .asString();

		var bodyElems = given()
			.when().get("/td/list")
			.then()
			.statusCode(200)
			.extract()
			.body()
			.asString()
			.split("\n");

		assertThat(bodyElems).containsExactlyInAnyOrder(sessionId1, sessionId2);
    }

	@BeforeEach
	@AfterEach
	public void resetTdServices() {
		tdService.disposeAll();
	}

}
