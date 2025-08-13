package com.und.server.scenario.dto.response;

import java.util.List;

import com.und.server.scenario.entity.Scenario;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Scenarios order update response")
public record OrderUpdateResponse(

	@Schema(description = "Reordering all Scenarios", example = "false")
	Boolean isReorder,

	@ArraySchema(
		arraySchema = @Schema(
			description = """
				List of (id, order) pairs reflecting the final order.
				When isReorder=false, it usually contains only one item.
				When true, it includes all affected scenarios.
				"""),
		schema = @Schema(implementation = OrderResponse.class), minItems = 1, maxItems = 20
	)
	List<OrderResponse> orderUpdates

) {

	public static OrderUpdateResponse from(final List<Scenario> scenarios, final Boolean isReorder) {
		List<OrderResponse> orderResponses = scenarios.stream()
			.map(OrderResponse::from)
			.toList();

		return OrderUpdateResponse.builder()
			.isReorder(isReorder)
			.orderUpdates(orderResponses)
			.build();
	}

}
