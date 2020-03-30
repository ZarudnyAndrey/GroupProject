package com.skillbox.sw.config.jwt;

import com.skillbox.sw.config.SecurityConstants;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

  public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
    super(authenticationManager);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    String header = request.getHeader(SecurityConstants.HEADER);

    if (header == null || !header.startsWith(SecurityConstants.PREFIX)) {
      log.info("doFilterInternal: request without or unknown server prefix ");
      chain.doFilter(request, response);
    }

    UsernamePasswordAuthenticationToken authenticationToken = getAuthentication(request);

    if (authenticationToken != null) {
      log.info("doFilterInternal: authenticationToken is not null");
      SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      chain.doFilter(request, response);
    }
  }

  private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
    String token = request.getHeader(SecurityConstants.HEADER);
    if (token != null && JwtProvider.validateToken(token)) {
      String user = JwtProvider.getUsername(token);
      if (user != null) {
        return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
      }
      return null;
    }
    return null;
  }
}