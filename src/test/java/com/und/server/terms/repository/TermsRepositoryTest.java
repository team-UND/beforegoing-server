package com.und.server.terms.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.und.server.member.entity.Member;
import com.und.server.member.repository.MemberRepository;
import com.und.server.terms.entity.Terms;

@DataJpaTest
class TermsRepositoryTest {

	@Autowired
	private TermsRepository termsRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Member member;

	@BeforeEach
	void setUp() {
		member = memberRepository.save(Member.builder().nickname("test-user").build());
	}

	@Test
	@DisplayName("Finds terms by member ID when they exist")
	void Given_ExistingTerms_When_FindByMemberId_Then_ReturnsOptionalOfTerms() {
		// given
		final Terms terms = Terms.builder()
			.member(member)
			.termsOfServiceAgreed(true)
			.privacyPolicyAgreed(true)
			.isOver14(true)
			.eventPushAgreed(false)
			.build();
		termsRepository.save(terms);

		// when
		final Optional<Terms> foundTerms = termsRepository.findByMemberId(member.getId());

		// then
		assertThat(foundTerms).isPresent();
		assertThat(foundTerms.get().getMember().getId()).isEqualTo(member.getId());
		assertThat(foundTerms.get().getEventPushAgreed()).isFalse();
	}

	@Test
	@DisplayName("Returns empty optional when finding terms for a member without them")
	void Given_MemberWithoutTerms_When_FindByMemberId_Then_ReturnsEmptyOptional() {
		// when
		final Optional<Terms> foundTerms = termsRepository.findByMemberId(member.getId());

		// then
		assertThat(foundTerms).isNotPresent();
	}

	@Test
	@DisplayName("Returns true when checking existence for a member with terms")
	void Given_ExistingTerms_When_ExistsByMemberId_Then_ReturnsTrue() {
		// given
		final Terms terms = Terms.builder().member(member).build();
		termsRepository.save(terms);

		// when
		final boolean exists = termsRepository.existsByMemberId(member.getId());

		// then
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("Returns false when checking existence for a member without terms")
	void Given_MemberWithoutTerms_When_ExistsByMemberId_Then_ReturnsFalse() {
		// when
		final boolean exists = termsRepository.existsByMemberId(member.getId());

		// then
		assertThat(exists).isFalse();
	}

	@Test
	@DisplayName("Finds all terms with members using EntityGraph to avoid N+1 problem")
	void Given_MultipleTerms_When_FindAll_Then_ReturnsTermsWithFetchedMembers() {
		// given
		final Member member2 = memberRepository.save(Member.builder().nickname("test-user-2").build());

		final Terms terms1 = Terms.builder().member(member).build();
		final Terms terms2 = Terms.builder().member(member2).build();
		termsRepository.saveAll(List.of(terms1, terms2));

		// when
		final List<Terms> allTerms = termsRepository.findAll();

		// then
		assertThat(allTerms).hasSize(2);
		assertThat(allTerms.stream().map(t -> t.getMember().getNickname()))
			.containsExactlyInAnyOrder("test-user", "test-user-2");
	}

}
