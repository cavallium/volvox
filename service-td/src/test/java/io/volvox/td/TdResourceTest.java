package io.volvox.td;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TdResourceTest {

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

        var expectedBodyElems = Set.of(sessionId1, sessionId2);

        var bodyElems = Set.of(given()
                .when().get("/td/list")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString()
                .split("\n"));

        Assertions.assertEquals(expectedBodyElems, bodyElems);
    }

}