package com.und.server.notification.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ActiveUpdateEventTest {

	private final Long memberId = 1L;


	@Test
	void Given_ValidMemberIdAndActiveTrue_When_CreateActiveUpdateEvent_Then_CreateEventWithCorrectValues() {
		// given
		boolean isActive = true;

		// when
		ActiveUpdateEvent event = new ActiveUpdateEvent(memberId, isActive);

		// then
		assertThat(event.memberId()).isEqualTo(memberId);
		assertThat(event.isActive()).isTrue();
	}


	@Test
	void Given_ValidMemberIdAndActiveFalse_When_CreateActiveUpdateEvent_Then_CreateEventWithCorrectValues() {
		// given
		boolean isActive = false;

		// when
		ActiveUpdateEvent event = new ActiveUpdateEvent(memberId, isActive);

		// then
		assertThat(event.memberId()).isEqualTo(memberId);
		assertThat(event.isActive()).isFalse();
	}


	@Test
	void Given_DifferentMemberId_When_CreateActiveUpdateEvent_Then_CreateEventWithCorrectValues() {
		// given
		Long differentMemberId = 2L;
		boolean isActive = true;

		// when
		ActiveUpdateEvent event = new ActiveUpdateEvent(differentMemberId, isActive);

		// then
		assertThat(event.memberId()).isEqualTo(differentMemberId);
		assertThat(event.isActive()).isTrue();
	}


	@Test
	void Given_SameValues_When_CreateTwoActiveUpdateEvents_Then_EventsAreEqual() {
		// given
		boolean isActive = true;

		// when
		ActiveUpdateEvent event1 = new ActiveUpdateEvent(memberId, isActive);
		ActiveUpdateEvent event2 = new ActiveUpdateEvent(memberId, isActive);

		// then
		assertThat(event1).isEqualTo(event2);
		assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
	}


	@Test
	void Given_DifferentValues_When_CreateTwoActiveUpdateEvents_Then_EventsAreNotEqual() {
		// given
		boolean isActive1 = true;
		boolean isActive2 = false;

		// when
		ActiveUpdateEvent event1 = new ActiveUpdateEvent(memberId, isActive1);
		ActiveUpdateEvent event2 = new ActiveUpdateEvent(memberId, isActive2);

		// then
		assertThat(event1).isNotEqualTo(event2);
		assertThat(event1.hashCode()).isNotEqualTo(event2.hashCode());
	}


	@Test
	void Given_ActiveUpdateEvent_When_ToString_Then_ReturnStringRepresentation() {
		// given
		boolean isActive = true;
		ActiveUpdateEvent event = new ActiveUpdateEvent(memberId, isActive);

		// when
		String toString = event.toString();

		// then
		assertThat(toString).contains("ActiveUpdateEvent");
		assertThat(toString).contains("memberId=" + memberId);
		assertThat(toString).contains("isActive=" + isActive);
	}

}
