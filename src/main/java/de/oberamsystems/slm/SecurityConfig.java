package de.oberamsystems.slm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.cors(Customizer.withDefaults()).csrf(Customizer.withDefaults())
				.authorizeHttpRequests(authorize -> authorize.requestMatchers("/").permitAll()
						.requestMatchers("/", "/index", "/index.html", "/login/*", "/error", "/webjars/**", "/css/**").permitAll()
						.requestMatchers("/sleeptime", "/sleeptime-bb", "/add-sleeptime").permitAll()
						.requestMatchers("/event", "/event.html", "/add-event", "/add-event.html").permitAll()
						.requestMatchers("/sport", "/add-sport").permitAll()
						.requestMatchers("/human", "/human.html", "/add-human", "/add-human.html").permitAll()
						.requestMatchers("/meditation", "/add-meditation").permitAll()
						.requestMatchers("/api/**").authenticated().anyRequest().denyAll())
				.formLogin(Customizer.withDefaults()).httpBasic(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder().username("simon").password("N0m1596.").roles("USER")
				.build();

		UserDetails admin = User.withDefaultPasswordEncoder().username("admin").password("N0m1596.").roles("ADMIN")
				.build();

		return new InMemoryUserDetailsManager(user, admin);
	}
}
