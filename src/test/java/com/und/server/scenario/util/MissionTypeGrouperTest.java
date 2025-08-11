package com.und.server.scenario.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.und.server.common.exception.ServerException;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.exception.ScenarioErrorResult;

@ExtendWith(MockitoExtension.class)
class MissionTypeGrouperTest {

	@InjectMocks
	private MissionTypeGroupSorter grouper;


	@BeforeEach
	void setUp() {
		grouper = new MissionTypeGroupSorter();
	}

	@Test
	void Given_BasicMissions_When_GroupAndSort_Then_ReturnSortedList() {
		// given
		Mission m1 = Mission.builder().missionOrder(2).missionType(MissionType.BASIC).build();
		Mission m2 = Mission.builder().missionOrder(1).missionType(MissionType.BASIC).build();
		Mission m3 = Mission.builder().missionOrder(null).missionType(MissionType.TODAY).build();
		List<Mission> input = List.of(m1, m2, m3);

		// when
		List<Mission> result = grouper.groupAndSortByType(input, MissionType.BASIC);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getMissionOrder()).isEqualTo(1);
		assertThat(result.get(1).getMissionOrder()).isEqualTo(2);
	}


	@Test
	void Given_TodayMissions_When_GroupAndSort_Then_ReturnReverseSortedList() {
		// given
		LocalDateTime now = LocalDateTime.now();

		Mission m1 = Mission.builder()
			.missionOrder(null)
			.missionType(MissionType.TODAY)
			.build();
		ReflectionTestUtils.setField(m1, "createdAt", now.minusDays(1));

		Mission m2 = Mission.builder()
			.missionOrder(null)
			.missionType(MissionType.TODAY)
			.build();
		ReflectionTestUtils.setField(m2, "createdAt", now);

		Mission m3 = Mission.builder()
			.missionOrder(2)
			.missionType(MissionType.BASIC)
			.build();

		List<Mission> input = List.of(m1, m2, m3);

		// when
		List<Mission> result = grouper.groupAndSortByType(input, MissionType.TODAY);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getCreatedAt()).isEqualTo(now);
		assertThat(result.get(1).getCreatedAt()).isEqualTo(now.minusDays(1));
	}


	@Test
	void Given_UnsupportedType_When_GroupAndSort_Then_ThrowException() {
		// given
		Mission invalidMission = Mission.builder().missionOrder(1).missionType(null).build();
		List<Mission> input = List.of(invalidMission);

		// then
		assertThatThrownBy(() ->
			grouper.groupAndSortByType(input, null)
		).isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.UNSUPPORTED_MISSION_TYPE.getMessage());
	}

}
