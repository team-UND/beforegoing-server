package com.und.server.weather.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.und.server.weather.constants.FineDustType;
import com.und.server.weather.constants.UvType;
import com.und.server.weather.constants.WeatherType;
import com.und.server.weather.dto.cache.WeatherCacheData;

@DisplayName("CacheSerializer 테스트")
class CacheSerializerTest {

	private CacheSerializer cacheSerializer;

	@BeforeEach
	void setUp() {
		cacheSerializer = new CacheSerializer();
	}

	@Test
	@DisplayName("WeatherCacheData를 JSON으로 직렬화할 수 있다")
	void Given_WeatherCacheData_When_Serialize_Then_ReturnsJsonString() {
		// given
		WeatherCacheData data = WeatherCacheData.builder()
			.weather(WeatherType.SUNNY)
			.fineDust(FineDustType.GOOD)
			.uv(UvType.LOW)
			.build();

		// when
		String result = cacheSerializer.serializeWeatherCacheData(data);

		// then
		assertThat(result).isNotNull();
		assertThat(result).contains("SUNNY");
		assertThat(result).contains("GOOD");
		assertThat(result).contains("LOW");
	}


	@Test
	@DisplayName("JSON을 WeatherCacheData로 역직렬화할 수 있다")
	void Given_JsonString_When_Deserialize_Then_ReturnsWeatherCacheData() {
		// given
		String json = """
			{
				"weather": "SUNNY",
				"fineDust": "GOOD",
				"uv": "LOW"
			}
			""";

		// when
		WeatherCacheData result = cacheSerializer.deserializeWeatherCacheData(json);

		// then
		assertThat(result).isNotNull();
		assertThat(result.weather()).isEqualTo(WeatherType.SUNNY);
		assertThat(result.fineDust()).isEqualTo(FineDustType.GOOD);
		assertThat(result.uv()).isEqualTo(UvType.LOW);
	}


	@Test
	@DisplayName("직렬화 후 역직렬화하면 원본 데이터와 같다")
	void Given_WeatherCacheData_When_SerializeAndDeserialize_Then_ReturnsOriginalData() {
		// given
		WeatherCacheData originalData = WeatherCacheData.builder()
			.weather(WeatherType.RAIN)
			.fineDust(FineDustType.BAD)
			.uv(UvType.HIGH)
			.build();

		// when
		String serialized = cacheSerializer.serializeWeatherCacheData(originalData);
		WeatherCacheData deserialized = cacheSerializer.deserializeWeatherCacheData(serialized);

		// then
		assertThat(deserialized).isNotNull();
		assertThat(deserialized.weather()).isEqualTo(originalData.weather());
		assertThat(deserialized.fineDust()).isEqualTo(originalData.fineDust());
		assertThat(deserialized.uv()).isEqualTo(originalData.uv());
	}


	@Test
	@DisplayName("WeatherCacheData 맵을 해시로 직렬화할 수 있다")
	void Given_WeatherCacheDataMap_When_SerializeToHash_Then_ReturnsStringMap() {
		// given
		Map<String, WeatherCacheData> hourlyData = new HashMap<>();
		hourlyData.put("12", WeatherCacheData.builder()
			.weather(WeatherType.SUNNY)
			.fineDust(FineDustType.GOOD)
			.uv(UvType.LOW)
			.build());
		hourlyData.put("13", WeatherCacheData.builder()
			.weather(WeatherType.CLOUDY)
			.fineDust(FineDustType.NORMAL)
			.uv(UvType.NORMAL)
			.build());

		// when
		Map<String, String> result = cacheSerializer.serializeWeatherCacheDataToHash(hourlyData);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get("12")).contains("SUNNY");
		assertThat(result.get("13")).contains("CLOUDY");
	}


	@Test
	@DisplayName("해시에서 WeatherCacheData를 역직렬화할 수 있다")
	void Given_JsonString_When_DeserializeFromHash_Then_ReturnsWeatherCacheData() {
		// given
		String json = """
			{
				"weather": "SNOW",
				"fineDust": "VERY_BAD",
				"uv": "VERY_HIGH"
			}
			""";

		// when
		WeatherCacheData result = cacheSerializer.deserializeWeatherCacheDataFromHash(json);

		// then
		assertThat(result).isNotNull();
		assertThat(result.weather()).isEqualTo(WeatherType.SNOW);
		assertThat(result.fineDust()).isEqualTo(FineDustType.VERY_BAD);
		assertThat(result.uv()).isEqualTo(UvType.VERY_HIGH);
	}


	@Test
	@DisplayName("빈 WeatherCacheData를 직렬화할 수 있다")
	void Given_EmptyWeatherCacheData_When_Serialize_Then_ReturnsEmptyJson() {
		// given
		WeatherCacheData data = WeatherCacheData.builder().build();

		// when
		String result = cacheSerializer.serializeWeatherCacheData(data);

		// then
		assertThat(result).isNotNull();
		assertThat(result).contains("{}");
	}


	@Test
	@DisplayName("빈 JSON을 역직렬화하면 null을 반환한다")
	void Given_EmptyJson_When_Deserialize_Then_ReturnsNullValues() {
		// given
		String json = "{}";

		// when
		WeatherCacheData result = cacheSerializer.deserializeWeatherCacheData(json);

		// then
		assertThat(result).isNotNull();
		assertThat(result.weather()).isNull();
		assertThat(result.fineDust()).isNull();
		assertThat(result.uv()).isNull();
	}


	@Test
	@DisplayName("잘못된 JSON을 역직렬화하면 null을 반환한다")
	void Given_InvalidJson_When_DeserializeWeatherCacheData_Then_ReturnsNull() {
		// given
		String invalidJson = "{ invalid json }";

		// when
		WeatherCacheData result = cacheSerializer.deserializeWeatherCacheData(invalidJson);

		// then
		assertThat(result).isNull();
	}


	@Test
	@DisplayName("null JSON을 역직렬화하면 예외가 발생한다")
	void Given_NullJson_When_DeserializeWeatherCacheData_Then_ThrowsException() {
		// given
		String nullJson = null;

		// when & then
		assertThatThrownBy(() -> cacheSerializer.deserializeWeatherCacheData(nullJson))
			.isInstanceOf(IllegalArgumentException.class);
	}


	@Test
	@DisplayName("null WeatherCacheData를 직렬화하면 null 문자열을 반환한다")
	void Given_NullWeatherCacheData_When_SerializeWeatherCacheData_Then_ReturnsNullString() {
		// given
		WeatherCacheData nullData = null;

		// when
		String result = cacheSerializer.serializeWeatherCacheData(nullData);

		// then
		assertThat(result).isEqualTo("null");
	}


	@Test
	@DisplayName("모든 WeatherType에 대해 직렬화/역직렬화가 가능하다")
	void Given_AllWeatherTypes_When_SerializeAndDeserializeWeatherCacheData_Then_ReturnsCorrectData() {
		// given
		for (WeatherType weatherType : WeatherType.values()) {
			WeatherCacheData data = WeatherCacheData.builder()
				.weather(weatherType)
				.fineDust(FineDustType.GOOD)
				.uv(UvType.LOW)
				.build();

			// when
			String serialized = cacheSerializer.serializeWeatherCacheData(data);
			WeatherCacheData deserialized = cacheSerializer.deserializeWeatherCacheData(serialized);

			// then
			assertThat(deserialized).isNotNull();
			assertThat(deserialized.weather()).isEqualTo(weatherType);
		}
	}


	@Test
	@DisplayName("모든 FineDustType에 대해 직렬화/역직렬화가 가능하다")
	void Given_AllFineDustTypes_When_SerializeAndDeserializeWeatherCacheData_Then_ReturnsCorrectData() {
		// given
		for (FineDustType fineDustType : FineDustType.values()) {
			WeatherCacheData data = WeatherCacheData.builder()
				.weather(WeatherType.SUNNY)
				.fineDust(fineDustType)
				.uv(UvType.LOW)
				.build();

			// when
			String serialized = cacheSerializer.serializeWeatherCacheData(data);
			WeatherCacheData deserialized = cacheSerializer.deserializeWeatherCacheData(serialized);

			// then
			assertThat(deserialized).isNotNull();
			assertThat(deserialized.fineDust()).isEqualTo(fineDustType);
		}
	}


	@Test
	@DisplayName("모든 UvType에 대해 직렬화/역직렬화가 가능하다")
	void Given_AllUvTypes_When_SerializeAndDeserializeWeatherCacheData_Then_ReturnsCorrectData() {
		// given
		for (UvType uvType : UvType.values()) {
			WeatherCacheData data = WeatherCacheData.builder()
				.weather(WeatherType.SUNNY)
				.fineDust(FineDustType.GOOD)
				.uv(uvType)
				.build();

			// when
			String serialized = cacheSerializer.serializeWeatherCacheData(data);
			WeatherCacheData deserialized = cacheSerializer.deserializeWeatherCacheData(serialized);

			// then
			assertThat(deserialized).isNotNull();
			assertThat(deserialized.uv()).isEqualTo(uvType);
		}
	}


	@Test
	@DisplayName("해시 직렬화에서 null 데이터도 처리한다")
	void Given_MapWithNullData_When_SerializeToHash_Then_ProcessesAllData() {
		// given
		Map<String, WeatherCacheData> hourlyData = new HashMap<>();
		hourlyData.put("12", WeatherCacheData.builder()
			.weather(WeatherType.SUNNY)
			.fineDust(FineDustType.GOOD)
			.uv(UvType.LOW)
			.build());
		// null 데이터는 "null" 문자열로 직렬화됨
		hourlyData.put("13", null);

		// when
		Map<String, String> result = cacheSerializer.serializeWeatherCacheDataToHash(hourlyData);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get("12")).contains("SUNNY");
		assertThat(result.get("13")).isEqualTo("null");
	}


	@Test
	@DisplayName("해시에서 잘못된 JSON을 역직렬화하면 null을 반환한다")
	void Given_InvalidJson_When_DeserializeFromHash_Then_ReturnsNull() {
		// given
		String invalidJson = "{ invalid json }";

		// when
		WeatherCacheData result = cacheSerializer.deserializeWeatherCacheDataFromHash(invalidJson);

		// then
		assertThat(result).isNull();
	}


	@Test
	@DisplayName("해시에서 null JSON을 역직렬화하면 예외가 발생한다")
	void Given_NullJson_When_DeserializeFromHash_Then_ThrowsException() {
		// given
		String nullJson = null;

		// when & then
		assertThatThrownBy(() -> cacheSerializer.deserializeWeatherCacheDataFromHash(nullJson))
			.isInstanceOf(IllegalArgumentException.class);
	}


	@Test
	@DisplayName("빈 맵을 해시로 직렬화하면 빈 맵을 반환한다")
	void Given_EmptyMap_When_SerializeToHash_Then_ReturnsEmptyMap() {
		// given
		Map<String, WeatherCacheData> emptyMap = new HashMap<>();

		// when
		Map<String, String> result = cacheSerializer.serializeWeatherCacheDataToHash(emptyMap);

		// then
		assertThat(result).isEmpty();
	}


	@Test
	@DisplayName("해시에서 빈 JSON을 역직렬화하면 null 값을 가진 객체를 반환한다")
	void Given_EmptyJson_When_DeserializeFromHash_Then_ReturnsNullValues() {
		// given
		String json = "{}";

		// when
		WeatherCacheData result = cacheSerializer.deserializeWeatherCacheDataFromHash(json);

		// then
		assertThat(result).isNotNull();
		assertThat(result.weather()).isNull();
		assertThat(result.fineDust()).isNull();
		assertThat(result.uv()).isNull();
	}

}
