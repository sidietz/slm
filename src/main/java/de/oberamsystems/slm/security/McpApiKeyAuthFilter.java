package de.oberamsystems.slm.security;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class McpApiKeyAuthFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(McpApiKeyAuthFilter.class);

	@Value("${slm.mcp.api-key:slm-mcp-secret-key-2026}")
	private String configuredApiKey;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getRequestURI();
		if (!isMcpPath(path)) {
			filterChain.doFilter(request, response);
			return;
		}

		String apiKey = extractApiKey(request);

		if (apiKey != null && apiKey.equals(configuredApiKey)) {
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					"mcp-client",
					null,
					Collections.singletonList(new SimpleGrantedAuthority("ROLE_MCP_CLIENT"))
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		} else {
			log.warn("Unauthorized access attempt to MCP endpoint '{}' with provided key: {}", path,
					apiKey != null ? "[PROTECTED]" : "NONE");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write("{\"error\":\"Unauthorized: Invalid or missing MCP API key\"}");
		}
	}

	private boolean isMcpPath(String path) {
		return path != null && (path.startsWith("/mcp") || path.equals("/sse") || path.startsWith("/sse/"));
	}

	private String extractApiKey(HttpServletRequest request) {
		// 1. Check X-API-KEY header
		String key = request.getHeader("X-API-KEY");
		if (key != null && !key.trim().isEmpty()) {
			return key.trim();
		}

		// 2. Check query parameter ?apiKey=
		key = request.getParameter("apiKey");
		if (key != null && !key.trim().isEmpty()) {
			return key.trim();
		}

		// 3. Check Authorization: Bearer <key>
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7).trim();
		}

		return null;
	}
}
