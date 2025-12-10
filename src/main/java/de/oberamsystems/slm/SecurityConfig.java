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
						.requestMatchers("/imprint", "/imprint.html").permitAll()
						.requestMatchers("/activity", "/add-activity").permitAll()
						.requestMatchers("/sleeptime", "/sleeptime-bb", "/add-sleeptime").permitAll()
						.requestMatchers("/event", "/event.html", "/add-event", "/add-event.html").permitAll()
						.requestMatchers("/sport", "/add-sport").permitAll()
						.requestMatchers("/human", "/human.html", "/add-human", "/add-human.html").permitAll()
						.requestMatchers("/meditation", "/add-meditation").permitAll()
						.requestMatchers("/traintrip", "/traintrip.html", "/trainstation", "/trainstation.html", "/add-traintrip", "/add-traintrip.html", "/add-trainstation", "/add-trainstation.html").permitAll()
						.requestMatchers("/traintrip2", "/traintrip2.html", "/trainstation2", "/trainstation2.html", "/add-traintrip2", "/add-traintrip2.html", "/add-trainstation2", "/add-trainstation2.html", "/add-trainline", "/add-trainline.html").permitAll()
						.requestMatchers("/purchase", "/purchase.html", "/vendor", "/vendor.html", "/add-purchase", "/add-purchase.html", "/add-vendor", "/add-vendor.html").permitAll()
						.requestMatchers("/contract", "/contract.html", "/contractor", "/contractor.html", "/add-contract", "/add-contract.html", "/add-contractor", "/add-contractor.html").permitAll()
						.requestMatchers("/habit", "/habit.html", "/habitentry", "/habitentry.html", "/add-habit", "/add-habit.html", "/add-habitentry", "/add-habitentry.html", "/last-done-habitentry").permitAll()
						.requestMatchers("/gaming-session", "/gaming-session.html", "/game", "/game.html", "/add-gaming-session", "/add-game", "/add-publisher", "/add-studio", "/publisher", "/studio", "/add-manufacturer", "/add-device-type", "/manufacturer", "/device-type", "/add-device", "/device").permitAll()
						.requestMatchers("/gratitude", "/gratitude.html", "/add-gratitude", "/add-gratitude.html").permitAll()
						.requestMatchers("/mood", "/mood.html", "/add-mood", "/add-mood.html", "/mood-bb", "/mood-bb.html").permitAll()
						.requestMatchers("/book", "/book.html", "/author", "/author.html", "/press", "/press.html", "/readingsession", "/readingsession.html", "/add-book", "/add-book.html", "/add-author", "/add-author.html", "/add-press", "/add-press.html", "/add-reading-session", "/add-reading-session.html").permitAll()
						.requestMatchers("/learning-session", "/learning-session.html", "/learningitem", "/learningitem.html", "/add-learning-session", "/add-learningitem").permitAll()
						.requestMatchers("/api/sleeptimes", "/api/moods").permitAll()
						.requestMatchers("/doctor", "/add-doctor", "/appointment", "/add-appointment", "/speciality", "/add-speciality").permitAll()
						.requestMatchers("/activity-management", "/book-management", "/human-management", "/habit-management", "/spend-management", "/misc-management").permitAll()
						.requestMatchers("/devices", "/games", "/sport-types", "/activity-types").permitAll()
						.requestMatchers("/publishers", "/studios", "/vendors", "/manufacturers").permitAll()
						.requestMatchers("/activities", "/sports", "/sleep-times", "/gaming-sessions").permitAll()
						.requestMatchers("/books", "/authors", "/reading-sessions").permitAll()
						.requestMatchers("/humans").permitAll()
						.requestMatchers("/last-done-habit-entries").permitAll()
						.requestMatchers("/train-trips", "/train-stations", "/train-lines").permitAll()
						.requestMatchers("/purchases", "/contracts", "/contractors").permitAll()
						.requestMatchers("/learning-sessions", "/habits", "/meditations", "/gratitudes").permitAll()
						.requestMatchers("/habit-entries", "/moods", "/learning-items", "/habits").permitAll()
						.requestMatchers("/api/**")
						.authenticated().anyRequest().denyAll())
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
