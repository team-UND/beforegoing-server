package com.und.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.und.server.dto.TestResponse;
import com.und.server.entity.Member;
import com.und.server.exception.GlobalExceptionHandler;
import com.und.server.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {

	@InjectMocks
	private TestController testController;

	@Mock
	private MemberRepository memberRepository;

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@BeforeEach
	void init() {
		mockMvc = MockMvcBuilders.standaloneSetup(testController)
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();

		objectMapper = new ObjectMapper();
	}

	@Test
	void helloWithNickname() throws Exception {
		// given
		Long memberId = 1L;
		Member member = Member.builder().id(memberId).nickname("Chori").build();

		doReturn(Optional.of(member)).when(memberRepository).findById(memberId);
		Authentication auth = new UsernamePasswordAuthenticationToken(memberId, null);

		// when
		ResultActions result = mockMvc.perform(
			MockMvcRequestBuilders.get("/hello")
				.principal(auth)
		);

		// then
		result.andExpect(status().isOk());

		String responseBody = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		TestResponse response = objectMapper.readValue(responseBody, TestResponse.class);

		assertThat(response.message()).isEqualTo("Hello, Chori!");
	}

	@Test
	void helloWithDefaultNickname() throws Exception {
		// given
		Long memberId = 2L;
		Member member = Member.builder().id(memberId).nickname(null).build();

		doReturn(Optional.of(member)).when(memberRepository).findById(memberId);
		Authentication auth = new UsernamePasswordAuthenticationToken(memberId, null);

		// when
		ResultActions result = mockMvc.perform(
			MockMvcRequestBuilders.get("/hello")
				.principal(auth)
		);

		// then
		result.andExpect(status().isOk());

		String responseBody = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
		TestResponse response = objectMapper.readValue(responseBody, TestResponse.class);

		assertThat(response.message()).isEqualTo("Hello, Member!");
	}

	@Test
	void helloWithMissingMember() throws Exception {
		// given
		Long memberId = 3L;

		doReturn(Optional.empty()).when(memberRepository).findById(memberId);
		Authentication auth = new UsernamePasswordAuthenticationToken(memberId, null);

		// when
		ResultActions result = mockMvc.perform(
			MockMvcRequestBuilders.get("/hello")
				.principal(auth)
		);

		// then
		result.andExpect(status().isUnauthorized());
	}
}
