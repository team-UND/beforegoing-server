package com.und.server.terms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.und.server.common.exception.ServerException;
import com.und.server.member.entity.Member;
import com.und.server.member.service.MemberService;
import com.und.server.terms.dto.request.EventPushAgreementRequest;
import com.und.server.terms.dto.request.TermsAgreementRequest;
import com.und.server.terms.dto.response.TermsAgreementResponse;
import com.und.server.terms.entity.Terms;
import com.und.server.terms.exception.TermsErrorResult;
import com.und.server.terms.repository.TermsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

	private final TermsRepository termsRepository;
	private final MemberService memberService;

	public List<TermsAgreementResponse> getTermsList() {
		return termsRepository.findAll()
			.stream().map(TermsAgreementResponse::from).toList();
	}

	public TermsAgreementResponse getTermsAgreement(final Long memberId) {
		memberService.checkMemberExists(memberId);
		final Terms terms = findTermsByMemberId(memberId);

		return TermsAgreementResponse.from(terms);
	}

	@Transactional
	public TermsAgreementResponse addTermsAgreement(
		final Long memberId,
		final TermsAgreementRequest termsAgreementRequest
	) {
		if (hasAgreedTerms(memberId)) {
			throw new ServerException(TermsErrorResult.TERMS_ALREADY_EXISTS);
		}

		final Member member =  memberService.findMemberById(memberId);
		final Terms terms = Terms.builder()
			.member(member)
			.termsOfServiceAgreed(termsAgreementRequest.termsOfServiceAgreed())
			.privacyPolicyAgreed(termsAgreementRequest.privacyPolicyAgreed())
			.isOver14(termsAgreementRequest.isOver14())
			.eventPushAgreed(termsAgreementRequest.eventPushAgreed())
			.build();

		return TermsAgreementResponse.from(termsRepository.save(terms));
	}

	@Transactional
	public TermsAgreementResponse updateEventPushAgreement(
		final Long memberId,
		final EventPushAgreementRequest eventPushAgreementRequest
	) {
		memberService.checkMemberExists(memberId);

		final Terms terms = findTermsByMemberId(memberId);
		terms.updateEventPushAgreed(eventPushAgreementRequest.eventPushAgreed());

		return TermsAgreementResponse.from(terms);
	}

	private boolean hasAgreedTerms(final Long memberId) {
		return termsRepository.existsByMemberId(memberId);
	}

	private Terms findTermsByMemberId(final Long memberId) {
		return termsRepository.findByMemberId(memberId)
			.orElseThrow(() -> new ServerException(TermsErrorResult.TERMS_NOT_FOUND));
	}

}
