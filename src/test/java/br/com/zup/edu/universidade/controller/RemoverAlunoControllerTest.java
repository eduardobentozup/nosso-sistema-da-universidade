package br.com.zup.edu.universidade.controller;

import br.com.zup.edu.universidade.model.RespostaAvaliacao;
import br.com.zup.edu.universidade.repository.AlunoRepository;
import br.com.zup.edu.universidade.repository.RespostaAvaliacaoRepository;
import br.com.zup.edu.universidade.repository.RespostaQuestaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.signedness.qual.SignedPositive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class RemoverAlunoControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private AlunoRepository alunoRepository;

	@Autowired
	private RespostaAvaliacaoRepository respostaAvaliacaoRepository;

	@Autowired
	private RespostaQuestaoRepository respostaQuestaoRepository;

	private final String URL = "/alunos/{id}";




	@BeforeEach
	void setUp() {
	}



	@Test
	void shouldNotDeleteAUnexistentStudent() throws Exception {

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(URL, Long.MIN_VALUE)
			.header("Accept-Language", "pt-br");

		Exception exception = mvc.perform(request)
			.andExpect(status().isNotFound())
			.andReturn()
			.getResolvedException();

		assertNotNull(exception);
		assertEquals(ResponseStatusException.class, exception.getClass());
		assertEquals("aluno nao cadastrado", ((ResponseStatusException) exception).getReason());

	}
}