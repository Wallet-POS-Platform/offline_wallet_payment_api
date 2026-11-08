package com.pixnetics.wallet_backend;

import com.pixnetics.wallet_backend.payment.client.ClientService;
import com.pixnetics.wallet_backend.payment.client.dto.ClientCreationRequest;
import com.pixnetics.wallet_backend.payment.client.dto.ClientResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.cockroachdb.CockroachContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ClientEndpointTests {

	@Container
	static CockroachContainer container = new CockroachContainer("cockroachdb/cockroach:latest-v26.2");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ClientService clientService;

    @DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", container::getJdbcUrl);
		registry.add("spring.datasource.username", container::getUsername);
		registry.add("spring.datasource.password", container::getPassword);
	}

	@Test
	void testCreateClient() throws Exception {
        ClientCreationRequest request = new ClientCreationRequest(
                "test_user_01", "password123", "Jane", "Doe", "123 Main Street", "+491234567890", "DE",
                "PK_GIuwVRcMjIRmLQ3DX5/AhDNbiRt"
        );

        String json = objectMapper.writeValueAsString(request);

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders
					.post("/api/v1/clients")
					.with(httpBasic("user", "apple"))
					.contentType(MediaType.APPLICATION_JSON)
					.content(json)
				)
				.andExpect(status().isCreated())
				.andReturn();

		ClientResponse response = objectMapper.readValue(
				result.getResponse().getContentAsString(),
				ClientResponse.class
		);

		Assertions.assertNotNull(response.id());
		Assertions.assertEquals(request.username(), response.username());
		Assertions.assertEquals(1, response.wallets().size());
	}

	@Test
	void testGetClient() throws Exception {
		ClientResponse testClient = clientService.create(new ClientCreationRequest(
				"test_user_02", "password456", "John", "Smith", null, null, null,
				"PK_MIGfMA0GCSqGSIb3DQEBAQUAA4GNAD"
		));

		MvcResult result = mockMvc.perform(
				MockMvcRequestBuilders
					.get("/api/v1/clients/{id}", testClient.id())
					.with(httpBasic("user", "apple"))
				)
				.andExpect(status().isOk())
				.andReturn();

		ClientResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(),
			ClientResponse.class
		);

		Assertions.assertEquals(testClient.id(), response.id());
	}

	@Test
	void testGetNonExitsClient() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders
					.get("/api/v1/clients/{id}", UUID.randomUUID())
					.with(httpBasic("user", "apple"))
				)
				.andExpect(status().isNotFound());
	}
}
