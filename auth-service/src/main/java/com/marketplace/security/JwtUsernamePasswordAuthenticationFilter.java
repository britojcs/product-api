package com.marketplace.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private JwtSecurityConfig jwtSecurityConfig;
  private AuthenticationManager authManager;


  public JwtUsernamePasswordAuthenticationFilter(JwtSecurityConfig jwtSecurityConfig,
      AuthenticationManager authManager) {
    super();
    this.jwtSecurityConfig = jwtSecurityConfig;
    this.authManager = authManager;

    this.setRequiresAuthenticationRequestMatcher(
        new AntPathRequestMatcher(jwtSecurityConfig.getUri(), "POST"));

  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException {

    try {

      UserCredentials userCredentials =
          new ObjectMapper().readValue(request.getInputStream(), UserCredentials.class);

      UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
          userCredentials.getUsername(), userCredentials.getPassword(), Collections.emptyList());

      return authManager.authenticate(authToken);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain, Authentication auth) throws IOException, ServletException {

    Long now = System.currentTimeMillis();
    String token = Jwts.builder().setSubject(auth.getName())
        .claim("authorities",
            auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()))
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(now + jwtSecurityConfig.getExpiration() * 1000))
        .signWith(SignatureAlgorithm.HS512, jwtSecurityConfig.getSecret().getBytes()).compact();

    response.addHeader(jwtSecurityConfig.getHeader(), jwtSecurityConfig.getPrefix() + token);
  }


  private static class UserCredentials {

    private String username;
    private String password;

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }

  }
}
