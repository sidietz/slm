package de.oberamsystems.slm.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

	@Bean
	public ToolCallbackProvider slmMcpToolCallbackProvider(
			McpLifeEventTools lifeEventTools,
			McpSessionTools sessionTools,
			McpWellnessTools wellnessTools,
			McpMedicalTools medicalTools,
			McpAcademicTools academicTools,
			McpVehicleTools vehicleTools) {

		return MethodToolCallbackProvider.builder()
				.toolObjects(lifeEventTools, sessionTools, wellnessTools, medicalTools, academicTools, vehicleTools)
				.build();
	}
}
