package com.skillbox.sw.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbox.sw.api.response.ErrorApi;
import com.skillbox.sw.api.response.ResponseApi;
import com.skillbox.sw.api.response.ResponsePersonApi;
import com.skillbox.sw.config.SecurityConstants;
import com.skillbox.sw.domain.Person;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@AllArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private ObjectMapper objectMapper;

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    log.info("On attemptAuthentication");
    try {

      Person cred = objectMapper.readValue(request.getInputStream(), Person.class);

      return getAuthenticationManager()
          .authenticate(
              new UsernamePasswordAuthenticationToken(
                  cred.getEmail(), cred.getPassword(), new ArrayList<>()));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authResult)
      throws IOException, ServletException {
    log.info("On successfulAuthentication");
    String token = JwtProvider.createToken(((User) authResult.getPrincipal()).getUsername());
    response.addHeader(SecurityConstants.HEADER, SecurityConstants.PREFIX + token);
    objectMapper.writeValue(
        response.getWriter(),
        new ResponseApi<>(
            "Successful authentication for " + JwtProvider.getUsername(token),
            ResponsePersonApi.builder().token(SecurityConstants.PREFIX + token).build()));
  }

  @Override
  protected void unsuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
      throws IOException, ServletException {
    log.info("On failedAuthentication");
    objectMapper.writeValue(
        response.getWriter(), new ErrorApi("Authentication failed", "Wrong username or password"));
  }
}