package io.volvox.td;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.specification.Argument;
import java.util.List;
import java.util.Set;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsNot;
import org.hamcrest.text.IsEmptyString;
import org.hamcrest.text.MatchesPattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;

@QuarkusTest
public class TdResourceTest {

    @Test
    public void testEmptyList() {
        given()
                .when().get("/api/td/list")
                .then()
                .statusCode(200)
                .body(is(""));
    }

    @Test
    public void testCreateSession() {
        given()
                .when().get("/api/td/create-session")
                .then()
                .statusCode(200)
                .body(not(emptyOrNullString()));
    }

    @Test
    public void testCreateMultipleSessions() {
        var sessionId1 = given()
                .when().get("/api/td/create-session")
                .then()
                .statusCode(200)
                .body(not(emptyOrNullString()))
                .extract()
                .body()
                .asString();
        var sessionId2 = given()
                .when().get("/api/td/create-session")
                .then()
                .statusCode(200)
                .body(not(emptyOrNullString()))
                .extract()
                .body()
                .asString();

        var expectedBodyElems = Set.of(sessionId1, sessionId2);

        var bodyElems = Set.of(given()
                .when().get("/api/td/list")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString()
                .split("\n"));

        Assertions.assertEquals(expectedBodyElems, bodyElems);
    }

}