package es.uv.adiez.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import es.uv.adiez.security.CustomAuthorizationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
    @Autowired
    private UserDetailsService customUserDetailsService;
    
	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	@Bean
	@Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }
    
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(customUserDetailsService)
            .passwordEncoder(passwordEncoder());
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {   
  
    	http.csrf().disable()
    		.sessionManagement().sessionCreationPolicy(STATELESS)
    		.and()
    		.authorizeRequests()
    		//.antMatchers("/api/**").permitAll()
    		.antMatchers("/authenticate", "/authenticate/refresh").permitAll()
    		.antMatchers(HttpMethod.GET, "/api/producers/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
    		.antMatchers(HttpMethod.PUT, "/api/producers/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
    		.antMatchers(HttpMethod.DELETE, "/api/producers/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
    		.antMatchers(HttpMethod.GET, "/api/files/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
    		.and()
    		//.addFilter(customAuthenticationFilter)
    		.addFilterBefore(new CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
    	
    }
}
