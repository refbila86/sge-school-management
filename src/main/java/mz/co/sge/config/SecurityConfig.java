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
		http
				// Desativa CSRF para requisições AJAX do PrimeFaces/JSF
				.csrf(csrf -> csrf.disable())

				// Configuração de permissões de acesso
				.authorizeHttpRequests(auth -> auth
						// Libera recursos estáticos do PrimeFaces / JSF
						.requestMatchers("/jakarta.faces.resource/**", "/javax.faces.resource/**").permitAll()
						// Libera a página de login
						.requestMatchers("/login.xhtml").permitAll()
						// Qualquer outra página exige autenticação
						.anyRequest().authenticated())

				// Formulário de Login adaptado ao JSF/PrimeFaces
				.formLogin(form -> form.loginPage("/login.xhtml").loginProcessingUrl("/login").defaultSuccessUrl("/index.xhtml", true)
						.failureUrl("/login.xhtml?error=true").permitAll())

				// Configuração de Logout
				.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login.xhtml?logout=true").deleteCookies("JSESSIONID").permitAll());

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder()
	{
		return new BCryptPasswordEncoder();
	}
}