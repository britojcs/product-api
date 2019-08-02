package com.marketplace.security;

import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
public class JwtTokenSecurityConfigurer extends WebSecurityConfigurerAdapter {

  @Autowired
  private JwtSecurityConfig jwtSecurityConfig;

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.csrf().disable().sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    http.addFilterAfter(new JwtTokenValidationFilter(jwtSecurityConfig),
        UsernamePasswordAuthenticationFilter.class);

    http.exceptionHandling().authenticationEntryPoint(
        (req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage()));

    http.authorizeRequests().antMatchers(HttpMethod.POST, jwtSecurityConfig.getUri()).permitAll()
        .antMatchers(HttpMethod.DELETE, "/api/**").hasRole(UserRole.ADMIN.toString())
        .antMatchers(HttpMethod.PUT, "/api/**").hasRole(UserRole.USER.toString())
        .antMatchers(HttpMethod.POST, "/api/**").hasRole(UserRole.USER.toString())
        .antMatchers(HttpMethod.GET, "/api/**").permitAll();
  }

}
