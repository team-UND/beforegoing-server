package com.und.server.terms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.member.service.MemberService;
import com.und.server.terms.dto.request.EventPushAgreementRequest;
import com.und.server.terms.dto.request.TermsAgreementRequest;
import com.und.server.terms.dto.response.TermsAgreementResponse;
import com.und.server.terms.entity.Terms;
import com.und.server.terms.exception.TermsErrorResult;
import com.und.server.terms.repository.TermsRepository;

@ExtendWith(MockitoExtension.class)
class TermsServiceTest {

	@InjectMocks
	private TermsService termsService;

	@Mock
	private TermsRepository termsRepository;

	@Mock
	private MemberService memberService;

	private final Long memberId = 1L;
	private final Member member = Member.builder().id(memberId).build();

	@Test
	@DisplayName("Retrieves a list of all term agreements")
	void Given_ExistingTermAgreements_When_GetTermsList_Then_ReturnsListOfResponses() {
		// given
		final Terms terms1 = Terms.builder()
			.id(1L)
			.member(member)
			.termsOfServiceAgreed(true)
			.privacyPolicyAgreed(true)
			.isOver14(true)
			.eventPushAgreed(false)
			.build();
		final Terms terms2 = Terms.builder()
			.id(2L)
			.member(Member.builder().id(2L).build())
			.termsOfServiceAgreed(true)
			.privacyPolicyAgreed(true)
			.isOver14(true)
			.eventPushAgreed(true)
			.build();
		final List<Terms> termsList = List.of(terms1, terms2);
		doReturn(termsList).when(termsRepository).findAll();

		// when
		final List<TermsAgreementResponse> responses = termsService.getTermsList();

		// then
		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).id()).isEqualTo(1L);
		assertThat(responses.get(1).id()).isEqualTo(2L);
		verify(termsRepository).findAll();
	}

	@Test
	@DisplayName("Fails to get terms agreement for a non-existent member")
	void Given_NonExistentMember_When_GetTermsAgreement_Then_ThrowsException() {
		// given
		doThrow(new ServerException(TermsErrorResult.TERMS_NOT_FOUND)).when(memberService).checkMemberExists(memberId);

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> termsService.getTermsAgreement(memberId));

		assertThat(exception.getErrorResult()).isEqualTo(TermsErrorResult.TERMS_NOT_FOUND);
	}

	@Test
	@DisplayName("Fails to get terms agreement when none exists for the member")
	void Given_MemberWithoutTerms_When_GetTermsAgreement_Then_ThrowsException() {
		// given
		doNothing().when(memberService).checkMemberExists(memberId);
		doReturn(Optional.empty()).when(termsRepository).findByMemberId(memberId);

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> termsService.getTermsAgreement(memberId));

		assertThat(exception.getErrorResult()).isEqualTo(TermsErrorResult.TERMS_NOT_FOUND);
	}

	@Test
	@DisplayName("Retrieves terms agreement for an existing member")
	void Given_ExistingMemberWithTerms_When_GetTermsAgreement_Then_ReturnsResponse() {
		// given
		final Terms terms = Terms.builder()
			.id(1L)
			.member(member)
			.termsOfServiceAgreed(true)
			.privacyPolicyAgreed(true)
			.isOver14(true)
			.eventPushAgreed(true)
			.build();
		doNothing().when(memberService).checkMemberExists(memberId);
		doReturn(Optional.of(terms)).when(termsRepository).findByMemberId(memberId);

		// when
		final TermsAgreementResponse response = termsService.getTermsAgreement(memberId);

		// then
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.memberId()).isEqualTo(memberId);
		assertThat(response.eventPushAgreed()).isTrue();
	}

	@Test
	@DisplayName("Fails to add terms agreement if it already exists")
	void Given_ExistingTerms_When_AddTermsAgreement_Then_ThrowsException() {
		// given
		final TermsAgreementRequest request = new TermsAgreementRequest(true, true, true, false);
		doReturn(member).when(memberService).findMemberById(memberId);
		doReturn(true).when(termsRepository).existsByMemberId(memberId);

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> termsService.addTermsAgreement(memberId, request));

		assertThat(exception.getErrorResult()).isEqualTo(TermsErrorResult.TERMS_ALREADY_EXISTS);
	}

	@Test
	@DisplayName("Successfully adds a new terms agreement")
	void Given_NewMember_When_AddTermsAgreement_Then_SavesAndReturnsResponse() {
		// given
		final TermsAgreementRequest request = new TermsAgreementRequest(true, true, true, true);
		final Terms savedTerms = Terms.builder()
			.id(1L)
			.member(member)
			.termsOfServiceAgreed(true)
			.privacyPolicyAgreed(true)
			.isOver14(true)
			.eventPushAgreed(true)
			.build();

		doReturn(member).when(memberService).findMemberById(memberId);
		doReturn(false).when(termsRepository).existsByMemberId(memberId);
		doReturn(savedTerms).when(termsRepository).save(any(Terms.class));

		// when
		final TermsAgreementResponse response = termsService.addTermsAgreement(memberId, request);

		// then
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.memberId()).isEqualTo(memberId);
		assertThat(response.eventPushAgreed()).isTrue();
		verify(termsRepository).save(any(Terms.class));
	}

	@Test
	@DisplayName("Fails to update event push agreement for a non-existent member")
	void Given_NonExistentMember_When_UpdateEventPushAgreement_Then_ThrowsException() {
		// given
		final EventPushAgreementRequest request = new EventPushAgreementRequest(true);
		doThrow(new ServerException(TermsErrorResult.TERMS_NOT_FOUND)).when(memberService).checkMemberExists(memberId);

		// when & then
		final ServerException exception = assertThrows(ServerException.class,
			() -> termsService.updateEventPushAgreement(memberId, request));

		assertThat(exception.getErrorResult()).isEqualTo(TermsErrorResult.TERMS_NOT_FOUND);
	}

	@Test
	@DisplayName("Successfully updates event push agreement")
	void Given_ExistingTerms_When_UpdateEventPushAgreement_Then_UpdatesAndReturnsResponse() {
		// given
		final EventPushAgreementRequest request = new EventPushAgreementRequest(false);
		final Terms existingTerms = Terms.builder()
			.id(1L)
			.member(member)
			.termsOfServiceAgreed(true)
			.privacyPolicyAgreed(true)
			.isOver14(true)
			.eventPushAgreed(true)
			.build();

		doNothing().when(memberService).checkMemberExists(memberId);
		doReturn(Optional.of(existingTerms)).when(termsRepository).findByMemberId(memberId);

		// when
		final TermsAgreementResponse response = termsService.updateEventPushAgreement(memberId, request);

		// then
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.eventPushAgreed()).isFalse();
		assertThat(existingTerms.getEventPushAgreed()).isFalse();
	}

}
