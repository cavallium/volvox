package it.cavallium;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ExampleDelayResourceTest {

	@Test
	public void testHelloEndpoint() {
		given()
				.when()
				.get("/delay?duration=1")
				.then()
				.statusCode(200)
				.body(is("Hello from the future! %d seconds have passed".formatted(1)));
	}

}