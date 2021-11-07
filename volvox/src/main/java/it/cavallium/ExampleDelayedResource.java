package it.cavallium;

import io.smallrye.mutiny.Uni;
import java.time.Duration;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/delay")
public class ExampleDelayedResource {

	@Path("{seconds:\\d+}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Uni<String> delay(int seconds) {
		return Uni

				// Create the response item
				.createFrom().item("Hello from the future! %d seconds have passed".formatted(seconds))

				// Delay the response by n seconds
				.onItem().delayIt().by(Duration.ofSeconds(seconds));
	}
}