package br.com.zup.edu.universidade.controller;

import br.com.zup.edu.universidade.controller.request.AvaliacaoAlunoRequest;
import br.com.zup.edu.universidade.controller.request.RespostaQuestaoRequest;
import br.com.zup.edu.universidade.model.Aluno;
import br.com.zup.edu.universidade.model.Avaliacao;
import br.com.zup.edu.universidade.model.Questao;
import br.com.zup.edu.universidade.model.RespostaAvaliacao;
import br.com.zup.edu.universidade.repository.AlunoRepository;
import br.com.zup.edu.universidade.repository.AvaliacaoRepository;
import br.com.zup.edu.universidade.repository.QuestaoRepository;
import br.com.zup.edu.universidade.repository.RespostaAvaliacaoRepository;
import br.com.zup.edu.universidade.repository.RespostaQuestaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class FazerAvaliacaoControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private AlunoRepository alunoRepository;

	@Autowired
	private AvaliacaoRepository avaliacaoRepository;

	@Autowired
	private RespostaAvaliacaoRepository respostaAvaliacaoRepository;

	@Autowired
	private QuestaoRepository questaoRepository;

	@Autowired
	private RespostaQuestaoRepository respostaQuestaoRepository;

	@Autowired
	private ObjectMapper mapper;

	private final String URL = "/alunos/{id}/avaliacoes/{idAvaliacao}/respostas";

	private Questao question;
	private Avaliacao exam;
	private Aluno student;

	@BeforeEach
	void setUp() {

		alunoRepository.deleteAll();
		avaliacaoRepository.deleteAll();
		questaoRepository.deleteAll();
		respostaAvaliacaoRepository.deleteAll();
		respostaQuestaoRepository.deleteAll();

		question = new Questao(
			"What is Java",
			"A programming language",
			new BigDecimal(3)
		);

		exam = new Avaliacao(Sets.set(question));

		avaliacaoRepository.save(exam);

		student = new Aluno("Fulano", "10101", LocalDate.of(2001, 5, 13));
		alunoRepository.save(student);

	}

	@Test
	void shouldNotDoEvaluationWhenStudentNotFound() throws Exception {

		List<RespostaQuestaoRequest> answers = List.of(new RespostaQuestaoRequest(1L, "this is the answer"));
		AvaliacaoAlunoRequest requestBody = new AvaliacaoAlunoRequest(answers);

		String content = mapper.writeValueAsString(requestBody);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(URL, Integer.MAX_VALUE, Integer.MAX_VALUE)
			.content(content)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Accept-Language", "pt-br");

		Exception resolvedException = mvc.perform(request)
			.andExpect(status().isNotFound())
			.andReturn()
			.getResolvedException();

		assertNotNull(resolvedException);
		assertEquals(ResponseStatusException.class, resolvedException.getClass());
		assertEquals("aluno nao cadastrado", ((ResponseStatusException) resolvedException).getReason());

		assertEquals(0L, respostaAvaliacaoRepository.count());
		assertEquals(0L, respostaQuestaoRepository.count());
	}

	@Test
	void shouldNotDoEvaluationWhenExamNotFound() throws Exception {

		List<RespostaQuestaoRequest> answers = List.of(new RespostaQuestaoRequest(1L, "this is the answer"));
		AvaliacaoAlunoRequest requestBody = new AvaliacaoAlunoRequest(answers);

		String content = mapper.writeValueAsString(requestBody);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(URL, student.getId(), Integer.MAX_VALUE)
			.content(content)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Accept-Language", "pt-br");

		Exception resolvedException = mvc.perform(request)
			.andExpect(status().isNotFound())
			.andReturn()
			.getResolvedException();

		assertNotNull(resolvedException);
		assertEquals(ResponseStatusException.class, resolvedException.getClass());
		assertEquals("Avaliacao não cadastrada", ((ResponseStatusException) resolvedException).getReason());

		assertEquals(0L, respostaAvaliacaoRepository.count());
		assertEquals(0L, respostaQuestaoRepository.count());
	}


	@Test
	void shouldNotDoEvaluationWhenExamQuestionNotFound() throws Exception {

		List<RespostaQuestaoRequest> answers = List.of(new RespostaQuestaoRequest(1000L, "this is the answer"));
		AvaliacaoAlunoRequest requestBody = new AvaliacaoAlunoRequest(answers);

		String content = mapper.writeValueAsString(requestBody);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(URL, student.getId(), exam.getId())
			.content(content)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Accept-Language", "pt-br");

		Exception resolvedException = mvc.perform(request)
			.andExpect(status().isUnprocessableEntity())
			.andReturn()
			.getResolvedException();

		assertNotNull(resolvedException);
		assertEquals(ResponseStatusException.class, resolvedException.getClass());
		assertEquals("Nao existe cadastro para questao com id "+1000L, ((ResponseStatusException) resolvedException).getReason());

		assertEquals(0L, respostaAvaliacaoRepository.count());
		assertEquals(0L, respostaQuestaoRepository.count());

	}

	@Test
	void shouldDoNotEvaluationWhenQuestionsIsNull() throws Exception {

		AvaliacaoAlunoRequest requestBody = new AvaliacaoAlunoRequest();

		String content = mapper.writeValueAsString(requestBody);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(URL, student.getId(), exam.getId())
			.content(content)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Accept-Language", "pt-br");

		String responseString = mvc.perform(request)
			.andExpect(status().isBadRequest())
			.andReturn()
			.getResponse()
			.getContentAsString(StandardCharsets.UTF_8);

		TypeFactory typeFactory = mapper.getTypeFactory();
		List<String> errorMessages = mapper.readValue(responseString,typeFactory.constructCollectionType(List.class, String.class));

		assertFalse(errorMessages.isEmpty());
		assertTrue(errorMessages.contains("O campo respostas não deve ser nulo"));

		assertEquals(0L, respostaAvaliacaoRepository.count());
		assertEquals(0L, respostaQuestaoRepository.count());

	}

	@Test
	void shouldDoEvaluation() throws Exception {

		List<RespostaQuestaoRequest> answers = List.of(new RespostaQuestaoRequest(question.getId(), "this is the answer"));
		AvaliacaoAlunoRequest requestBody = new AvaliacaoAlunoRequest(answers);

		String content = mapper.writeValueAsString(requestBody);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(URL, student.getId(), exam.getId())
			.content(content)
			.contentType(MediaType.APPLICATION_JSON)
			.header("Accept-Language", "pt-br");

		mvc.perform(request)
			.andExpect(status().isCreated())
			.andExpect(MockMvcResultMatchers.redirectedUrlPattern("http://localhost/alunos/{id}/avaliacoes/{idAvaliacao}/respostas/{idResposta}"));

		assertTrue(respostaAvaliacaoRepository.count() > 0);
		assertTrue(respostaQuestaoRepository.count() > 0);
	}
}