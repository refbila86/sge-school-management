package mz.co.sge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
	{
		http.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(
						auth -> auth.requestMatchers("/login.xhtml", "/jakarta.faces.resource/**", "/javax.faces.resource/**", "/resources/**", "/public/**")
								.permitAll().anyRequest().authenticated())
				// IMPORTANTE: desativa formLogin porque tu já fazes login no LoginBean
				.formLogin(form -> form.disable()).logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login.xhtml?faces-redirect=true")
						.deleteCookies("JSESSIONID").invalidateHttpSession(true))
				.sessionManagement(sess -> sess.sessionFixation().migrateSession());

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder(12); // força 12 rounds, gera $2a$
	}
}