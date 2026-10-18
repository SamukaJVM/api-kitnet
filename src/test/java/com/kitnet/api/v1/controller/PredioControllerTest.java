package com.kitnet.api.v1.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.kitnet.api.v1.input.PredioInput;
import com.kitnet.config.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PredioControllerTest {

	@Autowired
	private WebApplicationContext wac;

	private final JsonMapper jsonMapper = new JsonMapper();

	private MockMvc mockMvc;

	private void setupMockMvc() {
		if (mockMvc == null) {
			mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
		}
	}

	@Test
	void shouldCreatePredioWithSuccess() throws Exception {
		setupMockMvc();

		PredioInput input = new PredioInput(
			"Prédio A",
			"12345678",
			"Rua A",
			"Apt 101",
			"Centro",
			"São Paulo",
			"SP",
			"100"
		);

		mockMvc.perform(post("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(input)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNotEmpty())
			.andExpect(jsonPath("$.descricao").value("Prédio A"))
			.andExpect(jsonPath("$.cep").value("12345678"));
	}

	@Test
	void shouldReturnCreatedWith201StatusAndLocationHeader() throws Exception {
		setupMockMvc();

		PredioInput input = new PredioInput(
			"Prédio B",
			"87654321",
			"Rua B",
			null,
			null,
			null,
			null,
			null
		);

		mockMvc.perform(post("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(input)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").exists());
	}

	@Test
	void shouldFindPredioById() throws Exception {
		setupMockMvc();

		PredioInput input = new PredioInput(
			"Prédio Para Buscar",
			"11111111",
			null, null, null, null, null, null
		);
		String response = mockMvc.perform(post("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(input)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();

		Long id = jsonMapper.readTree(response).get("id").asLong();

		mockMvc.perform(get("/v1/predios/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.descricao").value("Prédio Para Buscar"));
	}

	@Test
	void shouldReturn404WhenPredioNotFound() throws Exception {
		setupMockMvc();

		mockMvc.perform(get("/v1/predios/9999"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.title").value("Entidade não encontrada"));
	}

	@Test
	void shouldReturnErrorForDuplicateDescricao() throws Exception {
		setupMockMvc();

		PredioInput input = new PredioInput(
			"Prédio Duplicado",
			"22222222",
			null, null, null, null, null, null
		);

		mockMvc.perform(post("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(input)))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(input)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.title").value("Violação de regra de negócio"));
	}

	@Test
	void shouldUpdatePredio() throws Exception {
		setupMockMvc();

		PredioInput input = new PredioInput(
			"Prédio Original",
			"33333333",
			"Rua Original",
			null, null, null, null, null
		);
		String response = mockMvc.perform(post("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(input)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();

		Long id = jsonMapper.readTree(response).get("id").asLong();

		PredioInput updatedInput = new PredioInput(
			"Prédio Atualizado",
			"44444444",
			"Rua Nova",
			null, null, null, null, null
		);

		mockMvc.perform(put("/v1/predios/{id}", id)
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(updatedInput)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.descricao").value("Prédio Atualizado"))
			.andExpect(jsonPath("$.logradouro").value("Rua Nova"));
	}

	@Test
	void shouldDeletePredio() throws Exception {
		setupMockMvc();

		PredioInput input = new PredioInput(
			"Prédio Para Deletar",
			"55555555",
			null, null, null, null, null, null
		);
		String response = mockMvc.perform(post("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(input)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();

		Long id = jsonMapper.readTree(response).get("id").asLong();

		mockMvc.perform(delete("/v1/predios/{id}", id))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/v1/predios/{id}", id))
			.andExpect(status().isNotFound());
	}

	@Test
	void shouldListPrediosPaginated() throws Exception {
		setupMockMvc();

		PredioInput input = new PredioInput(
			"Prédio Para Listar",
			"66666666",
			null, null, null, null, null, null
		);

		mockMvc.perform(post("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(input)))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/v1/predios"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray());
	}

	@Test
	void shouldUpdatePredioWithSameDescricao() throws Exception {
		setupMockMvc();

		PredioInput input = new PredioInput(
			"Prédio Mesmo Nome",
			"77777777",
			"Rua Inicial",
			null, null, null, null, null
		);
		String response = mockMvc.perform(post("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(input)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();

		Long id = jsonMapper.readTree(response).get("id").asLong();

		PredioInput sameDescInput = new PredioInput(
			"Prédio Mesmo Nome",
			"88888888",
			"Rua Alterada",
			null, null, null, null, null
		);

		mockMvc.perform(put("/v1/predios/{id}", id)
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(sameDescInput)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.descricao").value("Prédio Mesmo Nome"))
			.andExpect(jsonPath("$.cep").value("88888888"))
			.andExpect(jsonPath("$.logradouro").value("Rua Alterada"));
	}

	@Test
	void shouldReturnValidationErrorWithFieldsForInvalidCep() throws Exception {
		setupMockMvc();

		PredioInput input = new PredioInput(
			"Prédio Validação CEP",
			"123",
			null, null, null, null, null, null
		);

		mockMvc.perform(post("/v1/predios")
			.contentType(MediaType.APPLICATION_JSON)
			.content(jsonMapper.writeValueAsString(input)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.title").value("Erro de validação"))
			.andExpect(jsonPath("$.fields.cep").exists());
	}

}
