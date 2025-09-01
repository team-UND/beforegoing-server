package com.und.server.weather.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.und.server.weather.dto.GridPoint;

@DisplayName("GridConverter 테스트")
class GridConverterTest {

	@Test
	@DisplayName("위도/경도를 API 그리드로 변환할 수 있다")
	void Given_LatitudeAndLongitude_When_ConvertToApiGrid_Then_ReturnsGridPoint() {
		// given
		double latitude = 37.5665; // 서울 위도
		double longitude = 126.9780; // 서울 경도

		// when
		GridPoint result = GridConverter.convertToApiGrid(latitude, longitude);

		// then
		assertThat(result).isNotNull();
		assertThat(result.gridX()).isPositive();
		assertThat(result.gridY()).isPositive();
	}


	@Test
	@DisplayName("위도/경도를 캐시 그리드로 변환할 수 있다")
	void Given_LatitudeAndLongitudeAndGrid_When_ConvertToCacheGrid_Then_ReturnsGridPoint() {
		// given
		double latitude = 37.5665; // 서울 위도
		double longitude = 126.9780; // 서울 경도
		double grid = 1.0; // 1km 그리드

		// when
		GridPoint result = GridConverter.convertToCacheGrid(latitude, longitude, grid);

		// then
		assertThat(result).isNotNull();
		assertThat(result.gridX()).isPositive();
		assertThat(result.gridY()).isPositive();
	}


	@Test
	@DisplayName("부산 좌표를 API 그리드로 변환할 수 있다")
	void Given_BusanCoordinates_When_ConvertToApiGrid_Then_ReturnsGridPoint() {
		// given
		double latitude = 35.1796; // 부산 위도
		double longitude = 129.0756; // 부산 경도

		// when
		GridPoint result = GridConverter.convertToApiGrid(latitude, longitude);

		// then
		assertThat(result).isNotNull();
		assertThat(result.gridX()).isPositive();
		assertThat(result.gridY()).isPositive();
	}


	@Test
	@DisplayName("제주도 좌표를 API 그리드로 변환할 수 있다")
	void Given_JejuCoordinates_When_ConvertToApiGrid_Then_ReturnsGridPoint() {
		// given
		double latitude = 33.4996; // 제주도 위도
		double longitude = 126.5312; // 제주도 경도

		// when
		GridPoint result = GridConverter.convertToApiGrid(latitude, longitude);

		// then
		assertThat(result).isNotNull();
		assertThat(result.gridX()).isPositive();
		assertThat(result.gridY()).isPositive();
	}

	@Test
	@DisplayName("다양한 그리드 크기로 변환할 수 있다")
	void Given_DifferentGridSizes_When_ConvertToCacheGrid_Then_ReturnsDifferentGridPoints() {
		// given
		double latitude = 37.5665;
		double longitude = 126.9780;
		double grid1 = 1.0;
		double grid5 = 5.0;
		double grid10 = 10.0;

		// when
		GridPoint result1 = GridConverter.convertToCacheGrid(latitude, longitude, grid1);
		GridPoint result5 = GridConverter.convertToCacheGrid(latitude, longitude, grid5);
		GridPoint result10 = GridConverter.convertToCacheGrid(latitude, longitude, grid10);

		// then
		assertThat(result1).isNotNull();
		assertThat(result5).isNotNull();
		assertThat(result10).isNotNull();

		// 그리드 크기가 클수록 좌표값이 작아지는 경향이 있음
		assertThat(result1.gridX()).isGreaterThanOrEqualTo(result5.gridX());
		assertThat(result5.gridX()).isGreaterThanOrEqualTo(result10.gridX());
	}


	@Test
	@DisplayName("같은 좌표는 같은 그리드로 변환된다")
	void Given_SameCoordinates_When_ConvertToApiGrid_Then_ReturnsSameGridPoint() {
		// given
		double latitude = 37.5665;
		double longitude = 126.9780;

		// when
		GridPoint result1 = GridConverter.convertToApiGrid(latitude, longitude);
		GridPoint result2 = GridConverter.convertToApiGrid(latitude, longitude);

		// then
		assertThat(result1).isEqualTo(result2);
	}


	@Test
	@DisplayName("극한 좌표값도 처리할 수 있다")
	void Given_ExtremeCoordinates_When_ConvertToApiGrid_Then_ReturnsValidGridPoint() {
		// given
		double minLatitude = 33.0; // 한국 최남단
		double maxLatitude = 38.6; // 한국 최북단
		double minLongitude = 124.5; // 한국 최서단
		double maxLongitude = 132.0; // 한국 최동단

		// when & then
		assertThat(GridConverter.convertToApiGrid(minLatitude, minLongitude)).isNotNull();
		assertThat(GridConverter.convertToApiGrid(maxLatitude, maxLongitude)).isNotNull();
		assertThat(GridConverter.convertToApiGrid(minLatitude, maxLongitude)).isNotNull();
		assertThat(GridConverter.convertToApiGrid(maxLatitude, minLongitude)).isNotNull();
	}


	@Test
	@DisplayName("경도가 180도를 넘는 경우를 처리할 수 있다")
	void Given_LongitudeOver180_When_ConvertToApiGrid_Then_ReturnsValidGridPoint() {
		// given
		double latitude = 37.5665;
		double longitude = 181.0; // 180도 초과

		// when
		GridPoint result = GridConverter.convertToApiGrid(latitude, longitude);

		// then
		assertThat(result).isNotNull();
		assertThat(result.gridX()).isPositive();
		assertThat(result.gridY()).isPositive();
	}


	@Test
	@DisplayName("경도가 -180도 미만인 경우를 처리할 수 있다")
	void Given_LongitudeUnderMinus180_When_ConvertToApiGrid_Then_ReturnsValidGridPoint() {
		// given
		double latitude = 37.5665;
		double longitude = -181.0; // -180도 미만

		// when
		GridPoint result = GridConverter.convertToApiGrid(latitude, longitude);

		// then
		assertThat(result).isNotNull();
		assertThat(result.gridX()).isPositive();
		assertThat(result.gridY()).isPositive();
	}


	@Test
	@DisplayName("경도가 정확히 180도인 경우를 처리할 수 있다")
	void Given_LongitudeExactly180_When_ConvertToApiGrid_Then_ReturnsValidGridPoint() {
		// given
		double latitude = 37.5665;
		double longitude = 180.0; // 정확히 180도

		// when
		GridPoint result = GridConverter.convertToApiGrid(latitude, longitude);

		// then
		assertThat(result).isNotNull();
		assertThat(result.gridX()).isPositive();
		assertThat(result.gridY()).isPositive();
	}


	@Test
	@DisplayName("경도가 정확히 -180도인 경우를 처리할 수 있다")
	void Given_LongitudeExactlyMinus180_When_ConvertToApiGrid_Then_ReturnsValidGridPoint() {
		// given
		double latitude = 37.5665;
		double longitude = -180.0; // 정확히 -180도

		// when
		GridPoint result = GridConverter.convertToApiGrid(latitude, longitude);

		// then
		assertThat(result).isNotNull();
		assertThat(result.gridX()).isPositive();
		assertThat(result.gridY()).isPositive();
	}

}
