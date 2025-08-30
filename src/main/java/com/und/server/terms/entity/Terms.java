package com.und.server.terms.entity;

import com.und.server.common.entity.BaseTimeEntity;
import com.und.server.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "terms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Terms extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, unique = true)
	private Member member;

	@Column(nullable = false)
	@Builder.Default
	private Boolean termsOfServiceAgreed = false;

	@Column(nullable = false)
	@Builder.Default
	private Boolean privacyPolicyAgreed = false;

	@Column(name = "is_over_14", nullable = false)
	@Builder.Default
	private Boolean isOver14 = false;

	@Column(nullable = false)
	@Builder.Default
	private Boolean eventPushAgreed = false;

	public void updateEventPushAgreed(final Boolean eventPushAgreed) {
		this.eventPushAgreed = eventPushAgreed;
	}

}
