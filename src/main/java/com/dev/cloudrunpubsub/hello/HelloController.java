package dev.cloudrunpubsub.hello;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping(path = "/api/v1/hello")
public class HelloController {

	@Value("${pubsub.bindings.output.destination}")
	private String cloudStreamOutputDestination;



	@Autowired
	private StreamBridge bridge;

	@ApiOperation(value = "Hello Message", notes = "Returns a hello greeting")
	@ApiProperty.Boolean(name = "x-42c-no-authentication", value = true)
	@ApiResponses({
			@ApiResponse(code = 200, message = "OK", response = HelloResponse.class),
			@ApiResponse(code = 0, message = "All Errors", response = StandardErrorResponse.class)
	})
	@GetMapping
	public HelloResponse hello() {
		return HelloResponse.builder().greeting("Hello").build();
	}

	@ApiOperation(value = "Customized Hello Message", notes = "Returns hello greeting with your first and last names")
	@ApiProperty.Boolean(name = "x-42c-no-authentication", value = true)
	@ApiResponses({
			@ApiResponse(code = 200, message = "OK", response = HelloResponse.class),
			@ApiResponse(code = 0, message = "All Errors", response = StandardErrorResponse.class)
	})
	@PostMapping
	public HelloResponse helloName(@RequestBody @Valid HelloRequest helloRequest) {

		String messageToPubSub = helloRequest.getFirstName() + helloRequest.getLastName();
		bridge.send(cloudStreamOutputDestination, messageToPubSub);

		return HelloResponse.builder()
				.greeting("Hello " + helloRequest.getFirstName() + " " + helloRequest.getLastName())
				.build();
	}
}
