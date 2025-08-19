package com.und.server.weather.util;

import org.springframework.stereotype.Component;

import com.und.server.weather.dto.GridPoint;

/**
 * 위도/경도를 기상청 격자 좌표로 변환하는 유틸리티
 * Lambert Conformal Conic 투영법 사용
 */
@Component
public class GridConverter {

	// 기상청 격자 변환 상수들
	private static final double RE = 6371.00877;     // 지구 반경(km)
	private static final double GRID = 5.0;          // 격자 간격(km)
	private static final double SLAT1 = 30.0;        // 투영 위도1(degree)
	private static final double SLAT2 = 60.0;        // 투영 위도2(degree)
	private static final double OLON = 126.0;        // 기준점 경도(degree)
	private static final double OLAT = 38.0;         // 기준점 위도(degree)
	private static final double XO = 43;             // 기준점 X좌표(GRID)
	private static final double YO = 136;            // 기준점 Y좌표(GRID)

	private static final double DEGRAD = Math.PI / 180.0;
	private static final double RADDEG = 180.0 / Math.PI;

	/**
	 * 위도/경도를 기상청 격자 좌표로 변환
	 *
	 * @param latitude  위도 (degree)
	 * @param longitude 경도 (degree)
	 * @return 격자 좌표 (x, y)
	 */
	public static GridPoint convertToGrid(double latitude, double longitude) {

		double re = RE / GRID;
		double slat1 = SLAT1 * DEGRAD;
		double slat2 = SLAT2 * DEGRAD;
		double olon = OLON * DEGRAD;
		double olat = OLAT * DEGRAD;

		double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
		sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);

		double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
		sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;

		double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
		ro = re * sf / Math.pow(ro, sn);

		double ra = Math.tan(Math.PI * 0.25 + latitude * DEGRAD * 0.5);
		ra = re * sf / Math.pow(ra, sn);

		double theta = longitude * DEGRAD - olon;
		if (theta > Math.PI) {
			theta -= 2.0 * Math.PI;
		}
		if (theta < -Math.PI) {
			theta += 2.0 * Math.PI;
		}
		theta *= sn;

		int x = (int) Math.floor(ra * Math.sin(theta) + XO + 0.5);
		int y = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

		return GridPoint.builder().x(x).y(y).build();
	}

	/**
	 * 격자 좌표를 위도/경도로 역변환 (테스트용)
	 *
	 * @param gridPoint 격자 좌표
	 * @return [위도, 경도] 배열
	 */
	public static double[] convertToLatLon(GridPoint gridPoint) {

		double re = RE / GRID;
		double slat1 = SLAT1 * DEGRAD;
		double slat2 = SLAT2 * DEGRAD;
		double olon = OLON * DEGRAD;
		double olat = OLAT * DEGRAD;

		double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
		sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);

		double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
		sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;

		double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
		ro = re * sf / Math.pow(ro, sn);

		double xn = gridPoint.x() - XO;
		double yn = ro - gridPoint.y() + YO;
		double ra = Math.sqrt(xn * xn + yn * yn);
		if (sn < 0.0) {
			ra = -ra;
		}

		double alat = Math.pow((re * sf / ra), (1.0 / sn));
		alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

		double theta = 0.0;
		if (Math.abs(xn) <= 0.0) {
			theta = 0.0;
		} else {
			if (Math.abs(yn) <= 0.0) {
				theta = Math.PI * 0.5;
				if (xn < 0.0) {
					theta = -theta;
				}
			} else {
				theta = Math.atan2(xn, yn);
			}
		}

		double alon = theta / sn + olon;

		double latitude = alat * RADDEG;
		double longitude = alon * RADDEG;

		return new double[] {latitude, longitude};
	}
}
