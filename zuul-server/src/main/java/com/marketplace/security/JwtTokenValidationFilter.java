package com.marketplace.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtTokenValidationFilter extends OncePerRequestFilter {

  private JwtSecurityConfig jwtSecurityConfig;

  public JwtTokenValidationFilter(JwtSecurityConfig jwtSecurityConfig) {
    super();
    this.jwtSecurityConfig = jwtSecurityConfig;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {


    String header = request.getHeader(jwtSecurityConfig.getHeader());

    if (header == null || !header.startsWith(jwtSecurityConfig.getPrefix())) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.replace(jwtSecurityConfig.getPrefix(), "");

    try {

      Claims claims = Jwts.parser().setSigningKey(jwtSecurityConfig.getSecret().getBytes())
          .parseClaimsJws(token).getBody();

      String username = claims.getSubject();
      if (username != null) {
        @SuppressWarnings("unchecked")
        List<String> authorities = (List<String>) claims.get("authorities");


        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(username, null,
                authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));


        SecurityContextHolder.getContext().setAuthentication(auth);
      }

    } catch (Exception e) {

      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }

}
