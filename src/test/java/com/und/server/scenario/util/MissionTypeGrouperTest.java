package com.und.server.scenario.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.common.exception.ServerException;
import com.und.server.scenario.constants.MissionType;
import com.und.server.scenario.entity.Mission;
import com.und.server.scenario.exception.ScenarioErrorResult;

@ExtendWith(MockitoExtension.class)
class MissionTypeGrouperTest {

	@InjectMocks
	private MissionTypeGrouper grouper;


	@BeforeEach
	void setUp() {
		grouper = new MissionTypeGrouper();
	}

	@Test
	void Given_BasicMissions_When_GroupAndSort_Then_ReturnSortedList() {
		// given
		Mission m1 = Mission.builder().order(2).missionType(MissionType.BASIC).build();
		Mission m2 = Mission.builder().order(1).missionType(MissionType.BASIC).build();
		Mission m3 = Mission.builder().order(3).missionType(MissionType.TODAY).build();
		List<Mission> input = List.of(m1, m2, m3);

		// when
		List<Mission> result = grouper.groupAndSortByType(input, MissionType.BASIC);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getOrder()).isEqualTo(1);
		assertThat(result.get(1).getOrder()).isEqualTo(2);
	}

	@Test
	void Given_TodayMissions_When_GroupAndSort_Then_ReturnReverseSortedList() {
		// given
		Mission m1 = Mission.builder().order(1).missionType(MissionType.TODAY).build();
		Mission m2 = Mission.builder().order(3).missionType(MissionType.TODAY).build();
		Mission m3 = Mission.builder().order(2).missionType(MissionType.BASIC).build();
		List<Mission> input = List.of(m1, m2, m3);

		// when
		List<Mission> result = grouper.groupAndSortByType(input, MissionType.TODAY);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getOrder()).isEqualTo(3);
		assertThat(result.get(1).getOrder()).isEqualTo(1);
	}

	@Test
	void Given_UnsupportedType_When_GroupAndSort_Then_ThrowException() {
		// given
		Mission invalidMission = Mission.builder().order(1).missionType(null).build();
		List<Mission> input = List.of(invalidMission);

		// then
		assertThatThrownBy(() ->
			grouper.groupAndSortByType(input, null)
		).isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.UNSUPPORTED_MISSION_TYPE.getMessage());
	}

}
