package com.und.server.scenario.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.und.server.common.exception.ServerException;
import com.und.server.scenario.exception.ScenarioErrorResult;
import com.und.server.scenario.repository.ScenarioRepository;

@ExtendWith(MockitoExtension.class)
class ScenarioValidatorTest {

	@Mock
	private ScenarioRepository scenarioRepository;

	@InjectMocks
	private ScenarioValidator scenarioValidator;

	@Test
	void Given_ExistingScenarioId_When_ValidateScenarioExists_Then_NoException() {
		// given
		Long scenarioId = 1L;
		given(scenarioRepository.existsById(scenarioId)).willReturn(true);

		// when & then
		assertDoesNotThrow(() -> scenarioValidator.validateScenarioExists(scenarioId));
	}

	@Test
	void Given_NonExistingScenarioId_When_ValidateScenarioExists_Then_ThrowException() {
		// given
		Long scenarioId = 99L;
		given(scenarioRepository.existsById(scenarioId)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> scenarioValidator.validateScenarioExists(scenarioId))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.NOT_FOUND_SCENARIO.getMessage());
	}

	@Test
	void Given_OrderListBelowMaxCount_When_ValidateMaxScenarioCount_Then_NoException() {
		// given
		List<Integer> orderList = List.of(1000, 2000, 3000); // 3개 (20개 미만)

		// when & then
		assertDoesNotThrow(() -> scenarioValidator.validateMaxScenarioCount(orderList));
	}

	@Test
	void Given_OrderListAtMaxCount_When_ValidateMaxScenarioCount_Then_ThrowException() {
		// given
		List<Integer> orderList = List.of(
			1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000,
			11000, 12000, 13000, 14000, 15000, 16000, 17000, 18000, 19000, 20000
		); // 20개 (최대값)

		// when & then
		assertThatThrownBy(() -> scenarioValidator.validateMaxScenarioCount(orderList))
			.isInstanceOf(ServerException.class)
			.hasMessageContaining(ScenarioErrorResult.MAX_SCENARIO_COUNT_EXCEEDED.getMessage());
	}

	@Test
	void Given_EmptyOrderList_When_ValidateMaxScenarioCount_Then_NoException() {
		// given
		List<Integer> orderList = List.of();

		// when & then
		assertDoesNotThrow(() -> scenarioValidator.validateMaxScenarioCount(orderList));
	}

}
