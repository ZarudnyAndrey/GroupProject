package com.skillbox.sw.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbox.sw.api.response.ResponseApi;
import com.skillbox.sw.config.jwt.JwtAuthenticationEntryPoint;
import com.skillbox.sw.config.jwt.JwtAuthenticationFilter;
import com.skillbox.sw.config.jwt.JwtAuthorizationFilter;
import com.skillbox.sw.service.PersonService;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

@Slf4j
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  private PersonService personService;
  private ApplicationContext applicationContext;
  private ObjectMapper objectMapper;

  private static final String[] AUTH_WHITELIST = {
      // -- swagger ui
      "/v2/api-docs",
      "/swagger-resources",
      "/swagger-resources/**",
      "/configuration/ui",
      "/configuration/security",
      "/swagger-ui.html",
      "/webjars/**",
      "/h2-console/**"
      // other public endpoints of your API may be appended to this array
  };

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors().and().csrf().disable().authorizeRequests()
        .antMatchers(HttpMethod.POST,
            SecurityConstants.API_LOGIN_URL,
            SecurityConstants.API_REGISTER_URL,
            SecurityConstants.API_PASSWORD_RECOVERY_URL)
        .permitAll()
        .antMatchers(AUTH_WHITELIST)
        .permitAll()
        .anyRequest().authenticated()
        .and()
        .addFilter(authenticationFilter())
        .addFilter(new JwtAuthorizationFilter(authenticationManager()))
        .exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint())
        .and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .headers().frameOptions().sameOrigin();

    http
        .logout()
        .logoutUrl(SecurityConstants.API_LOGOUT_URL)
        .logoutSuccessHandler(this::logoutSuccessHandler);

  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(personService).passwordEncoder(bCryptPasswordEncoder());
  }

  @Bean
  public JwtAuthenticationFilter authenticationFilter() throws Exception {
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(objectMapper);
    filter.setFilterProcessesUrl(SecurityConstants.API_LOGIN_URL);
    filter.setAuthenticationManager(authenticationManager());
    return filter;
  }

  @Bean
  public PasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @EventListener
  public void handle(ContextRefreshedEvent event) {
    FilterChainProxy proxy = applicationContext.getBean(FilterChainProxy.class);
    for (Filter f : proxy.getFilters("/")) {
      if (f instanceof FilterSecurityInterceptor) {
        ((FilterSecurityInterceptor) f).setPublishAuthorizationSuccess(true);
      }
    }
  }

  private void logoutSuccessHandler(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    response.setStatus(HttpStatus.OK.value());
    objectMapper.writeValue(
        response.getWriter(),
        new ResponseApi<>(
            "Bye and cheers",
            new ResponseApi.Message("ok")
        )
    );
  }
}