package br.com.zup.edu.universidade.controller;

import br.com.zup.edu.universidade.model.Aluno;
import br.com.zup.edu.universidade.model.Avaliacao;
import br.com.zup.edu.universidade.model.Questao;
import br.com.zup.edu.universidade.model.RespostaAvaliacao;
import br.com.zup.edu.universidade.model.RespostaQuestao;
import br.com.zup.edu.universidade.repository.AlunoRepository;
import br.com.zup.edu.universidade.repository.AvaliacaoRepository;
import br.com.zup.edu.universidade.repository.RespostaAvaliacaoRepository;
import br.com.zup.edu.universidade.repository.RespostaQuestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class RemoverAlunoControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private AlunoRepository alunoRepository;

	@Autowired
	private RespostaAvaliacaoRepository respostaAvaliacaoRepository;

	@Autowired
	private RespostaQuestaoRepository respostaQuestaoRepository;

	@Autowired
	private AvaliacaoRepository avaliacaoRepository;

	private final String URL = "/alunos/{id}";

	private Avaliacao exam;
	private Aluno student;
	private RespostaAvaliacao examAnswer;
	Set<RespostaQuestao> answers;

	private Set<Questao> questions = new LinkedHashSet<>();


	@BeforeEach
	void setUp() {

		respostaAvaliacaoRepository.deleteAll();
		avaliacaoRepository.deleteAll();
		alunoRepository.deleteAll();

		questions.addAll(Set.of(
			new Questao("Quanto é 1+1?", "2", new BigDecimal("1")),
			new Questao("Qual é a capital de Minas Gerais?", "Belo Horizonte", new BigDecimal("2")),
			new Questao("Qual a ciência que estuda a computação?", "Ciências da Computação", new BigDecimal("4"))
		));
		this.exam = new Avaliacao(this.questions);

		avaliacaoRepository.save(exam);

		this.student = new Aluno("Fulano", "10101", LocalDate.of(2001, 5, 13));

		answers = questions.stream()
			.map(q -> new RespostaQuestao(student, q, "any answer"))
			.collect(Collectors.toSet());

		examAnswer = new RespostaAvaliacao(student, exam, answers);
		student.adicionar(examAnswer);

		alunoRepository.save(student);
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

	@Test
	void shouldDeleteStudentAndYourExams() throws Exception {

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(URL, student.getId())
			.header("Accept-Language", "pt-br");

		mvc.perform(request)
			.andExpect(status().isNoContent());

		assertFalse(alunoRepository.existsById(student.getId()));
		assertFalse(respostaAvaliacaoRepository.existsById(examAnswer.getId()));
		answers.forEach(a -> assertFalse(respostaQuestaoRepository.existsById(a.getId())));
	}
}