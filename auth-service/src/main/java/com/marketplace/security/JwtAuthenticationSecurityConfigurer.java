package com.marketplace.security;

import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@EnableWebSecurity
public class JwtAuthenticationSecurityConfigurer extends WebSecurityConfigurerAdapter {

  @Autowired
  private JwtSecurityConfig jwtSecurityConfig;

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.csrf().disable().sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    http.exceptionHandling().authenticationEntryPoint((req, res, e) -> {
      res.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
    });

    http.addFilter(
        new JwtUsernamePasswordAuthenticationFilter(jwtSecurityConfig, authenticationManager()));
    http.authorizeRequests().antMatchers(HttpMethod.POST, jwtSecurityConfig.getUri()).permitAll()
        .antMatchers("/error").permitAll().anyRequest().authenticated();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication().withUser("user").password("{noop}user").roles("USER").and()
        .withUser("admin").password("{noop}admin").roles("USER", "ADMIN");
  }

}
